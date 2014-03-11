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
package net.welen.buzz.protocols;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.management.MalformedObjectNameException;

import junit.framework.Assert;

import net.welen.buzz.typehandler.AbstractTypeHandler;
import net.welen.buzz.typehandler.TypeHandler;
import net.welen.buzz.typehandler.TypeHandlerException;

import org.junit.Test;



/**
 * JUnit testcase
 * 
 * @author welle
 */
public class ProtocolTest {
	
	private static class TestTypeHander extends AbstractTypeHandler {

		public TestTypeHander(String category, String measurementUnit,
				Properties typeHandlerProperties) {
			super(category, measurementUnit, typeHandlerProperties);
		}

		public List<String> getMeasurableUnits() throws TypeHandlerException {
			ArrayList<String> answer = new ArrayList<String>();
			answer.add("MeasurementUnit");
			return answer;
		}

		public void getValues(BuzzAnswer values) throws TypeHandlerException {
			values.put("Category", "MeasurementUnit", "attribute1", "1");
			values.put("Category", "MeasurementUnit", "attribute2", "2");
			values.put("Category", "MeasurementUnit", "attribute3", "3");
			values.put("Category", "MeasurementUnit", "attribute4", "1");
		}
		
	}
	
	private static class TestProtocolImpl extends AbstractProtocol {

		@Override
		protected List<TypeHandler> getHandlers() {
			ArrayList<TypeHandler> list = new ArrayList<TypeHandler>();
			Properties prop = new Properties();
			prop.put("threshold.attribute1.warning", "2");
			prop.put("threshold.attribute2.warning", "100:100");
			prop.put("threshold.attribute3.warning", "2");
			prop.put("threshold.attribute3.critical", ":2");
			prop.put("threshold.attribute4.critical", ":1");
			prop.put("threshold.attribute4.warning", ":1");
			prop.put("threshold.attribute4.critical.MeasurementUnit", ":0");
			prop.put("threshold.attribute4.warning.MeasurementUnit", ":0");
			list.add(new TestTypeHander("Category", "MeasurementUnit", prop));
			return list;
		}

		public void getWarnings(BuzzAnswer input, BuzzAnswer output) {
			filterWarnings(input, output);
		}
		
		public void getAlarms(BuzzAnswer input, BuzzAnswer output) {
			filterAlarms(input, output);
		}

		public void startProtocol() throws Exception {
		}

		public void stopProtocol() throws Exception {
		}

	}

	@Test
	public void testProtocolConfiguration() throws MalformedObjectNameException, NullPointerException {
		TestProtocolImpl protocol = new TestProtocolImpl();
		String name = "Test:test=test";
		protocol.setConfigurationMBeanName(name);
		Assert.assertEquals(name, protocol.getConfigurationMBeanName());
	}

	@Test
	public void testGetEnabled() {
		TestProtocolImpl protocol = new TestProtocolImpl();
		Assert.assertFalse(protocol.getEnabled());
		protocol.setEnabled(true);
		Assert.assertTrue(protocol.getEnabled());
	}
	
	@Test
	public void testGetValues() {
		TestProtocolImpl protocol = new TestProtocolImpl();
		BuzzAnswer answer = new BuzzAnswer();
		protocol.getValues(answer);
		Assert.assertEquals(1, answer.size());
	}
	
	@Test
	public void testFilterWarnings() {
		TestProtocolImpl protocol = new TestProtocolImpl();
		BuzzAnswer input = new BuzzAnswer();
		BuzzAnswer output = new BuzzAnswer();
		protocol.getValues(input);
		
		protocol.getWarnings(input, output);
		Assert.assertEquals("Category=Category, Name=MeasurementUnit, Attribute=attribute4, Value=1\nCategory=Category, Name=MeasurementUnit, Attribute=attribute1, Value=1\nCategory=Category, Name=MeasurementUnit, Attribute=attribute2, Value=2\n", output.toString());
	}

	@Test
	public void testFilterAlarms() {
		TestProtocolImpl protocol = new TestProtocolImpl();
		BuzzAnswer input = new BuzzAnswer();
		BuzzAnswer output = new BuzzAnswer();
		protocol.getValues(input);
		
		protocol.getAlarms(input, output);
		Assert.assertEquals("Category=Category, Name=MeasurementUnit, Attribute=attribute4, Value=1\nCategory=Category, Name=MeasurementUnit, Attribute=attribute3, Value=3\n", output.toString());
	}

}
