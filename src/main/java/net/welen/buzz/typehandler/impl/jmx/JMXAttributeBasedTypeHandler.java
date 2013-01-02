/*
 * Buzz - a monitoring framework for JBoss
 *
 * Copyright 2012-2013 Anders Welén, anders@welen.net
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
package net.welen.buzz.typehandler.impl.jmx;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.management.MalformedObjectNameException;

import net.welen.buzz.protocols.BuzzAnswer;
import net.welen.buzz.typehandler.AbstractTypeHandler;
import net.welen.buzz.typehandler.TypeHandlerException;
import net.welen.buzz.typehandler.utils.jmxinvoker.JMXInvoker;
import net.welen.buzz.typehandler.utils.jmxinvoker.JMXInvokerFactory;

import org.apache.log4j.Logger;


/**
 * Handler for JMX Attributes
 * 
 * @author welle
 */
public class JMXAttributeBasedTypeHandler extends AbstractTypeHandler {

	private static final Logger LOG = Logger.getLogger(JMXAttributeBasedTypeHandler.class);
		
	// A lookup table for finding the correct MBean name from the internal Buzz name of a measurement
	protected HashMap<String, String> nameToBeanNameTable = new HashMap<String, String>();

	/**
	 * @param params
	 */
	public JMXAttributeBasedTypeHandler(String category, String measurementUnit, Properties typeHandlerProperties) {
		super(category, measurementUnit, typeHandlerProperties);
	}

	public void getValues(BuzzAnswer values) throws TypeHandlerException {

		boolean debug = LOG.isDebugEnabled();

		for (String name : getMeasurableUnits()) {
			// Lookup the MBean name
			String beanName = nameToBeanNameTable.get(name);
			if (beanName == null) {
				LOG.error("No MBean name found for the internal name: " + name + ". Skipping it.");
				continue;
			}
	
			JMXInvoker invoker = JMXInvokerFactory.getJMXInvoker();

			// Check JBoss MBean life-cycle extension. Is it active?
			if (!invoker.isMBeanStarted(beanName)) {
				if (debug) {
					LOG.debug("MBean: " + beanName + " is not started. Skipping it.");
				}
				continue;
			}
			
			String attributes[] = ((String) getProperties().get("type.param.attributes")).split(",");
			
			String measurementUnit = getMeasurementUnit();
			for (int i=0; i<attributes.length; i++) {
				Object value = invoker.getAttributeValue(beanName, attributes[i]);
				if (debug) {
					LOG.debug("Saving: " + measurementUnit + ", " + name + ", " + attributes[i] + ", " + value);
				}
				values.put(measurementUnit, name, attributes[i], value);
			}
		}
	}
	
	public List<String> getMeasurableUnits() throws TypeHandlerException {
		List<String> answer = new ArrayList<String>();

		boolean debug = LOG.isDebugEnabled();
		
		// Get the JMX filter for finding the correct MBeans
		String filter = (String) getProperties().get("type.param.filter");
		if (debug) {
			LOG.debug("Found filter: " + filter);
		}
		if (filter == null) {
			throw new IllegalStateException("JMX filter parameter not found for " + getMeasurementUnit());
		}
				
		JMXInvoker invoker = JMXInvokerFactory.getJMXInvoker();

		Set<String> beans;
		try {
			beans = invoker.findJMXBeans(filter);
			if (debug) {
				LOG.debug("Found MBeans: " + beans);
			}
		} catch (MalformedObjectNameException e) {
			throw new TypeHandlerException(e);
		} catch (IOException e) {
			throw new TypeHandlerException(e);
		}

		// Get config data for more checks if the collected MBeans are to be used
		String checkClassName = (String) getProperties().get("type.param.checkClassName");
		String checkAttribute = (String) getProperties().get("type.param.checkAttribute");
		String checkAttributeRegExp = (String) getProperties().get("type.param.checkAttributeRegExp");		
		if (debug) {
			LOG.debug("Using setup: " + checkClassName + ", " + checkAttribute + ", " + checkAttributeRegExp);
		}

		// Perform the additional tests
		for (String bean : beans) {
			// Check JBoss MBean life-cycle extension. Is it active?			
			if (!invoker.isMBeanStarted(bean)) {
				if (debug) {
					LOG.debug("MBean: " + bean + " is not started. Skipping it.");
				}
				continue;
			}

			// Check impl. class		
			if (checkClassName != null) {
				if (!invoker.getImplementingClassName(bean).equals(checkClassName)) {
					if (debug) {
						LOG.debug("MBean: " + bean + " is not implementing: \"" + checkClassName + "\". Skipping it.");
					}
					continue;										
				}				
			}

			// Check attribute
			if (checkAttribute != null && checkAttributeRegExp != null) {
				if (invoker.attributeExists(bean, checkAttribute)) {
					if (!invoker.getAttributeValueAsString(bean, checkAttribute).matches(checkAttributeRegExp)) {
						if (debug) {
							LOG.debug("MBean: " + bean + " Attribute: \"" + checkAttribute + "\" is not matching value \"" + checkAttributeRegExp + "\"");
						}
						continue;					
					}
				} else {
					if (debug) {
						LOG.debug("MBean: " + bean + " Attribute: \"" + checkAttribute + "\" non existing.");
					}
					continue;					
				}
				if (debug) {
					LOG.debug("MBean: " + bean + " Attribute: \"" + checkAttribute + "\" matching value \"" + checkAttributeRegExp + "\"");
				}
			}
						
			// Get the name prefix
			String namePrefix = (String) getProperties().get("type.param.namePrefix");
			if (namePrefix == null) {
				namePrefix = "";
			}
			if (debug) {
				LOG.debug("Using namePrefix: " + namePrefix);
			}
			
			// Construct a unique name for the measurement unit
			
			// Name constructed from MBean attribute values
			String configNameSetting = (String) getProperties().get("type.param.nameAttributes");
			
			if (configNameSetting != null) {
				String nameParameters[] = configNameSetting.split(",");
				StringBuffer name = new StringBuffer();
				for (int i=0; i<nameParameters.length; i++) {					
					String partName = invoker.getAttributeValueAsString(bean, nameParameters[i]);
					if (name.length() > 0)
						name.append("__");
					name.append(partName);									
				}
				String fixedName = namePrefix + name;
				answer.add(fixedName);
				// Put the constructed name in the table
				nameToBeanNameTable.put(fixedName, bean);				
			}
			// Static name from the config
			configNameSetting = (String) getProperties().get("type.param.staticName");
			if (configNameSetting != null) {
				answer.add(namePrefix + configNameSetting);
				// Put the constructed name in the table				
				nameToBeanNameTable.put(namePrefix + configNameSetting, bean);
			}
			// Name constructed from MBean name values			
			configNameSetting = (String) getProperties().get("type.param.nameParameters");
			if (configNameSetting != null) {
				String nameParameters[] = configNameSetting.split(",");
				String parameters[] = bean.split(":")[1].split(",");
				StringBuffer nameParameter = new StringBuffer();
				for (int i=0; i<parameters.length; i++) {					
					for (int j=0; j<nameParameters.length; j++) {
						if (parameters[i].startsWith(nameParameters[j] + "=")) {
							if (nameParameter.length() > 0)
								nameParameter.append("__");
							nameParameter.append(parameters[i].split("=")[1]);
						}
					}					
				}
				String fixedName = namePrefix + nameParameter;
				answer.add(fixedName);
				// Put the constructed name in the table
				nameToBeanNameTable.put(fixedName, bean);				
			}
		}
		
		if (debug) {
			LOG.debug("Returning: " + answer);
		}
		return answer;
	}

}
