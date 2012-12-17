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
package net.welen.buzz.protocols.munin;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

import org.apache.log4j.Logger;

import net.welen.buzz.protocols.AbstractProtocol;

/**
 * Implements the Munin protocol
 * 
 * @author welle
 */
public class Munin extends AbstractProtocol implements MuninMBean, Runnable {
	
	private static final Logger LOG = Logger.getLogger(Munin.class);

	private ServerSocket serverSocket;
	
	private String name = "jboss";
	private String address = "localhost";
	private Integer port = 4949;
	private Integer tcpReadTimeOut = 10000;
	private Integer maxThreads = 5;

	private boolean stopped = false;
	private boolean socketStopped = false;

	protected int currentThreads;

	public void startProtocol() throws Exception {
		LOG.info("Starting Buzz Munin plugin on: " + address + ":" + port);

		// TODO TLS support goes here in the future
		// if (useTLS) {
		// ..
		// } else {
		serverSocket = new ServerSocket(port, -1, InetAddress.getByName(address));
		
		serverSocket.setSoTimeout(1000);
			
		new Thread(this, "Buzz Munin thread").start();
	}

	public void stopProtocol() throws Exception {		
		LOG.info("Stopping Buzz Munin protocol..");
		stopped = true;
		while (!socketStopped) {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				LOG.warn(e);
			}
		}
		LOG.info("Buzz Munin protocol stopped.");
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		stopped = false;
		socketStopped = false;
		try {
			while (!stopped) {
				try {
					Socket socket = serverSocket.accept();
					LOG.debug("Incoming connection. Starting new socket thread");
					new MuninSocketHandler(socket, this).start();
				} catch (SocketTimeoutException e) {
					LOG.trace("ServerSocket timeout thrown.");
				} catch (IOException e) {
					LOG.error(e.getMessage(), e);
				}
			}
		} finally {
			LOG.debug("Munin server socket stopped");
			if (serverSocket != null && !serverSocket.isClosed()) {
				try {
					serverSocket.close();
				} catch (IOException e) {
					LOG.error(e.getMessage(), e);
				}
			}
			socketStopped = true;
		}
	}	

	/* (non-Javadoc)
	 * @see net.welen.buzz.mbean.MuninMBean#getName()
	 */
	public String getName() {
		return name;
	}

	/* (non-Javadoc)
	 * @see net.welen.buzz.mbean.MuninMBean#setName(java.lang.String)
	 */
	public void setName(String name) {
		this.name = name;
	}

	/* (non-Javadoc)
	 * @see net.welen.buzz.mbean.MuninMBean#getAddress()
	 */
	public String getAddress() {
		return address;
	}

	/* (non-Javadoc)
	 * @see net.welen.buzz.mbean.MuninMBean#setAddress(java.lang.String)
	 */
	public void setAddress(String address) {
		this.address = address;
	}

	/* (non-Javadoc)
	 * @see net.welen.buzz.mbean.MuninMBean#getPort()
	 */
	public Integer getPort() {
		return port;
	}

	/* (non-Javadoc)
	 * @see net.welen.buzz.mbean.MuninMBean#setPort(int)
	 */
	public void setPort(Integer port) {
		this.port = port;
	}

	/* (non-Javadoc)
	 * @see net.welen.buzz.mbean.MuninMBean#getTcpReadTimeOut()
	 */
	public Integer getTcpReadTimeOut() {
		return tcpReadTimeOut;
	}

	/* (non-Javadoc)
	 * @see net.welen.buzz.mbean.MuninMBean#setTcpReadTimeOut(int)
	 */
	public void setTcpReadTimeOut(Integer timeout) {
		this.tcpReadTimeOut = timeout;
	}

	/* (non-Javadoc)
	 * @see net.welen.buzz.mbean.MuninMBean#getMaxThreads()
	 */
	public Integer getMaxThreads() {
		return maxThreads;
	}

	/* (non-Javadoc)
	 * @see net.welen.buzz.mbean.MuninMBean#setMaxThreads(int)
	 */
	public void setMaxThreads(Integer maxThreads) {
		this.maxThreads = maxThreads;
	}

	/* (non-Javadoc)
	 * @see net.welen.buzz.protocols.munin.MuninMBean#getCurrentThreads()
	 */
	public Integer getCurrentThreads() {
		return currentThreads;
	}

}
