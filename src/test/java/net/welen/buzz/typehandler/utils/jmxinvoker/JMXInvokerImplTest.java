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

import static org.junit.Assert.*;

import java.io.IOException;

import org.jboss.system.ServiceMBean;
import org.junit.Before;
import org.junit.Test;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

/**
 * @author welle
 *
 */
public class JMXInvokerImplTest {

	public interface MyTestMBean {
		public int getValue();
		public void testMethod();
	}
	public class MyTest implements MyTestMBean {
		public int getValue() {return 1;}
		public void testMethod(){};
	}
	
	static public int status = ServiceMBean.STOPPED;
	public interface MyJBossTestMBean extends ServiceMBean{}
	public class MyJBossTest implements MyJBossTestMBean {		
		public void create() throws Exception {}
		public void destroy() {}
		public void start() throws Exception {}
		public void stop() {}
		public String getName() {return null;}
		public int getState() {	
			return status;
		}
		public String getStateString() {
			return null;
		}
		public void jbossInternalLifecycle(String arg0) throws Exception {}
	}
	
	static String MBEAN_NAME = "test:type=test";
	static String JBOSS_MBEAN_NAME = "test:type=jbosstest";
	JMXInvoker invoker;
	
	@Before
	public void setup() throws InstanceAlreadyExistsException, MBeanRegistrationException, NotCompliantMBeanException, MalformedObjectNameException, NullPointerException {
		MBeanServer server = MBeanServerFactory.createMBeanServer();
		server.registerMBean(new MyTest(), new ObjectName(MBEAN_NAME));
		server.registerMBean(new MyJBossTest(), new ObjectName(JBOSS_MBEAN_NAME));
		
		invoker = JMXInvokerFactory.getJMXInvoker();
	}
		
	@Test(expected=MalformedObjectNameException.class)
	public void testFindWithIllegalInputJMXBeans() throws MalformedObjectNameException, IOException {
		assertEquals(0, invoker.findJMXBeans("ILLEGAL INPUT").size());
	}

	@Test
	public void testFindJMXBeans() throws MalformedObjectNameException, IOException {
		assertEquals(0, invoker.findJMXBeans("NOT.EXISTING:*").size());
		assertEquals(1, invoker.findJMXBeans(MBEAN_NAME).size());
		assertEquals(2, invoker.findJMXBeans("test:*").size());
	}

	@Test
	public void testGetAttributeValue()  {
		assertEquals(1, invoker.getAttributeValue(MBEAN_NAME, "Value"));		
	}

	@Test(expected=RuntimeException.class)
	public void testGetNonExistingAttributeValue()  {
		assertEquals(1, invoker.getAttributeValue(MBEAN_NAME, "NoteExisting"));		
	}

	@Test
	public void testGetAttributeValueAsString()  {
		assertEquals("1", invoker.getAttributeValueAsString(MBEAN_NAME, "Value"));		
	}

	@Test
	public void testExecuteMethod()  {
		invoker.executeMethod(MBEAN_NAME, "testMethod");		
	}

	@Test(expected=RuntimeException.class)
	public void testExecuteNonExistringMethod()  {
		invoker.executeMethod(MBEAN_NAME, "nonExistingMethod");		
	}

	@Test
	public void testIsMBeanStarted() {
		assertTrue(invoker.isMBeanStarted(MBEAN_NAME));
		status = ServiceMBean.STOPPED;
		assertFalse(invoker.isMBeanStarted(JBOSS_MBEAN_NAME));
		status = ServiceMBean.STARTED;
		assertTrue(invoker.isMBeanStarted(JBOSS_MBEAN_NAME));
	}
	
	@Test
	public void testFindAttribute() {
		assertEquals(0, invoker.findAttribute(MBEAN_NAME, "NonExistingAttribute").length);
		assertEquals(1, invoker.findAttribute(MBEAN_NAME, "Value").length);
		assertEquals(1, invoker.findAttribute(MBEAN_NAME, "Val.*").length);
	}

	@Test
	public void testAttributeExist() {
		assertFalse(invoker.attributeExists(MBEAN_NAME, "NonExistingAttribute"));
		assertFalse(invoker.attributeExists(MBEAN_NAME, "Valu"));
		assertTrue(invoker.attributeExists(MBEAN_NAME, "Value"));
	}


	@Test
	public void testGetImplementingClassName() {
		assertEquals("net.welen.buzz.typehandler.utils.jmxinvoker.JMXInvokerImplTest$MyTest", invoker.getImplementingClassName(MBEAN_NAME));
	}

}
