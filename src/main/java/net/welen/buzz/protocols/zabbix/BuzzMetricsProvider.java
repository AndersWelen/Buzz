package net.welen.buzz.protocols.zabbix;

import org.apache.log4j.Logger;

import com.quigley.zabbixj.metrics.MetricsException;
import com.quigley.zabbixj.metrics.MetricsKey;
import com.quigley.zabbixj.metrics.MetricsProvider;

public class BuzzMetricsProvider implements MetricsProvider {
	private static final Logger LOG = Logger.getLogger(BuzzMetricsProvider.class);
	
	private Zabbix protocol = null;
	
	public BuzzMetricsProvider(Zabbix protocol) {
		this.protocol = protocol;
	}
	
	public Object getValue(MetricsKey key) throws MetricsException {		
		LOG.debug("Key: " + key.getKey());
        
		// Get Data
		String keys[] = key.getKey().split("\\.");
		if (keys.length != 3) {
			LOG.error("Incorrect key: " + key.getKey());
			throw new MetricsException("Incorrect key: " + key.getKey());			
		}		
		Object answer = protocol.getIndividualValue(keys[0], keys[1], keys[2]);
		if (answer == null) {
			LOG.warn("Key not found: " + key.getKey());
			throw new MetricsException("Key not found: " + key.getKey());						
		}
		
		LOG.debug("Returning \"" + answer.toString() + "\" for: " + key.getKey());		
		return answer;
	}
	
}
