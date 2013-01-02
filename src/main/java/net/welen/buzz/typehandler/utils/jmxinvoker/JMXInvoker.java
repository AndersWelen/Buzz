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
package net.welen.buzz.typehandler.utils.jmxinvoker;

import java.io.IOException;
import java.util.Set;

import javax.management.MalformedObjectNameException;

/**
 * @author welle
 *
 */
public interface JMXInvoker {

	/**
	 * Return a list of JMX Bean names
	 * 
	 * @param filter
	 * @return
	 */
	public Set<String> findJMXBeans(String filter) throws MalformedObjectNameException, IOException;

	/**
	 * Get the attribute value as a String
	 * 
	 * @param bean
	 * @param nameAttribute
	 * @return
	 */
	public String getAttributeValueAsString(String bean, String nameAttribute);

	/**
	 * Get the attribute value as an Object
	 * 
	 * @param bean
	 * @param nameAttribute
	 * @return
	 */
	public Object getAttributeValue(String bean, String nameAttribute);

	/**
	 * Execute the method
	 * 
	 * @param bean
	 * @param method
	 * @return
	 */
	public Object executeMethod(String bean, String method);

	/**
	 * Execute the method
	 * 
	 * @param bean
	 * @param method
	 * @param params
	 * @param signature
	 * @return
	 */
	public Object executeMethod(String bean, String method, Object[] params, String[] signature);

	/**
	 * If it's a JBoss JMX lifecycle extened MBean. Is it in "running" state?
	 * 
	 * @param bean
	 */
	public boolean isMBeanStarted(String bean);
	
	/**
	 * Check if a certain attribute exists
	 * 
	 * @param bean JMX bean name
	 * @param attribute Name of the attribute
	 */
	public boolean attributeExists(String bean, String attribute);

	/**
	 * Uses a reg.exp to find matching attributes
	 * 
	 * @param bean
	 * @param attribute
	 * @return
	 */
	public String[] findAttribute(String bean, String attributeRegExp);
	
	/**
	 * Get the implementation classname of the MBean
	 * 
	 * @param bean
	 * @return
	 */
	public String getImplementingClassName(String bean);
}
