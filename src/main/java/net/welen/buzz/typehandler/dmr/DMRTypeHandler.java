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
package net.welen.buzz.typehandler.dmr;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;

import net.welen.buzz.protocols.BuzzAnswer;
import net.welen.buzz.typehandler.AbstractTypeHandler;
import net.welen.buzz.typehandler.TypeHandler;
import net.welen.buzz.typehandler.TypeHandlerException;

/**
 * JBoss DMR typeHandler
 * 
 * This is just a simple front that checks if the real impl. is available
 * (if we are running in JBoss 7) and tries to load the needed impl. in
 * that case
 *  
 * @author welle
 */
public class DMRTypeHandler extends AbstractTypeHandler {

	private static final Logger LOG = Logger.getLogger(DMRTypeHandler.class);
	private static String IMPL_CLASSNAME = "net.welen.buzz.typehandler.dmr.DMRTypeHandlerImpl";
	
	// Real impl.	
	private TypeHandler impl = null;
	
	/**
	 * @param category
	 * @param measurementUnit
	 * @param typeHandlerProperties
	 * @throws UnknownHostException 
	 */
	public DMRTypeHandler(String category, String measurementUnit, Properties typeHandlerProperties) throws UnknownHostException {
		super(category, measurementUnit, typeHandlerProperties);

		boolean debug = LOG.isDebugEnabled();
		
		if (debug) {
			LOG.debug("Trying to load TypeHandler \"" + IMPL_CLASSNAME + "\"");
		}	
		Class<? extends TypeHandler> typehandlerClass;
		try {
			typehandlerClass = Class.forName(IMPL_CLASSNAME).asSubclass(TypeHandler.class);
			Class<?> argClasses[] = {String.class, String.class, Properties.class};
			Constructor<?> constructor = typehandlerClass.getConstructor(argClasses);
			Object argValues[] = {category, measurementUnit, typeHandlerProperties};
			impl = (TypeHandler) constructor.newInstance(argValues);
		} catch (ClassNotFoundException e) {
			if (debug) {
				LOG.debug("Failed load TypeHandler \"" + IMPL_CLASSNAME + "\". We are not running JBoss 7");
			}	
		} catch (SecurityException e) {
			if (debug) {
				LOG.debug("Failed load TypeHandler \"" + IMPL_CLASSNAME + "\". We are not running JBoss 7");
			}	
		} catch (NoSuchMethodException e) {
			if (debug) {
				LOG.debug("Failed load TypeHandler \"" + IMPL_CLASSNAME + "\". We are not running JBoss 7");
			}	
		} catch (IllegalArgumentException e) {
			if (debug) {
				LOG.debug("Failed load TypeHandler \"" + IMPL_CLASSNAME + "\". We are not running JBoss 7");
			}	
		} catch (InstantiationException e) {
			if (debug) {
				LOG.debug("Failed load TypeHandler \"" + IMPL_CLASSNAME + "\". We are not running JBoss 7");
			}	
		} catch (IllegalAccessException e) {
			if (debug) {
				LOG.debug("Failed load TypeHandler \"" + IMPL_CLASSNAME + "\". We are not running JBoss 7");
			}	
		} catch (InvocationTargetException e) {
			if (debug) {
				LOG.debug("Failed load TypeHandler \"" + IMPL_CLASSNAME + "\". We are not running JBoss 7");
			}	
		}
	}

	/* (non-Javadoc)
	 * @see net.welen.buzz.typehandler.TypeHandler#getMeasurableUnits()
	 */
	public List<String> getMeasurableUnits() throws TypeHandlerException {		
		if (impl != null) {
			return impl.getMeasurableUnits();
		}
		// Return empty list
		return new ArrayList<String>();				
	}

	/* (non-Javadoc)
	 * @see net.welen.buzz.typehandler.TypeHandler#getValues(net.welen.buzz.protocols.BuzzAnswer)
	 */
	public void getValues(BuzzAnswer values) throws TypeHandlerException {
		if (impl != null) {
			impl.getValues(values);
		}
	}
	
}
