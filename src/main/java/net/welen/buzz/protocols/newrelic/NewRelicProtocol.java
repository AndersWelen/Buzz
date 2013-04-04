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
package net.welen.buzz.protocols.newrelic;


import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.newrelic.api.agent.NewRelic;

import net.welen.buzz.protocols.BuzzAnswer;
import net.welen.buzz.protocols.IntervalBased;

/**
 * @author welle
 *
 */
public class NewRelicProtocol extends IntervalBased implements NewRelicProtocolBean {
	
	private static final Logger LOG = Logger.getLogger(NewRelicProtocol.class);

	public void startProtocol() throws Exception {
		super.startProtocol();
		LOG.info("Starting Buzz New Relic protocol.");
		
		// New Relic setup
		NewRelic.setTransactionName("Buzz", "/Buzz");
		//NewRelic.setRequestAndResponse(new Req, null);
	}

	public void stopProtocol() throws Exception {
		super.stopProtocol();
		LOG.info("Stopping Buzz New Relic protocol.");
	}

	public void performWork() {
		// Get values
		BuzzAnswer input = new BuzzAnswer();
		getValues(input);
				
		// Send values
		LOG.debug("Sending data to New Relic");
		
		NewRelic.recordMetric("AA", 10);
		NewRelic.recordMetric("A/B", 100);
		NewRelic.recordMetric("A/B/C", 1000);
		
		// Send errors
		LOG.debug("Sending errors to New Relic");
		// TODO Filter data
		Map<String, String> data = new HashMap<String, String>();
		data.put("Value1", "Data1");
		data.put("Value2", "Data2");		
		NewRelic.noticeError("Errors detected by Buzz. See attached parameters!", data);
	}
		
}
