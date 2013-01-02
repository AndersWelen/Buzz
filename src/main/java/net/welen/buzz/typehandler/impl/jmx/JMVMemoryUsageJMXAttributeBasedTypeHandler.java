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
package net.welen.buzz.typehandler.impl.jmx;

import java.util.Properties;

import javax.management.openmbean.CompositeDataSupport;

import net.welen.buzz.protocols.BuzzAnswer;
import net.welen.buzz.typehandler.TypeHandlerException;

import org.apache.log4j.Logger;


/**
 * Handler for the JVM JMX Memory attributes
 * 
 * @author welle
 */
public class JMVMemoryUsageJMXAttributeBasedTypeHandler extends JMXAttributeBasedTypeHandler {

	private static final Logger LOG = Logger.getLogger(JMVMemoryUsageJMXAttributeBasedTypeHandler.class);
		
	/**
	 * @param params
	 */
	public JMVMemoryUsageJMXAttributeBasedTypeHandler(String category, String measurementUnit, Properties typeHandlerProperties) {
		super(category, measurementUnit, typeHandlerProperties);
	}

	public void getValues(BuzzAnswer values) throws TypeHandlerException {
		super.getValues(values);
		
		String category = getCategory();
		String key = getProperties().getProperty("type.param.attributes");
			
		for (String name : getMeasurableUnits()) {
			CompositeDataSupport cds = (CompositeDataSupport) values.getIndividualValue(category, name, key);
			if (LOG.isDebugEnabled()) {
				LOG.debug("Extracted: " + cds.toString());
			}
			
			Object init = cds.get("init");
			Object used = cds.get("used");
			Object committed = cds.get("committed");
			Object max = cds.get("max");
		
			if (LOG.isDebugEnabled()) {
				LOG.debug("init=" + init + ", used=" + used + ", committed=" + committed + ", max=" + max);
			}
			
			// Update the answer
			values.remove(category, name, key);
			values.put(category, name, "init", init);
			values.put(category, name, "used", used);
			values.put(category, name, "committed", committed);
			values.put(category, name, "max", max);
		}
	}
	
}
