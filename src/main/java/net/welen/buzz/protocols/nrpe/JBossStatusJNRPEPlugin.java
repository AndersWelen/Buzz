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
package net.welen.buzz.protocols.nrpe;

import org.apache.log4j.Logger;

import net.welen.buzz.protocols.BuzzAnswer;
import it.jnrpe.ICommandLine;
import it.jnrpe.ReturnValue;
import it.jnrpe.net.IJNRPEConstants;
import it.jnrpe.plugins.IPluginInterface;

/**
 * JNRPE plugin for Buzz
 * 
 * @author welle
 *
 */
public class JBossStatusJNRPEPlugin implements IPluginInterface {

	private static final Logger LOG = Logger.getLogger(JBossStatusJNRPEPlugin.class);
	
	private NRPE nrpe;

	public JBossStatusJNRPEPlugin(NRPE nrpe) {
		this.nrpe = nrpe;
	}
	
	/* (non-Javadoc)
	 * @see it.jnrpe.plugins.IPluginInterface#execute(it.jnrpe.ICommandLine)
	 */
	public ReturnValue execute(ICommandLine commandLine) {
		
		BuzzAnswer input = new BuzzAnswer();
		nrpe.getValues(input);
		
		boolean debug = LOG.isDebugEnabled();
		
		// Critical?
		NRPEAnswer critical = new NRPEAnswer();
		nrpe.filterAlarms(input, critical);
		if (critical.size() > 0) {
			if (debug) {
				LOG.debug("Returning: CRITICAL " + critical.toString());
			}
			return new ReturnValue(IJNRPEConstants.STATE_CRITICAL, "CRITICAL - " + critical.toString());
		}

		// Warnings
		NRPEAnswer warnings = new NRPEAnswer();
		nrpe.filterWarnings(input, warnings);
		if (warnings.size() > 0) {
			if (debug) {
				LOG.debug("Returning: WARNING " + warnings.toString());
			}			
			return new ReturnValue(IJNRPEConstants.STATE_WARNING, "WARNING - " + warnings.toString());
		}

		if (debug) {
			LOG.debug("Returning: OK ");
		}
		return new ReturnValue(IJNRPEConstants.STATE_OK, "OK - " + "No warnings or critical problems detected.");		
	}

}
