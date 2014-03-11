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
package net.welen.buzz.typehandler.utils.jmxinvoker;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.MBeanServerConnection;
import javax.management.MBeanServerFactory;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;

import org.apache.log4j.Logger;
import org.jboss.system.ServiceMBean;


/**
 * @author welle
 *
 */
public class JMXInvokerImpl implements JMXInvoker {

	private static final Logger LOG = Logger.getLogger(JMXInvokerImpl.class);
	
	private static volatile MBeanServerConnection mBeanServer = null;
	private static Object lockObject = new Object();
			
	// Lazy initialization of the thread-safe MBeanServerConnection
	private static MBeanServerConnection getMBeanServerConnection() {
		if (mBeanServer == null) {
			synchronized (lockObject) {
				if (mBeanServer == null) {
					// This doesn't work in JBoss 7
					// mBeanServer = org.jboss.mx.util.MBeanServerLocator.locateJBoss();
					// So we perform the MBean standard way of doing it
					mBeanServer = (MBeanServer) MBeanServerFactory.findMBeanServer(null).get(0);
					
					if (LOG.isDebugEnabled()) {
						LOG.debug("Found MBean server: "+ mBeanServer);
					}
				}
			}		
		}
		if (LOG.isDebugEnabled()) {
			LOG.debug("Returning MBean server: "+ mBeanServer);
		}
		return mBeanServer;
	}
	
	public Set<String> findJMXBeans(String filter) throws MalformedObjectNameException, IOException {		
		MBeanServerConnection server = getMBeanServerConnection();
		Set<String> answer = new HashSet<String>();
		
		String filters[] = filter.split("\\|");			
		for (int i=0; i<filters.length; i++) {
			for (ObjectName item : server.queryNames(new ObjectName(filters[i]), null)) {
				answer.add(item.toString());
			}
		}
		
		if (LOG.isDebugEnabled()) {
			LOG.debug("The filter: " + filter + " matches the following MBeans: " + answer);
		}
		return answer;
	}
	
	public Object getAttributeValue(String bean, String attribute) {		
		MBeanServerConnection server = getMBeanServerConnection();
		
		try {
			Object value = server.getAttribute(new ObjectName(bean), attribute);
			if (LOG.isDebugEnabled()) {				 
				LOG.debug("The bean: " + bean + " attribute: " + attribute + " has the value: " + value);
			}
			return value;
		} catch (IOException e) {
			throw new RuntimeException("Failed to get attribute: " + attribute + ", for MBean: " + bean, e);
		} catch (AttributeNotFoundException e) {
			throw new RuntimeException("Failed to get attribute: " + attribute + ", for MBean: " + bean, e);
		} catch (InstanceNotFoundException e) {
			throw new RuntimeException("Failed to get attribute: " + attribute + ", for MBean: " + bean, e);
		} catch (MalformedObjectNameException e) {
			throw new RuntimeException("Failed to get attribute: " + attribute + ", for MBean: " + bean, e);
		} catch (MBeanException e) {
			throw new RuntimeException("Failed to get attribute: " + attribute + ", for MBean: " + bean, e);
		} catch (ReflectionException e) {
			throw new RuntimeException("Failed to get attribute: " + attribute + ", for MBean: " + bean, e);
		} catch (NullPointerException e) {
			throw new RuntimeException("Failed to get attribute: " + attribute + ", for MBean: " + bean, e);
		}
	}

	public String getAttributeValueAsString(String bean, String attribute) {		
		Object o = getAttributeValue(bean, attribute);
		String answer;
		if (o != null) {												
			
			answer = o.toString();
			
			// These extreme values are often used in
			// max/min execution time values. 
			if (answer.equals("" + Long.MIN_VALUE)) {
				answer = "";
			}
			if (answer.equals("" + Long.MAX_VALUE)) {			
				answer = "";
			}
		} else {
			answer = "";
		}
		
		if (LOG.isDebugEnabled()) {				 
			LOG.debug("The bean: " + bean + " attribute: " + attribute + " has the string value: " + answer);
		}
		return answer;
	}

	public Object executeMethod(String bean, String method) {
		return executeMethod(bean, method, null, null);
	}
	
	public Object executeMethod(String bean, String method, Object[] params, String[] signature) {
		MBeanServerConnection server = getMBeanServerConnection();
		try {
			Object value = server.invoke(new ObjectName(bean), method, params, signature);
			if (LOG.isDebugEnabled()) {							
				LOG.debug("The bean: " + bean + " method: " + method + " params=" + Arrays.toString(params) + " signature=" + Arrays.toString(signature) + " returned: " + value);
			}
			return value;
		} catch (IOException e) {
			throw new RuntimeException("Failed to invoke method: " + method + ", for MBean: " + bean, e);
		} catch (InstanceNotFoundException e) {
			throw new RuntimeException("Failed to invoke method: " + method + ", for MBean: " + bean, e);
		} catch (MalformedObjectNameException e) {
			throw new RuntimeException("Failed to invoke method: " + method + ", for MBean: " + bean, e);
		} catch (MBeanException e) {
			throw new RuntimeException("Failed to invoke method: " + method + ", for MBean: " + bean, e);
		} catch (ReflectionException e) {
			throw new RuntimeException("Failed to invoke method: " + method + ", for MBean: " + bean, e);
		} catch (NullPointerException e) {
			throw new RuntimeException("Failed to invoke method: " + method + ", for MBean: " + bean, e);
		}
	}

	public boolean isMBeanStarted(	String bean) {
		MBeanServerConnection server = getMBeanServerConnection();
		
		boolean debug = LOG.isDebugEnabled();
		
		try {
			// If not a JBoss life-cycle extended MBean, It's "Started"!
			if (!server.isInstanceOf(new ObjectName(bean), "org.jboss.system.ServiceMBean")) {
				if (debug) {
					LOG.debug("MBean: " + bean + " is not an org.jboss.system.ServiceMBean. Returning true");
				}
				return true;
			}						
		} catch (InstanceNotFoundException e) {
			throw new RuntimeException("Failed to check MBean: " + bean, e);
		} catch (MalformedObjectNameException e) {
			throw new RuntimeException("Failed to check MBean: " + bean, e);		
		} catch (NullPointerException e) {
			throw new RuntimeException("Failed to check MBean: " + bean, e);
		} catch (IOException e) {
			throw new RuntimeException("Failed to check MBean: " + bean, e);
		}

		// Check state
		try {
			Integer state = (Integer) getAttributeValue(bean, "State");		
			if (state == ServiceMBean.STARTED) {
				if (debug) {
					LOG.debug("MBean: " + bean + " is started. Returning true");
				}
				return true;
			}
		} catch (RuntimeException e) {
			// State attribute not present. Assuming it started
			if (e.getCause() instanceof AttributeNotFoundException) {
				if (debug) {
					LOG.debug("MBean: " + bean + " is missing the attribute. Returning true");
				}
				return true;
			}
			throw e;
		}
		
		if (debug) {
			LOG.debug("MBean: " + bean + " is not started. Returning false");
		}
		return false;
	}

	public String[] findAttribute(String bean, String attributeRegExp) {
		try {
			MBeanAttributeInfo[] attributes = getMBeanServerConnection().getMBeanInfo(new ObjectName(bean)).getAttributes();
			ArrayList<String> answer = new ArrayList<String>();
			for (int i=0; i<attributes.length; i++) {
				if (attributes[i].getName().matches(attributeRegExp)) {
					answer.add(attributes[i].getName());
				}
			}
			if (LOG.isDebugEnabled()) {
				LOG.debug("Found the following matching attributes: " + answer);
			}
			return answer.toArray(new String[answer.size()]);
		} catch (InstanceNotFoundException e) {
			throw new RuntimeException("Failed to check MBean: " + bean, e);
		} catch (IntrospectionException e) {
			throw new RuntimeException("Failed to check MBean: " + bean, e);
		} catch (MalformedObjectNameException e) {
			throw new RuntimeException("Failed to check MBean: " + bean, e);
		} catch (ReflectionException e) {
			throw new RuntimeException("Failed to check MBean: " + bean, e);
		} catch (NullPointerException e) {
			throw new RuntimeException("Failed to check MBean: " + bean, e);
		} catch (IOException e) {
			throw new RuntimeException("Failed to check MBean: " + bean, e);
		}		
	}

	
	public boolean attributeExists(String bean, String attribute) {
		try {
			MBeanAttributeInfo[] attributes = getMBeanServerConnection().getMBeanInfo(new ObjectName(bean)).getAttributes();
			for (int i=0; i<attributes.length; i++) {
				if (attributes[i].getName().equals(attribute)) {
					if (LOG.isDebugEnabled()) {
						LOG.debug("MBean: " + bean + " has the attribute: " + attribute + ".Returning true");
					}
					return true;
				}
			}
		} catch (IllegalStateException e) {
			// TODO Remove when https://issues.jboss.org/browse/AS7-6074 is fixed
			if (LOG.isDebugEnabled()) {
				LOG.debug("Ignored Exception (See: https://issues.jboss.org/browse/AS7-6074) when failing to check attribute for: " + bean, e);
			}
		} catch (InstanceNotFoundException e) {
			throw new RuntimeException("Failed to check MBean: " + bean, e);
		} catch (IntrospectionException e) {
			throw new RuntimeException("Failed to check MBean: " + bean, e);
		} catch (MalformedObjectNameException e) {
			throw new RuntimeException("Failed to check MBean: " + bean, e);
		} catch (ReflectionException e) {
			throw new RuntimeException("Failed to check MBean: " + bean, e);
		} catch (NullPointerException e) {
			throw new RuntimeException("Failed to check MBean: " + bean, e);
		} catch (IOException e) {
			throw new RuntimeException("Failed to check MBean: " + bean, e);
		}
		return false;
	}	

	/* (non-Javadoc)
	 * @see net.welen.buzz.typehandler.utils.jmxinvoker.JMXInvoker#getImplementingClassName(java.lang.String)
	 */
	public String getImplementingClassName(String bean) {
		try {
			return getMBeanServerConnection().getMBeanInfo(new ObjectName(bean)).getClassName();
		} catch (IllegalStateException e) {
			// TODO Remove when https://issues.jboss.org/browse/AS7-6074 is fixed
			if (LOG.isDebugEnabled()) {
				LOG.debug("Ignored Exception (See: https://issues.jboss.org/browse/AS7-6074) when failing to get classname for MBean: " + bean, e);
			}
			return "";
		} catch (Exception e) {
			LOG.error("Failed to get classname for MBean: " + bean, e);
			return "";
		}
	}
}
