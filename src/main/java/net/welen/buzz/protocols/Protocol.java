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
package net.welen.buzz.protocols;

/**
 * Internal interface for a Buzz protocol
 * 
 * @author welle
 */
public interface Protocol {
	
	/**
	 * Callback for starting the protocol, if needed
	 * 
	 * @throws Exception
	 */
	public void startProtocol() throws Exception;

	/**
	 * Callback for stopping the protocol, if needed
	 * 
	 * @throws Exception
	 */
	public void stopProtocol() throws Exception;

}
