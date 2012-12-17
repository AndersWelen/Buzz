/*
 * Buzz - a monitoring framework for JBoss
 *
 * Copyright 2012 Anders Welén, anders@welen.net
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
package net.welen.buzz.protocols.jmx;

import java.util.Map;
import java.util.Map.Entry;

import net.welen.buzz.protocols.BuzzAnswer;
import net.welen.buzz.protocols.AbstractProtocol;
import net.welen.buzz.typehandler.TypeHandlerException;
import net.welen.buzz.utils.TreeNode;

import org.apache.log4j.Logger;

/**
 * This MBean has everything impl. as attributes instead of methods
 * as it seems like many monitoring tools just support JMX attributes.
 * As attributes often are displayed frequent a simple cache is impl.
 * to stop generating a lot of unneeded calls to the core.
 * 
 * @author welle
 *
 */
public class JMX extends AbstractProtocol implements JMXMBean {
	
	private static final Logger LOG = Logger.getLogger(JMX.class);
	
	private Long cacheTime = 10000L;		// Default cache value for 10 sec
	private long lastFetch = 0;
	private TreeNode cachedPayload = null;
	
	/* (non-Javadoc)
	 * @see net.welen.buzz.protocols.jmx.JMXMBean#getTimeToCache()
	 */
	public Long getTimeToCache() {
		return cacheTime;
	}

	/* (non-Javadoc)
	 * @see net.welen.buzz.protocols.jmx.JMXMBean#setTimeToCache(long)
	 */
	public void setTimeToCache(Long ms) {
		cacheTime = ms;
	}
	
	protected void getValues(JMXAnswer answer) {
		if (cachedPayload != null 
				&& ((lastFetch + cacheTime) > System.currentTimeMillis())) {			
			LOG.debug("Returning cached payload");
			answer.setPayload(cachedPayload);			
			return;
		}
		super.getValues(answer);
		lastFetch = System.currentTimeMillis();
		if (LOG.isDebugEnabled()) {
			LOG.debug("Returning: " + answer.getPayload());
		}
		cachedPayload = answer.getPayload();
	}

	/* (non-Javadoc)
	 * @see net.welen.buzz.protocols.jmx.JMXMBean#getValues()
	 */
	public Map<String, Object> fetchValues() {
		JMXAnswer answer = new JMXAnswer();
		getValues(answer);
		return answer.getMap();
	}

	/* (non-Javadoc)
	 * @see net.welen.buzz.protocols.jmx.JMXMBean#fetchValuesAsString()
	 */
	public String fetchValuesAsString() {		
		return mapToString(fetchValues());
	}

	/* (non-Javadoc)
	 * @see net.welen.buzz.protocols.jmx.JMXMBean#getAlarms()
	 */
	public Map<String, Object> fetchAlarms() {
		BuzzAnswer answer = new BuzzAnswer();
		JMXAnswer alarms = new JMXAnswer();
		getValues(answer);
		filterAlarms(answer, alarms);
		return alarms.getMap();
	}

	/* (non-Javadoc)
	 * @see net.welen.buzz.protocols.jmx.JMXMBean#fetchAlarmsAsString()
	 */
	public String fetchAlarmsAsString() {
		return mapToString(fetchAlarms());
	}

	/* (non-Javadoc)
	 * @see net.welen.buzz.protocols.jmx.JMXMBean#fetchWarnings()
	 */
	public Map<String, Object> fetchWarnings() {
		BuzzAnswer answer = new BuzzAnswer();
		JMXAnswer warnings = new JMXAnswer();
		getValues(answer);
		filterWarnings(answer, warnings);
		return warnings.getMap();
	}

	/* (non-Javadoc)
	 * @see net.welen.buzz.protocols.jmx.JMXMBean#fetchWarningsAsString()
	 */
	public String fetchWarningsAsString() {
		return mapToString(fetchWarnings());
	}
	
	private String mapToString(Map<String, Object> map) {
		StringBuffer answer = new StringBuffer();
		
		for (Entry<String, Object> entry : map.entrySet()) {
			answer.append(entry.getKey() + "=" + entry.getValue() + "\n");
		}
		
		return answer.toString();		
	}

	/* (non-Javadoc)
	 * @see net.welen.buzz.protocols.jmx.JMXMBean#fetchIndividualValue(java.lang.String, java.lang.String, java.lang.String)
	 */
	public Object fetchIndividualValue(String typeHandler, String unit, String key) throws TypeHandlerException {		
		JMXAnswer answer = new JMXAnswer();
		getHandler(typeHandler).getValues(answer);
		
		return answer.getIndividualValue(typeHandler, unit, key);
	}

	/* (non-Javadoc)
	 * @see net.welen.buzz.protocols.Protocol#fetchProtocol()
	 */
	public void startProtocol() throws Exception {
		// Nothing to start
	}

	/* (non-Javadoc)
	 * @see net.welen.buzz.protocols.Protocol#stopProtocol()
	 */
	public void stopProtocol() throws Exception {
		// Nothing to stop
	}

}
