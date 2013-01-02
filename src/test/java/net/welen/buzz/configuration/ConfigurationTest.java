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
package net.welen.buzz.configuration;

import java.io.IOException;

import junit.framework.Assert;

import org.junit.Test;

public class ConfigurationTest {
		
	private void cleanSystemProperties() {
		System.getProperties().remove("buzz.level");
		System.getProperties().remove("buzz.externalConfigFilename");
		System.getProperties().remove("buzz.enableBuiltInConfig");
		System.getProperties().remove("buzz.munin.port");
		System.getProperties().remove("buzz.munin.address");
		System.getProperties().remove("buzz.munin.name");
		System.getProperties().remove("buzz.munin.TcpReadTimeOut");
		System.getProperties().remove("buzz.munin.maxThreads");		
	}
	
	@Test
	public void testCreation() throws IOException {
		cleanSystemProperties();
		new Configuration().reconfigure();
	}

	@Test
	public void testCreationWithoutAnyConfig() throws IOException {
		cleanSystemProperties();
		Configuration config = new Configuration();
		config.setEnableBuiltInConfig(false);
		Assert.assertFalse(config.getEnableBuiltInConfig());
		config.reconfigure(); 
		Assert.assertEquals(0, config.getTypeHandlers().size());
	}

	@Test
	public void testCreationWithExternalConfigFile() throws IOException {
		cleanSystemProperties();
		Configuration config = new Configuration();
		config.setExternalConfigFilename("src/test/resources/setup.properties");
		config.setEnableBuiltInConfig(true);
		config.reconfigure();
		Assert.assertTrue(config.getTypeHandlers().size() > 0);
	}

	@Test
	public void testCreationWithExternalConfigFileOnly() throws IOException {
		cleanSystemProperties();
		Configuration config = new Configuration();
		config.setExternalConfigFilename("src/test/resources/setup.properties");
		config.setEnableBuiltInConfig(false);
		config.reconfigure();
		Assert.assertEquals(1, config.getTypeHandlers().size());
	}

	@Test(expected=IllegalArgumentException.class)
	public void testCreationWithIllegalExternalConfigFile() throws IOException {
		cleanSystemProperties();
		Configuration config = new Configuration();		
		config.setExternalConfigFilename("NON_EXISTING_FILE");
		config.reconfigure();
	}

	@Test
	public void testStartService() throws Exception {
		cleanSystemProperties();
		new Configuration().start();
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testClassNotFound() throws IOException {
		cleanSystemProperties();
		Configuration config = new Configuration();		
		config.setExternalConfigFilename("src/test/resources/ClassNotFound.properties");
		config.reconfigure();
	}

	@Test(expected=IllegalArgumentException.class)
	public void testClassCast() throws IOException {
		cleanSystemProperties();
		Configuration config = new Configuration();		
		config.setExternalConfigFilename("src/test/resources/ClassCast.properties");
		config.reconfigure();
	}

	@Test
	public void testSetLevel() throws IOException {
		cleanSystemProperties();
		Configuration config = new Configuration();
		config.reconfigure();				
		
		config.setLevel(6);
		Assert.assertEquals(6, config.getLevel().intValue());
	}

	@Test(expected=IllegalArgumentException.class)
	public void testSetLevelToHigh() throws IOException {
		cleanSystemProperties();
		Configuration config = new Configuration();
		config.reconfigure();				
		
		config.setLevel(11);		
	}

	@Test(expected=IllegalArgumentException.class)
	public void testSetLevelToLow() throws IOException {
		cleanSystemProperties();
		Configuration config = new Configuration();
		config.reconfigure();				
		
		config.setLevel(0);		
	}

	@Test
	public void testGetLevel() throws IOException {
		cleanSystemProperties();
		Configuration config = new Configuration();
		config.reconfigure();
		
		Assert.assertEquals(5, config.getLevel().intValue());		
	}
	
	@Test
	public void testGetDefaultLevel() throws IOException {
		cleanSystemProperties();
		Configuration config = new Configuration();
		config.reconfigure();
		
		Assert.assertEquals(5, config.getDefaultConfigurationLevel().intValue());
	}

	@Test
	public void testLevelFiltering() throws IOException {
		cleanSystemProperties();
		Configuration config = new Configuration();
		config.setLevel(5);
		config.setEnableBuiltInConfig(false);
		config.setExternalConfigFilename("src/test/resources/setup.properties");
		config.reconfigure();
		
		Assert.assertEquals(1, config.getTypeHandlers().size());
		
		config.setLevel(1);
		config.reconfigure();

		Assert.assertEquals(0, config.getTypeHandlers().size());
	}	
	
	@Test
	public void testSetExternalConfigFilename() throws IOException {
		cleanSystemProperties();
		Configuration config = new Configuration();
		
		Assert.assertNull(config.getExternalConfigFilename());

		String fileName="src/test/resources/setup.properties";
		config.setExternalConfigFilename(fileName);
		Assert.assertEquals(fileName, config.getExternalConfigFilename());
	}

	@Test
	public void testSetNullExternalConfigFilename() throws IOException {
		cleanSystemProperties();
		Configuration config = new Configuration();
		config.setExternalConfigFilename(null);
		Assert.assertNull(config.getExternalConfigFilename());
	}

	@Test(expected=IllegalArgumentException.class)
	public void testSetNonExistingExternalConfigFilename() throws IOException {
		cleanSystemProperties();
		new Configuration().setExternalConfigFilename("NON_EXISTING_FILE");
	}

	@Test
	public void testGetTypeHandler() throws IOException {
		cleanSystemProperties();
		Configuration config = new Configuration();
		config.setExternalConfigFilename("src/test/resources/setup.properties");
		config.setEnableBuiltInConfig(false);
		Assert.assertNull(config.getTypeHandler("sss"));
		config.reconfigure();
		Assert.assertNull(config.getTypeHandler("sss"));
		Assert.assertNotNull(config.getTypeHandler("mem"));
	}
}
