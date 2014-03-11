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
package net.welen.buzz.protocols.log4j;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import net.welen.buzz.protocols.IntervalBased;

/**
 * @author welle
 *
 */
public class Log4j extends IntervalBased implements Log4jMBean {
	
	private static final Logger LOG = Logger.getLogger(Log4j.class);
	
	private Boolean logValues = true;
	private Boolean logWarnings = true;
	private Boolean logAlarms = true;
	private String logPrefix = "Message from Buzz: ";
	
	/* (non-Javadoc)
	 * @see net.welen.buzz.protocols.IntervalBasedMBean#performWork()
	 */
	public void performWork() {
		Log4jAnswer input = new Log4jAnswer();
		Log4jAnswer answer = new Log4jAnswer();
		getValues(input);

		// Measurements
		if (logValues) {
			performLog(Level.INFO, input);
		}

		// Warnings
		if (logWarnings) {
			filterWarnings(input, answer);	
			performLog(Level.WARN, answer);
		}
		
		// Alarms
		if (logAlarms) {
			answer = new Log4jAnswer();
			filterAlarms(input, answer);
			performLog(Level.ERROR, answer);
		}
	}
	
	// Perform the logging
	private void performLog(Level logLevel, Log4jAnswer answer) {
		for (String line : answer.getLogLines()) {
			LOG.log(logLevel, logPrefix + line);
		}
	}	

	/* (non-Javadoc)
	 * @see net.welen.buzz.protocols.log4j.Log4jMBean#setLogValues(boolean)
	 */
	public void setLogValues(Boolean value) {
		logValues = value;
	}

	/* (non-Javadoc)
	 * @see net.welen.buzz.protocols.log4j.Log4jMBean#getLogValues()
	 */
	public Boolean getLogValues() {
		return logValues;
	}

	/* (non-Javadoc)
	 * @see net.welen.buzz.protocols.log4j.Log4jMBean#setLogWarnings(boolean)
	 */
	public void setLogWarnings(Boolean value) {
		logWarnings = value;
	}

	/* (non-Javadoc)
	 * @see net.welen.buzz.protocols.log4j.Log4jMBean#getLogWarnings()
	 */
	public Boolean getLogWarnings() {
		return logWarnings;
	}

	/* (non-Javadoc)
	 * @see net.welen.buzz.protocols.log4j.Log4jMBean#setLogAlarms(boolean)
	 */
	public void setLogAlarms(Boolean value) {
		logAlarms = value;
	}

	/* (non-Javadoc)
	 * @see net.welen.buzz.protocols.log4j.Log4jMBean#getLogAlarms()
	 */
	public Boolean getLogAlarms() {
		return logAlarms;
	}

	/* (non-Javadoc)
	 * @see net.welen.buzz.protocols.log4j.Log4jMBean#getLogPrefix()
	 */
	public String getLogPrefix() {
		return logPrefix;
	}

	/* (non-Javadoc)
	 * @see net.welen.buzz.protocols.log4j.Log4jMBean#setLogPrefix(java.lang.String)
	 */
	public void setLogPrefix(String logPrefix) {
		this.logPrefix = logPrefix;
	}

}
