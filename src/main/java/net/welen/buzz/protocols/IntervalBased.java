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
package net.welen.buzz.protocols;

import org.apache.log4j.Logger;

/**
 * @author welle
 *
 */
public abstract class IntervalBased extends AbstractProtocol implements IntervalBasedMBean, Runnable {
	
	private static final Logger LOG = Logger.getLogger(IntervalBased.class);
	
	private Long sleepTime = 60000L;  // Default 60 sec	
	private boolean stopped = false;

	/* (non-Javadoc)
	 * @see net.welen.buzz.protocols.IntervalBasedMBean#getSleepTime()
	 */
	public Long getSleepTime() {
		return sleepTime;
	}

	/* (non-Javadoc)
	 * @see net.welen.buzz.protocols.IntervalBasedMBean#setSleepTime(long)
	 */
	public void setSleepTime(Long ms) {
		sleepTime = ms;
	}

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		while (!stopped) {
			try {
				if (LOG.isDebugEnabled()) {
					LOG.debug("Sleeping for " + sleepTime + " ms");
				}
				Thread.sleep(sleepTime);
			} catch (InterruptedException e) {
				LOG.error(e.getMessage(), e);
			}
			LOG.debug("Waking up");			
			if (!stopped) {
				try {
					LOG.debug("Calling performWork()");
					performWork();
				} catch (Throwable t) {
					LOG.error(t.getMessage(), t);
				}
			}
		}
	}

	public void startProtocol() throws Exception {
		stopped = false;		
		LOG.debug("Starting the intervall thread");
		new Thread(this, "Buzz Intervall thread " + this.hashCode()).start();
	}

	public void stopProtocol() throws Exception {
		LOG.debug("Stopping the intervall thread");
		stopped = true;
	}

}

