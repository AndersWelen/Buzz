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

import java.net.InetAddress;

import org.apache.log4j.Logger;

import com.quigley.zabbixj.agent.ZabbixAgent;

import net.welen.buzz.protocols.AbstractProtocol;
import net.welen.buzz.protocols.BuzzAnswer;

/**
 * Zabbix protocol
 * 
 * @author welle
 *
 */
public class Zabbix extends AbstractProtocol implements ZabbixMBean {

	private static final Logger LOG = Logger.getLogger(Zabbix.class);

	private ZabbixAnswer answer = null;
	private ZabbixAnswerCollector collector = new ZabbixAnswerCollector();
	
	private ZabbixAgent agent = null;
	private Boolean passive = true;	
	private Integer listenPort = 10050;
	private String listenAddress = "0.0.0.0";
	private Boolean active = false;
	private String hostName = "buzz";
	private String serverAddress = "localhost";
	private Integer serverPort = 10051;
	private Integer interval = 30*1000;

	private class ZabbixAnswerCollector extends Thread {
		boolean stop = false;
		
		public void run() {
				stop = false;
		        try {
		        	while (!stop) {
		        		ZabbixAnswer tmp = new ZabbixAnswer();
		        		getValues(tmp);
		        		answer = tmp;			
		        		Thread.sleep(interval);
		        	}
		        } catch (InterruptedException e) {
		        	// Nothing to do
		    }
		}
	
		public void stopCollector() {
			stop = true;
		}

	}
	
	/* (non-Javadoc)
	 * @see net.welen.buzz.protocols.Protocol#startProtocol()
	 */
	public void startProtocol() throws Exception {
		ZabbixAgent agent = new ZabbixAgent();

		// Start the collection thread
		collector.start();
		
		// Setup passive
		agent.setEnablePassive(passive);
		if (passive) {
			agent.setListenPort(listenPort);
			agent.setListenAddress(listenAddress);
		}

		// Setup active
		agent.setEnableActive(active);
		if (active) {			
			agent.setHostName(hostName);
			agent.setServerAddress(InetAddress.getByName(serverAddress));
			agent.setServerPort(serverPort);
		}
		
		// Add providers
		agent.addProvider("buzz", new BuzzMetricsProvider(this));
		
		// Start service
		agent.start();
		LOG.info("Buzz Zabbix started.");
	}
	
	protected Object getIndividualValue(String category, String name, String key) {
		BuzzAnswer data = getBuzzAnswer();					
		return data.getIndividualValue(category, name, key);
	}

	protected String[] getAllPaths() {
		ZabbixAnswer data = getBuzzAnswer();						
		return data.getAllPaths();
	}

	private ZabbixAnswer getBuzzAnswer() {		
		return answer;
	}
	
	/* (non-Javadoc)
	 * @see net.welen.buzz.protocols.Protocol#stopProtocol()
	 */
	public void stopProtocol() throws Exception {
		// Stop the collector
		collector.stopCollector();
		
		// TODO It doesn't seem to release the socket
		if (agent != null) {
			agent.stop();
		}
		LOG.info("Buzz Zabbix stopped.");
	}

	public void setListenPort(Integer listenPort) {
		this.listenPort = listenPort;
	}

	public Integer getListenPort() {
		return listenPort;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}

	public Boolean getActive() {
		return active;
	}

	public void setHostName(String hostName) {
		this.hostName = hostName;
	}

	public String getHostName() {
		return hostName;
	}

	public void setServerAddress(String serverAddress) {
		this.serverAddress = serverAddress;
	}

	public String getServerAddress() {
		return serverAddress;
	}

	public void setServerPort(Integer serverPort) {
		this.serverPort = serverPort;
	}

	public Integer getServerPort() {
		return serverPort;
	}

	public void setPassive(Boolean passive) {
		this.passive = passive;
	}

	public Boolean getPassive() {
		return passive;
	}

	public void setListenAddress(String listenAddress) {
		this.listenAddress = listenAddress;  
	}

	public String getListenAddress() {
		return listenAddress;
	}

	public void setInterval(Integer interval) {	
		this.interval = interval;
	}
	
	public Integer getInterval() {
		return interval;
	}
}
