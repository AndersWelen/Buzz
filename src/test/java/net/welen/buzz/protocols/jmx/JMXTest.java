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

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import net.welen.buzz.protocols.BuzzAnswer;
import net.welen.buzz.typehandler.AbstractTypeHandler;
import net.welen.buzz.typehandler.TypeHandler;
import net.welen.buzz.typehandler.TypeHandlerException;

import org.junit.Assert;
import org.junit.Test;

/**
 * JUnit testcase
 * 
 * @author welle
 */
public class JMXTest {
	
	private class JMXTestProtocol extends JMX {

		TypeHandler handler = null;
		
		@Override
		protected List<TypeHandler> getHandlers() {
			List<TypeHandler> answer = new ArrayList<TypeHandler>();
			
			if (handler != null) {
				answer.add(handler);				
			}
			return answer;
		}

		@Override
		protected TypeHandler getHandler(String name) {		
			if (handler != null) {
				return handler;
			}
			return null;
		}
	}
	
	private class TestHandler extends AbstractTypeHandler {

		public TestHandler(String category, String measurementUnit, Properties typeHandlerProperties) {
			super(category, measurementUnit, typeHandlerProperties);
		}

		public List<String> getMeasurableUnits() throws TypeHandlerException {
			List<String> answer = new ArrayList<String>();
			answer.add("measurementUnit");
			return answer;
		}

		public void getValues(BuzzAnswer values) throws TypeHandlerException {			
			values.put("category", "measurementUnit", "attribute", "1");
		}

	}

	
	@Test
	public void testDefaultCacheTime() {
		Assert.assertEquals(10000L, (new JMX()).getTimeToCache().longValue());
	}

	@Test
	public void testSetCacheTime() {
		JMX test = new JMX();
		
		Long value = new Long(1);
		test.setTimeToCache(value);
		Assert.assertEquals(value.longValue(), test.getTimeToCache().longValue());
	}

	@Test
	public void testFetchValues() {
		JMXTestProtocol test = new JMXTestProtocol();
		
		test.setTimeToCache(0L);
		Assert.assertTrue(test.fetchValues().isEmpty());	

		test.handler = new TestHandler("category", "measurementUnit", new Properties());
		Assert.assertEquals(1, test.fetchValues().size());
	}

	@Test
	public void testFetchIndividualValue() throws TypeHandlerException {
		JMXTestProtocol test = new JMXTestProtocol();		
		test.handler = new TestHandler("category", "measurementUnit", new Properties());
		
		Assert.assertEquals("1", test.fetchIndividualValue("category", "measurementUnit", "attribute"));		
		Assert.assertNull(test.fetchIndividualValue("category", "measurementUnit", "NonExistingAttribute"));
		Assert.assertNull(test.fetchIndividualValue("category", "NonExistingMeasurementUnit", "attribute"));
		Assert.assertNull(test.fetchIndividualValue("NonExistingCategory", "measurementUnit", "attribute"));
	}

	@Test
	public void testCachingInFetchValues() throws InterruptedException {
		JMXTestProtocol test = new JMXTestProtocol();
		
		test.setTimeToCache(2000L);
		test.handler = new TestHandler("category", "measurementUnit", new Properties());
			
		Assert.assertEquals(1, test.fetchValues().size());
		
		test.handler = null;
		
		Thread.sleep(500L);		
		Assert.assertEquals(1, test.fetchValues().size());

		Thread.sleep(2000L);
		Assert.assertTrue(test.fetchValues().isEmpty());
	}

	@Test
	public void testFetchAlarms() {
		JMXTestProtocol test = new JMXTestProtocol();
		
		Assert.assertTrue(test.fetchAlarms().isEmpty());

		Properties prop = new Properties();
		prop.setProperty("threshold.attribute.critical", ":0");
		test.handler = new TestHandler("category", "measurementUnit", prop);				
		Assert.assertEquals(1, test.fetchAlarms().size());					
		
		prop.setProperty("threshold.attribute.critical", ":1");
		test.handler = new TestHandler("category", "measurementUnit", prop);
		Assert.assertEquals(0, test.fetchAlarms().size());							
	}

	@Test
	public void testFetchWarnings() {
		JMXTestProtocol test = new JMXTestProtocol();
		
		Assert.assertTrue(test.fetchWarnings().isEmpty());

		Properties prop = new Properties();
		prop.setProperty("threshold.attribute.warning", ":0");
		test.handler = new TestHandler("category", "measurementUnit", prop);						
		Assert.assertEquals(1, test.fetchWarnings().size());					
		
		prop.setProperty("threshold.attribute.warning", ":1");
		test.handler = new TestHandler("category", "measurementUnit", prop);
		Assert.assertEquals(0, test.fetchWarnings().size());							
	}

}
