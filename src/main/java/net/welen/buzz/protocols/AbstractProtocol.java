/*
 * Buzz - a monitoring framework for JBoss
 *
 * Copyright 2012-2014 Anders Welén, anders@welen.net
 * 
 * This file is part of Buzz.
 *
 * Buzz is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * Buzz is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Buzz.  If not, see <http://www.gnu.org/licenses/>. 
 */
package net.welen.buzz.protocols;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import net.welen.buzz.typehandler.TypeHandler;
import net.welen.buzz.typehandler.TypeHandlerException;
import net.welen.buzz.typehandler.utils.jmxinvoker.JMXInvokerFactory;
import net.welen.buzz.utils.RPNCalculator;

import org.apache.log4j.Logger;

/**
 * @author welle
 *
 */
public abstract class AbstractProtocol implements Protocol, ProtocolMBean {

	private static final Logger LOG = Logger.getLogger(AbstractProtocol.class);
	
	private ObjectName configuration;
	private Boolean enabled = false;
	
	public String getConfigurationMBeanName() {
		return configuration.toString();
	}

	public void setConfigurationMBeanName(String configuration) throws MalformedObjectNameException, NullPointerException {
		if (configuration == null) {
			// Ignore this as JBoss 7 keeps sending null during "uninject"			
			return;
		}
		this.configuration = new ObjectName(configuration);
	}

	/* (non-Javadoc)
	 * @see net.welen.buzz.protocols.ProtocolMBean#setEnabled(boolean)
	 */
	public void setEnabled(Boolean value) {
		enabled = value;
	}

	/* (non-Javadoc)
	 * @see net.welen.buzz.protocols.ProtocolMBean#getEnabled()
	 */
	public Boolean getEnabled() {
		return enabled;
	}
	
	/**
	 * Callback for starting the protocol
	 * 
	 * @throws Exception
	 */
	public void start() throws Exception {		
		if (enabled) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("Starting the protocol: " + this.getClass().getName());
			}
			startProtocol();
		}
	}

	/**
	 * Callback for stopping the protocol
	 * 
	 * @throws Exception
	 */
	public void stop() throws Exception {
		if (enabled) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("Stopping the protocol: " + this.getClass().getName());
			}			
			stopProtocol();
		}
	}

	/**
	 * Helper method. Executes a fetch of all data.
	 * 
	 * @param answer
	 */
	protected void getValues(BuzzAnswer answer) {
		for (TypeHandler typeHandler : getHandlers()) {
			try {
				typeHandler.getValues(answer);
			} catch (TypeHandlerException e) {
				// Just log it and continue. Hopefully we can get SOME data at least!
				LOG.error(e.getMessage(), e);
			}
		}			
		
		if (LOG.isDebugEnabled()) {
			LOG.debug("Returning " + answer);
		}
	}
	
	/**
	 * Helper method. Filters out all warnings
	 * 
	 * @param answer
	 */
	protected void filterWarnings(BuzzAnswer input, BuzzAnswer output) {
		filter(input, output, "warning");
	}
	
	/**
	 * Helper method. Filters out all critical values
	 * 
	 * @param answer
	 */
	protected void filterAlarms(BuzzAnswer input, BuzzAnswer output) {
		filter(input, output, "critical");
	}
		
	private void filter(BuzzAnswer input, BuzzAnswer output, String filterString) {
		for (TypeHandler typeHandler : getHandlers()) {
			try {				
				for (String measurableUnit : typeHandler.getMeasurableUnits()) {
					// Get all critical attributes from config
					ArrayList<String> thresholdAttributes = new ArrayList<String>();
					Enumeration<Object> keys = typeHandler.getProperties().keys();
					while(keys.hasMoreElements()) {
						String key = (String) keys.nextElement();
						
						if (key.matches("^threshold\\..*\\." + filterString + "$")) {
							thresholdAttributes.add(key);			
						}
					}

					// Check current values					
					for (String key : thresholdAttributes) {
						String keyPart = key.split("\\.")[1];
						
						String currentValue = input.getIndividualValue(typeHandler.getCategory(), measurableUnit, keyPart).toString();
						String threshold = typeHandler.getProperties().getProperty(key);
						
						// Override?
						String overrideThreshold = typeHandler.getProperties().getProperty(key + "." + measurableUnit);
						if (overrideThreshold != null) {							
							threshold = overrideThreshold;
						}
						
						// Calculate and check
						if (threshold.length() == 0) {
							continue;
						}
						String[] thresholds = calculatedThreshold(threshold, input.getAttributes(typeHandler.getCategory(), measurableUnit));						
						if (thresholds.length < 1 || thresholds.length > 2) {
							LOG.error("The " + filterString + " setting is corrupt. Got #" + thresholds.length + " parts");
							continue;
						}
						try {
							if (thresholds.length == 1) {
								if (Double.parseDouble(currentValue) < Double.parseDouble(thresholds[0])) {
									output.put(typeHandler.getCategory(), measurableUnit, keyPart, currentValue);
								}
							} else {
								if (thresholds[0].length() > 0 && Double.parseDouble(currentValue) < Double.parseDouble(thresholds[0])) {
									output.put(typeHandler.getCategory(), measurableUnit, keyPart, currentValue);
								}
								if (thresholds[1].length() > 0 &&  Double.parseDouble(currentValue) > Double.parseDouble(thresholds[1])) {
									output.put(typeHandler.getCategory(), measurableUnit, keyPart, currentValue);
								}
							}						
						} catch (NumberFormatException e) {
							LOG.error("The current value (" + currentValue + ") for \"" + key + "\" or threshold values are NOT a Double");
							continue;
						}
					}										
				}
			} catch (Throwable t) {
				// Just log it and continue. Hopefully we can get SOME data at least!
				LOG.error("Problem filtering typehandler: " + typeHandler, t);
			}

		}
	}
	
	private String[] calculatedThreshold(String threshold, Map<String, Object> values) {
		// Replace all placeholders with the correct values		
		for (Entry<String, Object> entry : values.entrySet()) {
			threshold = threshold.replace(entry.getKey(), entry.getValue().toString());
		}
		
		DecimalFormatSymbols symbols = new DecimalFormatSymbols();
		symbols.setDecimalSeparator('.');
		DecimalFormat formatter = new DecimalFormat("#.#", symbols); 
		
		// Execute a RPN calculation and return the answer
		String[] parts = threshold.split(":");		
			
		for (int i=0; i<parts.length; i++) {
			if (parts[i].length() > 0) {
				parts[i] = "" + formatter.format(RPNCalculator.calculate(parts[i]));
			}
		}
		
		if (LOG.isDebugEnabled()) {
			LOG.debug("Threshold: " + threshold + " Values: " + values + " returning " + Arrays.toString(parts));
		}
		
		return parts;
	}

	@SuppressWarnings("unchecked")
	protected List<TypeHandler> getHandlers() {
		return (List<TypeHandler>) JMXInvokerFactory.getJMXInvoker().getAttributeValue(getConfigurationMBeanName(), "TypeHandlers");
	}

	protected TypeHandler getHandler(String name) {
		Object[] params = new Object[1];
		params[0] = name;
		String[] signature = new String[1];
		signature[0] = "java.lang.String";
		return (TypeHandler) JMXInvokerFactory.getJMXInvoker().executeMethod(getConfigurationMBeanName(), "getTypeHandler", params, signature);
	}

}
