package net.welen.buzz.protocols.zabbix;

import org.apache.log4j.Logger;

import com.quigley.zabbixj.metrics.MetricsException;
import com.quigley.zabbixj.metrics.MetricsKey;
import com.quigley.zabbixj.metrics.MetricsProvider;

public class BuzzMetricsProvider implements MetricsProvider {
	private static final Logger LOG = Logger.getLogger(BuzzMetricsProvider.class);
	
	public Object getValue(MetricsKey key) throws MetricsException {		
		LOG.debug("Key: " + key.getKey());
        
		// TODO
		throw new MetricsException("Not supported key: " + key.getKey());
		/*
		Object answer = new Integer(42);		
		
		LOG.debug("Returning value for key (" + key.getKey() + "): " + answer);
		return answer;
		*/
	}

}
