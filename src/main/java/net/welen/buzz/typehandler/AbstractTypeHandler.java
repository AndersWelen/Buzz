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

import java.util.Properties;

/**
 * Base for handlers
 * 
 * @author welle
 */
public abstract class AbstractTypeHandler implements TypeHandler {

	private String category = null;
	private String measurementUnit = null;
	private Properties typeHandlerProperties = null;	
	
	/**
	 * Constructor
	 * 
	 * @param The subset of properties from the Munin configuration for this measurement
	 */
	public AbstractTypeHandler(String category, String measurementUnit, Properties typeHandlerProperties) {
		this.category = category;
		this.measurementUnit = measurementUnit;		
		this.typeHandlerProperties = typeHandlerProperties;		
	}

	
	/* (non-Javadoc)
	 * @see net.welen.buzz.typehandler.TypeHandler#getCategory()
	 */
	public String getCategory() {
		return category;
	}


	/* (non-Javadoc)
	 * @see net.welen.buzz.typehandler.TypeHandler#getMeasurementUnit()
	 */
	public String getMeasurementUnit() throws TypeHandlerException {
		return measurementUnit;
	}

			
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return super.toString() + ":" + measurementUnit;
	}

	/* (non-Javadoc)
	 * @see net.welen.buzz.typehandler.TypeHandler#getProperties()
	 */
	public Properties getProperties() {
		return typeHandlerProperties;
	}

}
