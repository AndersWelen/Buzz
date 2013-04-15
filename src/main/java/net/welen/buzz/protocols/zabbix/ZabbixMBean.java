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
package net.welen.buzz.protocols.zabbix;

import net.welen.buzz.protocols.ProtocolMBean;

/**
 * @author welle
 *
 */
public interface ZabbixMBean extends ProtocolMBean {

	public void setInterval(Integer interval);
	public Integer getInterval();
	public void setListenPort(Integer listenPort);
	public Integer getListenPort();
	public void setListenAddress(String listenAddress);
	public String getListenAddress();
	public void setActive(Boolean active);
	public Boolean getActive();
	public void setPassive(Boolean active);
	public Boolean getPassive();
	public void setHostName(String hostName);
	public String getHostName();
	public void setServerAddress(String serverAddress);
	public String getServerAddress();	
	public void setServerPort(Integer serverPort);
	public Integer getServerPort();
	
}
