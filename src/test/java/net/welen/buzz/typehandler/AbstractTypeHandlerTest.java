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
package net.welen.buzz.typehandler;

import java.util.List;
import java.util.Properties;

import junit.framework.Assert;

import net.welen.buzz.protocols.BuzzAnswer;
import net.welen.buzz.typehandler.AbstractTypeHandler;
import net.welen.buzz.typehandler.TypeHandlerException;

import org.junit.Test;


public class AbstractTypeHandlerTest {
	
	private static class TestTypeHandler extends AbstractTypeHandler {

		public TestTypeHandler(String category, String measurementUnit, Properties typeHandlerProperties) {
			super(category, measurementUnit, typeHandlerProperties);
		}

		public List<String> getMeasurableUnits() throws TypeHandlerException {
			return null;
		}

		public void getValues(BuzzAnswer values) throws TypeHandlerException {			
		}
		
	}
	
	@Test
	public void testHandler() throws TypeHandlerException {
		Properties props = new Properties();
		props.put("TestKey", "TestValue");
		TestTypeHandler handler = new TestTypeHandler("TestCategory", "TestMeasurementUnit", props);
		
		Assert.assertEquals("TestCategory", handler.getCategory());
		Assert.assertEquals("TestMeasurementUnit", handler.getMeasurementUnit());		
		Assert.assertEquals(props, handler.getProperties());
		Assert.assertTrue(handler.toString().endsWith("TestMeasurementUnit"));
	}
	
}