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


import org.apache.log4j.Logger;

import com.newrelic.api.agent.NewRelic;

import net.welen.buzz.protocols.BuzzAnswer;
import net.welen.buzz.protocols.IntervalBased;
import net.welen.buzz.protocols.mail.MailAnswer;

/**
 * @author welle
 *
 */
public class NewRelicProtocol extends IntervalBased implements NewRelicProtocolBean {
	
	private static final Logger LOG = Logger.getLogger(NewRelicProtocol.class);

	public void performWork() {
		// Get values
		BuzzAnswer input = new BuzzAnswer();
		getValues(input);
		
		// NewRelic setup	
		NewRelic.ignoreTransaction();
		NewRelic.ignoreApdex();
		
		// Send values
		NewRelic.recordMetric("", 0);
		
		// Send warnings
		
		// Send alarms
	}
		
}
