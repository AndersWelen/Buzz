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

import junit.framework.Assert;

import org.junit.Test;



/**
 * JUnit testcase
 * 
 * @author welle
 */
public class IntervalBasedTest {
	
	private static class TestIntervalImpl extends IntervalBased {

		public boolean called = false;
		
		/* (non-Javadoc)
		 * @see net.welen.buzz.protocols.IntervalBasedMBean#performWork()
		 */
		public void performWork() {
			called = true;
		}
			
	}

	@Test
	public void testSetSleepTime() {	
		TestIntervalImpl interval = new TestIntervalImpl();
		Long sleep = 1L;
		interval.setSleepTime(sleep);
		Assert.assertEquals(1L, interval.getSleepTime().longValue());
	}


	@Test
	public void testProtocolConfiguration() throws Exception {
		TestIntervalImpl interval = new TestIntervalImpl();
		interval.setEnabled(true);
		
		interval.setSleepTime(1000L);		
		Assert.assertFalse(interval.called);
		try {
			interval.start();			
			Assert.assertFalse(interval.called);
			Thread.sleep(2000);
			Assert.assertTrue(interval.called);
			interval.called = false;
			Thread.sleep(2000);
			Assert.assertTrue(interval.called);
		} finally {
			interval.stop();
		}
	}
	
}
