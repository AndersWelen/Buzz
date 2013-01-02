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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;

import net.welen.buzz.typehandler.TypeHandler;

import org.apache.log4j.Logger;

/**
 * MBean for Buzz configuration
 * 
 * @author welle
 */
public class Configuration implements ConfigurationMBean {

	private static final Logger LOG = Logger.getLogger(Configuration.class);
	private static final String filename = "setup.properties";

	private Integer level = 5;
	private Integer defaultConfigurationLevel = 5;
	private String externalFilename = null;
	private Properties setup = new Properties();
	private List<TypeHandler> typeHandlers = null;
	private Boolean enableBuiltInConfig = true;
	

	public void start() throws Exception {
		reconfigure();
	}

	/* (non-Javadoc)
	 * @see net.welen.buzz.configuration.ConfigurationMBean#reconfigure()
	 */
	public void reconfigure() throws IOException {
				
		if (LOG.isDebugEnabled()) {
			LOG.debug("Reading configuration from \"" + filename + "\" and \"" + externalFilename + "\"");
		}
		
		Properties newSetup = new Properties();
		InputStream inputStream = null;

		// Read the internal config file
		if (enableBuiltInConfig) {
			try {
				inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(filename);
				newSetup.load(inputStream);			
			} finally{
				if (inputStream != null) {
					inputStream.close();
				}			
			}
		}

		// Complement with the external config file (if any)				
		if (externalFilename != null) {				
			try {
				if (LOG.isDebugEnabled()) {
					LOG.debug("Adding configuration from external file: " + externalFilename);
				}
				inputStream = new FileInputStream(new File(externalFilename));
				newSetup.load(inputStream);			
			} finally{
				if (inputStream != null) {
					inputStream.close();
				}			
			}
		}
				
		// Create a new list of TypeHandlers
		List<TypeHandler> newTypeHandlers = new ArrayList<TypeHandler>();
		for (Object keyObject : newSetup.keySet()) {
			String key = (String) keyObject;
						
			String className;
			if (key.matches("[^.]*\\.type$")) {
				String measurementName = key.split("\\.")[0];
				
				// Check level
				int configurationLevel = defaultConfigurationLevel;
				String configurationLevelString = newSetup.getProperty(measurementName + ".type.param.level");
				if (configurationLevelString != null) {
					configurationLevel = Integer.parseInt(configurationLevelString);
				}
				if (configurationLevel > level) {
					if (LOG.isDebugEnabled()) {
						LOG.debug(configurationLevel + ">" + level + " skipping: " + measurementName);
					}
					continue;
				}
		
				// Create the typehandler
				className = newSetup.getProperty(key);
				if (LOG.isDebugEnabled()) {
					LOG.debug("Creating TypeHandler for \"" + measurementName + "\" using class \"" + className + "\"");
				}

				try {
					Class<? extends TypeHandler> typehandlerClass = Class.forName(className).asSubclass(TypeHandler.class);
					
					Class<?> argClasses[] = {String.class, String.class, Properties.class};
					Constructor<?> constructor = typehandlerClass.getConstructor(argClasses);
									
					Object argValues[] = {key.replace(".type", ""), measurementName, getSubProperties(newSetup, measurementName)};
					newTypeHandlers.add((TypeHandler) constructor.newInstance(argValues));
					
				} catch (ClassNotFoundException e) {
					throw new IllegalArgumentException("Class not found: " + className, e);
				} catch (ClassCastException e) {
					throw new IllegalArgumentException("Class does not implement TypeHandler: " + className, e);
				} catch (NoSuchMethodException e) {
					throw new IllegalArgumentException("Constructor not found: " + className, e);
				} catch (InvocationTargetException e) {
					throw new IllegalArgumentException("Constructor could not be called: " + className, e);
				} catch (IllegalArgumentException e) {
					throw new IllegalArgumentException("Constructor could not be called: " + className, e);
				} catch(IllegalAccessException e) {
					throw new IllegalArgumentException("Constructor could not be called: " + className, e);
				} catch (InstantiationException e) {
					throw new IllegalArgumentException("Constructor could not be called: " + className, e);
				}

			}			
		}
		
		// Use the new setup
		setup = newSetup;
		typeHandlers = newTypeHandlers;
		
		LOG.info("Buzz configured and started with " + typeHandlers.size() + " typehandlers.");		
	}

	private Object getSubProperties(Properties setup, String measurementName) {
		Properties answer = new Properties();
				
		for (Entry<Object, Object> entry : setup.entrySet()) {
			String key = (String) entry.getKey();			
			if (key.matches("^" + measurementName + ".*")) {
				answer.put(key.substring(measurementName.length() + 1), entry.getValue());
			}
		}
		if (LOG.isDebugEnabled()) {
			LOG.debug("Returning the following subProperties: " + answer + " for measurementName: "+ measurementName);
		}
		return answer;
	}

	/* (non-Javadoc)
	 * @see net.welen.buzz.configuration.ConfigurationMBean#getLevel()
	 */
	public Integer getLevel() {
		return level;
	}

	/* (non-Javadoc)
	 * @see net.welen.buzz.configuration.ConfigurationMBean#setLevel(int)
	 */
	public void setLevel(Integer level) {
		if (level == null) {
			// TODO JBoss 7 inject null values at undeploy (https://issues.jboss.org/browse/AS7-5726) 
			return;
		}
		if (level < 1 || level > 10) {
			throw new IllegalArgumentException("Level must be between 1 and 10.");
		}
		if (LOG.isDebugEnabled()) {
			LOG.debug("Setting level to " + level);
		}
		this.level = level;
	}

	/* (non-Javadoc)
	 * @see net.welen.buzz.configuration.ConfigurationMBean#printSetup()
	 */
	public String printSetup() {
		return setup.toString();
	}

	/* (non-Javadoc)
	 * @see net.welen.buzz.configuration.ConfigurationMBean#getExternalConfigFilename()
	 */
	public String getExternalConfigFilename() {
		return externalFilename;
	}

	/* (non-Javadoc)
	 * @see net.welen.buzz.configuration.ConfigurationMBean#setExternalConfigFilename(java.lang.String)
	 */
	public void setExternalConfigFilename(String filename) {
		// TODO JBoss 7 injects null values at undeploy (https://issues.jboss.org/browse/AS7-5726)
		if (filename != null) {
			if (filename.length() == 0) {
				filename = null;
			} else if (!new File(filename).canRead() || !new File(filename).isFile()) {
				throw new IllegalArgumentException("File \"" + filename + "\" is not a readable file.");
			}
		}
		
		if (LOG.isDebugEnabled()) {
			LOG.debug("Setting external filename to: \"" + filename + "\"");
		}

		externalFilename = filename;
	}

	/* (non-Javadoc)
	 * @see net.welen.buzz.configuration.ConfigurationMBean#getDefaultConfigurationLevel()
	 */
	public Integer getDefaultConfigurationLevel() {
		return defaultConfigurationLevel;
	}

	/* (non-Javadoc)
	 * @see net.welen.buzz.configuration.ConfigurationMBean#getTypeHandlers()
	 */
	public List<TypeHandler> getTypeHandlers() {
		return typeHandlers;
	}

	/* (non-Javadoc)
	 * @see net.welen.buzz.configuration.ConfigurationMBean#getTypeHandler(java.lang.String)
	 */
	public TypeHandler getTypeHandler(String name) {
		if (typeHandlers != null) {
			for (TypeHandler typeHandler : typeHandlers) {
				if (typeHandler.getCategory().equals(name)) {
					if (LOG.isDebugEnabled()) {
						LOG.debug("Returning the typehandler: " + typeHandler + " matching " + name);
					}
					return typeHandler;
				}
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see net.welen.buzz.configuration.ConfigurationMBean#getEnableBuiltInConfig()
	 */
	public Boolean getEnableBuiltInConfig() {
		return enableBuiltInConfig ;
	}

	/* (non-Javadoc)
	 * @see net.welen.buzz.configuration.ConfigurationMBean#setEnableBuiltInConfig(boolean)
	 */
	public void setEnableBuiltInConfig(Boolean value) {
		enableBuiltInConfig = value;		
	} 

}
