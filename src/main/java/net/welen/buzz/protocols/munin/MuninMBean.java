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
package net.welen.buzz.protocols.munin;

import net.welen.buzz.protocols.ProtocolMBean;

/**
 * MBean interface for the Munin plugin.
 * 
 * @author welle
 */
public interface MuninMBean extends ProtocolMBean {
	
	public String getName();
	public void setName(String name);

	public String getAddress();
	public void setAddress(String address);

	public Integer getPort();
	public void setPort(Integer port);

	public Integer getTcpReadTimeOut();
	public void setTcpReadTimeOut(Integer timeout);

	public Integer getMaxThreads();
	public void setMaxThreads(Integer maxThreads);

	public Integer getCurrentThreads();
}
