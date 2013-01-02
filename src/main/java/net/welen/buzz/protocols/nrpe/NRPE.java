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
package net.welen.buzz.protocols.nrpe;

import java.util.Arrays;

import org.apache.log4j.Logger;

import it.jnrpe.JNRPE;
import it.jnrpe.commands.CommandDefinition;
import it.jnrpe.commands.CommandOption;
import it.jnrpe.commands.CommandRepository;
import it.jnrpe.plugins.IPluginInterface;
import it.jnrpe.plugins.PluginDefinition;
import it.jnrpe.plugins.PluginOption;
import it.jnrpe.plugins.PluginRepository;
import net.welen.buzz.protocols.AbstractProtocol;
import net.welen.buzz.protocols.BuzzAnswer;
import net.welen.buzz.typehandler.TypeHandler;

/**
 * NRPE protcol for Nagios (and Icinga)
 * 
 * @author welle
 *
 */
public class NRPE extends AbstractProtocol implements NRPEMBean {

	private static final Logger LOG = Logger.getLogger(NRPE.class);
	
	private String address = "127.0.0.1";
	private Integer port = 5666;
	private String acceptedHosts[] = {"127.0.0.1"};
	private Boolean useSSL = false;
	private JNRPE engine = null;
	
	/* (non-Javadoc)
	 * @see net.welen.buzz.protocols.Protocol#startProtocol()
	 */
	public void startProtocol() throws Exception {
		
		// Create the plugins
		IPluginInterface jbossStatusPlugin = new JBossStatusJNRPEPlugin(this);
		PluginDefinition jbossStatusPluginDef = new PluginDefinition("JBossStatusPlugin", "JBoss Status Plugin", jbossStatusPlugin);
		IPluginInterface getValuePlugin = new GetValueJNRPEPlugin(this);		
		PluginDefinition getValuePluginDef = new PluginDefinition("GetValuePlugin", "GetValue Plugin", getValuePlugin);		 
		getValuePluginDef.addOption(new PluginOption()
								.setOption("g")
								.setLongOpt("get")
								.setArgName("variable")
								.setArgsOptional(false)
								.setRequired(true));		
		getValuePluginDef.addOption(new PluginOption()
                              	.setOption("w")
                              	.setLongOpt("warning")
                              	.setArgName("value")
                              	.setArgsOptional(false)
                              	.setRequired(false));
		getValuePluginDef.addOption(new PluginOption()
                              	.setOption("c")
                              	.setLongOpt("critical")
                              	.setArgName("value")
                              	.setArgsOptional(false)
                              	.setRequired(false)								
							);
		
		// Create the plugin repository
		PluginRepository pluginRepository = new PluginRepository();
		pluginRepository.addPluginDefinition(jbossStatusPluginDef);
		pluginRepository.addPluginDefinition(getValuePluginDef);

		// Create the commands
		CommandDefinition jbossStatusCommand = new CommandDefinition("checkJBoss", "JBossStatusPlugin");
		CommandDefinition specificCommand = new CommandDefinition("getValue", "GetValuePlugin")
												.addArgument(new CommandOption("get", "$ARG1$"))
												.addArgument(new CommandOption("warning", "$ARG2$"))
												.addArgument(new CommandOption("critical", "$ARG3$")
												);
		
		// Create the command repository		
		CommandRepository commandRepository = new CommandRepository();
		commandRepository.addCommandDefinition(jbossStatusCommand);
		commandRepository.addCommandDefinition(specificCommand);
		
		// Start JNRPE
		engine = new JNRPE(pluginRepository, commandRepository);
		if (LOG.isDebugEnabled()) {
			LOG.debug("Adding following hosts: " + Arrays.toString(getAcceptedHosts()));
		}
		for (int i=0; i<getAcceptedHosts().length; i++) {
			engine.addAcceptedHost(getAcceptedHosts()[i]);
		}		
		
		if (LOG.isDebugEnabled()) {
			LOG.debug("Starting listener");
		}
		// TODO How to handle SSL keys?
		engine.listen(address, port, useSSL);
		
		LOG.info("Buzz NRPE started.");
	}

	/* (non-Javadoc)
	 * @see net.welen.buzz.protocols.Protocol#stopProtocol()
	 */
	public void stopProtocol() throws Exception {
		if (engine != null)  {
			engine.shutdown();
		}
		LOG.info("Buzz NRPE stopped.");
	}

	/* (non-Javadoc)
	 * @see net.welen.buzz.protocols.nrpe.NRPEMBean#getAddress()
	 */
	public String getAddress() {
		return address;
	}

	/* (non-Javadoc)
	 * @see net.welen.buzz.protocols.nrpe.NRPEMBean#setAddress(java.lang.String)
	 */
	public void setAddress(String address) {
		this.address = address;
	}

	/* (non-Javadoc)
	 * @see net.welen.buzz.protocols.nrpe.NRPEMBean#getPort()
	 */
	public Integer getPort() {
		return port;
	}

	/* (non-Javadoc)
	 * @see net.welen.buzz.protocols.nrpe.NRPEMBean#setPort(int)
	 */
	public void setPort(Integer port) {
		this.port = port;
	}

	/**
	 * @return the acceptedHosts
	 */
	public String[] getAcceptedHosts() {
		return acceptedHosts.clone();
	}

	/**
	 * @param acceptedHosts the acceptedHosts to set
	 */
	public void setAcceptedHosts(String acceptedHosts[]) {
		// TODO JBoss 7 injects null values at undeploy (https://issues.jboss.org/browse/AS7-5726)
		if (acceptedHosts != null) {
			this.acceptedHosts = acceptedHosts.clone();
		}
	}

	/**
	 * @return the useSSL
	 */
	public Boolean isUseSSL() {
		return useSSL;
	}

	/**
	 * @param useSSL the useSSL to set
	 */
	public void setUseSSL(Boolean useSSL) {	
		this.useSSL = useSSL;
	}

	/* (non-Javadoc)
	 * @see net.welen.buzz.protocols.AbstractProtocol#getValues(net.welen.buzz.protocols.BuzzAnswer)
	 */
	@Override
	public void getValues(BuzzAnswer answer) {
		super.getValues(answer);
	}

	/* (non-Javadoc)
	 * @see net.welen.buzz.protocols.AbstractProtocol#filterWarnings(net.welen.buzz.protocols.BuzzAnswer, net.welen.buzz.protocols.BuzzAnswer)
	 */
	@Override
	public void filterWarnings(BuzzAnswer input, BuzzAnswer output) {
		super.filterWarnings(input, output);
	}

	/* (non-Javadoc)
	 * @see net.welen.buzz.protocols.AbstractProtocol#filterAlarms(net.welen.buzz.protocols.BuzzAnswer, net.welen.buzz.protocols.BuzzAnswer)
	 */
	@Override
	public void filterAlarms(BuzzAnswer input, BuzzAnswer output) {
		super.filterAlarms(input, output);
	}

	/* (non-Javadoc)
	 * @see net.welen.buzz.protocols.AbstractProtocol#getHandler(java.lang.String)
	 */
	@Override
	public TypeHandler getHandler(String name) {
		return super.getHandler(name);
	}

}
