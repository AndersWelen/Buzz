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
package net.welen.buzz.configuration;

import java.io.IOException;
import java.util.List;

import net.welen.buzz.typehandler.TypeHandler;

/**
 * MBean interface for the Buzz configuration
 * 
 * @author welle
 */
public interface ConfigurationMBean {
	
	public void start() throws Exception;
	
	public void reconfigure() throws IOException;

	public Integer getLevel();
	public void setLevel(Integer level);

	public Integer getDefaultConfigurationLevel();

	public List<TypeHandler> getTypeHandlers();
	public TypeHandler getTypeHandler(String name);
	
	public String printSetup();
	
	public String getExternalConfigFilename();
	public void setExternalConfigFilename(String filename);
	
	public Boolean getEnableBuiltInConfig();
	public void setEnableBuiltInConfig(Boolean value);
}
