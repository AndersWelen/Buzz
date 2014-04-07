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
package net.welen.buzz.protocols.zabbix;

import java.util.Properties;

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
			} catch (Throwable t) {
				LOG.error("Couldn't get Zabbix discovery", t);
				throw new MetricsException(t);	
			}
		}
		
		// Zabbix ping
		if (key.equals("ping")) {
			return 1;
		}
		
		// Zabbix fetching values?		
		if (key.equals("fetch")) {
			return getZabbixFetch(metricKey);
		}
		
		LOG.error("Unknown key: " + key);
		throw new MetricsException("Unknown key: " + key);					
	}
	
	private String getZabbixFetch(MetricsKey metricKey) {							
		String path = metricKey.getParameters()[0];
		String paths[] = path.split("/");
		if (paths.length != 3) {
			LOG.error("Incorrect path: " + path);
			throw new MetricsException("Incorrect path: " + path);			
		}
		Object answer = protocol.getIndividualValue(paths[0], paths[1], paths[2]);
		if (answer == null) {
			// This may happen for a legal value as Buzz may be starting before the other service
			LOG.warn("Path not found: " + path);
			// If a MetricsException is thrown the values will be marked as "Not supported by Zabbix Agent" in Zabbix
			// and not tries again so we return an empty string instead.
			return "";
		}
		
		if (LOG.isDebugEnabled()) {
			LOG.debug("Returning \"" + answer.toString() + "\" for: " + metricKey.getKey());
		}
		return answer.toString();
	}
	
	private String getZabbixDiscovery() throws JSONException {							
		JSONObject jsonObject = new JSONObject();
		JSONArray jsonArray = new JSONArray();
		
		// Get all measurements
		String paths[] = protocol.getAllPaths();
		for (String path: paths) {
			JSONObject tmpObject = new JSONObject();
			tmpObject.put("{#BUZZPATH}", path);
			String parts[] = path.split("/");
			tmpObject.put("{#BUZZCATEGORY}", parts[0]);
			tmpObject.put("{#BUZZNAME}", parts[1]);
			tmpObject.put("{#BUZZKEY}", parts[2]);

			Properties setup = protocol.getTypeHandler(parts[0]).getProperties();
			// TODO Should not reference Munin setup
			// This is not used until Zabbix can pick it up in the template
			tmpObject.put("{#BUZZUNIT}", setup.get("protocol.munin.graph_vlabel"));
			tmpObject.put("{#BUZZTITLE}", ((String) setup.get("protocol.munin.graph_title")).replaceAll("%n", parts[1]));
			tmpObject.put("{#BUZZLABEL}", setup.get("protocol.munin." + parts[2] + ".label"));
					
			String warnThreshold = (String) setup.get("threshold." + parts[2] + ".warning");
			if (warnThreshold != null) {				
				String warnLevels[] = warnThreshold.split(":");
				if (warnLevels[0].length() > 0) {
					tmpObject.put("{#BUZZWARNLOW}", warnLevels[0]);
				}
				if (warnLevels.length > 1 && warnLevels[1].length() > 0) {
					tmpObject.put("{#BUZZWARNHIGH}", warnLevels[1]);
				}
			}		
			String criticalThreshold = (String) setup.get("threshold." + parts[2] + ".critical");
			if (criticalThreshold != null) {
				String criticalLevels[] = criticalThreshold.split(":");
				if (criticalLevels[0].length() > 0) {
					tmpObject.put("{#BUZZCRITICALLOW}", criticalLevels[0]);
				}
				if (criticalLevels.length > 1 && criticalLevels[1].length() > 0) {
					tmpObject.put("{#BUZZCRITICALHIGH}", criticalLevels[1]);
				}				
			}		
			
			jsonArray.put(tmpObject);
		}						
		jsonObject.put("data", jsonArray);
		
		String answer = jsonObject.toString();

		if (LOG.isDebugEnabled()) {
			LOG.debug("Returning Zabbix discovery: " + answer);
		}
		return answer;
	}
	
}
