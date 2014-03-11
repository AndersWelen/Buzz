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
package net.welen.buzz.protocols.nrpe;

import net.welen.buzz.protocols.BuzzAnswer;

import org.apache.log4j.Logger;

import it.jnrpe.ICommandLine;
import it.jnrpe.ReturnValue;
import it.jnrpe.net.IJNRPEConstants;
import it.jnrpe.plugins.IPluginInterface;
import it.jnrpe.utils.ThresholdUtil;

/**
 * @author welle
 *
 */
public class GetValueJNRPEPlugin implements IPluginInterface {

	private static final Logger LOG = Logger.getLogger(GetValueJNRPEPlugin.class);
	
	private NRPE nrpe;

	public GetValueJNRPEPlugin(NRPE nrpe) {
		this.nrpe = nrpe;
	}	

	/* (non-Javadoc)
	 * @see it.jnrpe.plugins.IPluginInterface#execute(it.jnrpe.ICommandLine)
	 */
	public ReturnValue execute(ICommandLine cl) {		
		String measurement = cl.getOptionValue("get");
		
		boolean debug = LOG.isDebugEnabled();
		
		// Clean up the incoming string
		if (debug) {
			LOG.debug("Measurement before cleanup: " + measurement);
		}
		measurement = measurement.replaceAll("\'$", "").replaceAll("^\'", "");
		if (debug) {
			LOG.debug("Measurement after cleanup: " + measurement);
		}

        String measurementSpec[] = measurement.split(BuzzAnswer.DELIMITER);

        // Illegal input
        if (measurementSpec.length != 3) {        	
        	LOG.error("Input not correct: " + measurement);
        	return new ReturnValue(IJNRPEConstants.STATE_UNKNOWN, "UNKNOWN - Input not correct: " + measurement);
        }
        
        String typeHandler = measurementSpec[0];
        String unit = measurementSpec[1];
        String key = measurementSpec[2];
        if (debug) {
        	LOG.debug("Typehandler: " + typeHandler + ", unit: " + unit + ", key: " + key);
        }

		NRPEAnswer answer = new NRPEAnswer();
		try {		
			String value;
			try {
				nrpe.getHandler(typeHandler).getValues(answer);				
				value = answer.getIndividualValue(typeHandler, unit, key).toString();
				if (debug) {
					LOG.debug("Value=" + value);
				}
				if (value == null) {
		        	return new ReturnValue(IJNRPEConstants.STATE_UNKNOWN, "UNKNOWN - Measurement " + measurement + " not found");						
				}
			} catch (NullPointerException e) {
				if (debug) {
					LOG.debug(e.getMessage(), e);
				}
				return new ReturnValue(IJNRPEConstants.STATE_UNKNOWN, "UNKNOWN - Measurement " + measurement + " not found");			
			}
			
			// Threshold checking
			String criticalThreshold = cl.getOptionValue("critical");			
			if (criticalThreshold != null && !criticalThreshold.equals("$ARG3$")) {
				int iValue = Integer.parseInt(value);
				if (ThresholdUtil.isValueInRange(criticalThreshold, iValue)) {
					if (debug) {
						LOG.debug("Value is critical");
					}
					return new ReturnValue(IJNRPEConstants.STATE_CRITICAL, "CRITICAL - " + measurement + "=" + value);
				}
			}

			String warningThreshold = cl.getOptionValue("warning");
			if (warningThreshold != null && !warningThreshold.equals("$ARG2$")) {
				int iValue = Integer.parseInt(value);
				if (ThresholdUtil.isValueInRange(warningThreshold, iValue)) {
					if (debug) {
						LOG.debug("Value is warning");
					}					
					return new ReturnValue(IJNRPEConstants.STATE_CRITICAL, "WARNING - " + measurement + "=" + value);
				}
			}
			
			if (debug) {
				LOG.debug("Returning value: " + value);
			}
			return new ReturnValue(IJNRPEConstants.STATE_OK, "OK - " + measurement + "=" + value);
		} catch (Throwable t) {
			LOG.error(t.getMessage(), t);
        	return new ReturnValue(IJNRPEConstants.STATE_UNKNOWN, "UNKNOWN - " + t.getMessage());			
		}		
	}

}
