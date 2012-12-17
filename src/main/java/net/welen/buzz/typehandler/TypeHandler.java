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

import net.welen.buzz.protocols.BuzzAnswer;


/**
 * Interface describing a handler
 * 
 * @author welle
 */
public interface TypeHandler {

	/**
	 * Get the category that this TypeHandler is connected to
	 * in the config file.
	 * 
	 * @return
	 */
	public String getCategory();
	
	/**
	 * Get all measurable units that matches this TypeHandler
	 * <P>
	 * For example a list of all Database connection pools found.
	 *  
	 * @return
	 */
	public List<String> getMeasurableUnits() throws TypeHandlerException;
	
	/**
	 * Get the MeasurementUnit "tied" to this instance
	 * 
	 * @return
	 * @throws TypeHandlerException
	 */
	public String getMeasurementUnit() throws TypeHandlerException;

	/**
	 * Gather the values and put it in the provided BuzzAnswer object.
	 * 
	 * @return
	 */
	public void getValues(BuzzAnswer values) throws TypeHandlerException;	

	/**
	 * Get the setup properties for this typehandler
	 * 
	 * @return
	 */
	public Properties getProperties();
}
