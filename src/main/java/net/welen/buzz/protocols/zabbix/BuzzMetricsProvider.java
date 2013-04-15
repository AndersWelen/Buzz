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
package net.welen.buzz.protocols.zabbix;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.quigley.zabbixj.metrics.MetricsException;
import com.quigley.zabbixj.metrics.MetricsKey;
import com.quigley.zabbixj.metrics.MetricsProvider;

public class BuzzMetricsProvider implements MetricsProvider {
	private static final Logger LOG = Logger.getLogger(BuzzMetricsProvider.class);
	
	private Zabbix protocol = null;
	
	public BuzzMetricsProvider(Zabbix protocol) {
		this.protocol = protocol;
	}
	
	public Object getValue(MetricsKey metricKey) throws MetricsException {
		String key = metricKey.getKey();
		if (LOG.isDebugEnabled()) {
			LOG.debug("Key: " + key);
		}

		// Zabbix Discovery?
		if (key.equals("discovery")) {
			try {
				return getZabbixDiscovery();
			} catch (JSONException e) {
				LOG.error("Couldn't get Zabbix discovery", e);
				throw new MetricsException(e);
			}
		}

		if (!key.equals("fetch")) {
			LOG.error("Incorrect key: " + key);
			throw new MetricsException("Incorrect key: " + key);			
		}

		// Get Data
		String path = metricKey.getParameters()[0];
		String paths[] = path.split("/");
		if (paths.length != 3) {
			LOG.error("Incorrect path: " + path);
			throw new MetricsException("Incorrect path: " + path);			
		}
		Object answer = protocol.getIndividualValue(paths[0], paths[1], paths[2]);
		if (answer == null) {
			LOG.warn("Path not found: " + path);
			throw new MetricsException("Key not found: " + path);						
		}
		
		if (LOG.isDebugEnabled()) {
			LOG.debug("Returning \"" + answer.toString() + "\" for: " + key);
		}
		return answer;
	}

	private String getZabbixDiscovery() throws JSONException {							
		JSONObject jsonObject = new JSONObject();
		JSONArray jsonArray = new JSONArray();
		
		// Get all measurements
		String paths[] = protocol.getAllPaths();
		for (String s: paths) {
			JSONObject tmpObject = new JSONObject();
			tmpObject.put("{#BUZZPATH}", s);
			String parts[] = s.split("/");
			tmpObject.put("{#BUZZCATEGORY}", parts[0]);
			tmpObject.put("{#BUZZNAME}", parts[1]);
			tmpObject.put("{#BUZZKEY}", parts[2]);
			// TODO
			//tmpObject.put("{#BUZZDESCRIPTION}", "The number of threads");
			//tmpObject.put("{#BUZZUNIT}", "Count");
			jsonArray.put(tmpObject);
		}						
		jsonObject.put("data", jsonArray);
		
		String answer = jsonObject.toString();

		if (LOG.isDebugEnabled()) {
			LOG.debug("Returning Zabbix discovery; " + answer);
		}
		return answer;
	}
	
}
