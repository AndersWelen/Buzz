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
package net.welen.buzz.typehandler.impl.hibernate;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.naming.InitialContext;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;

import net.welen.buzz.protocols.BuzzAnswer;
import net.welen.buzz.typehandler.AbstractTypeHandler;
import net.welen.buzz.typehandler.TypeHandlerException;

import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.ejb.EntityManagerFactoryImpl;
import org.hibernate.stat.SecondLevelCacheStatistics;

/**
 * Get Hibernate Second Level statistics (if enabled and bound in the JNDI)
 * 
 * @author welle
 */
public class HibernateSecondLevelStatisticsUnitHandler extends AbstractTypeHandler {
	
	private static final Logger LOG = Logger.getLogger(HibernateSecondLevelStatisticsUnitHandler.class);
	private static final char SEPARATOR = '@';
	
	public HibernateSecondLevelStatisticsUnitHandler(String category, String measurementUnit, Properties typeHandlerProperties) {
		super(category, measurementUnit, typeHandlerProperties);
	}

	public void getValues(BuzzAnswer values) throws TypeHandlerException {
		for (String tmp : getMeasurableUnits()) {
			String name = tmp.split("" + SEPARATOR)[0];
			String region = tmp.split("" + SEPARATOR)[1];			
			
			// Find the Hibernate Session Factory in JNDI
			SessionFactory sessionFactory = null;

			boolean debug = LOG.isDebugEnabled();
			
			Object o;
			try {
				if (debug) {
					LOG.debug("Looking up: " + name);
				}
				o = new InitialContext().lookup(name);
			} catch (NamingException e) {
				throw new TypeHandlerException(e);
			} 

			if (debug) {
				LOG.debug("Found class: " + o.getClass().getName());
			}
			if (o.getClass().getName().equals("org.hibernate.ejb.EntityManagerFactoryImpl")) {
				// Hibernate 4
				sessionFactory = ((EntityManagerFactoryImpl) o).getSessionFactory();
			} else {			
				// Hibernate 3
				sessionFactory = (SessionFactory) o;
			}
						
			// Get all values for the SecondLevelCacheRegion
			SecondLevelCacheStatistics stats = sessionFactory.getStatistics().getSecondLevelCacheStatistics(region);
			LOG.debug("Got SecondLevelCacheStatistics for region: " + region + " " + stats);
			String measurementUnit = getMeasurementUnit();
			
			try {
				values.put(measurementUnit, name + SEPARATOR + region, "ElementCountInMemory", stats.getElementCountInMemory());
				values.put(measurementUnit, name + SEPARATOR + region, "ElementCountOnDisk", stats.getElementCountOnDisk());
				values.put(measurementUnit, name + SEPARATOR + region, "HitCount", stats.getHitCount());
				values.put(measurementUnit, name + SEPARATOR + region, "MissCount", stats.getMissCount());
				values.put(measurementUnit, name + SEPARATOR + region, "PutCount", stats.getPutCount());
				values.put(measurementUnit, name + SEPARATOR + region, "SizeInMemory", stats.getSizeInMemory());
			} catch (IncompatibleClassChangeError e) {
				// Newer versions of SecondLevelCacheStatistics is not an Object anymore. It's an interface
				// so we use Reflection in that case (otherwise we need to recompile Buzz for the specific
				// version if Hibernate.
				LOG.debug("SecondLevelCacheStatistics is an Interface. Using Reflection");
				
				Class<?> statsClass = stats.getClass();
				try {
					values.put(measurementUnit, name + SEPARATOR + region, "ElementCountInMemory",
							statsClass.getMethod("getElementCountInMemory", (Class<?>) null).invoke(stats, (Object[]) null).toString()
							);
					values.put(measurementUnit, name + SEPARATOR + region, "ElementCountOnDisk",
							statsClass.getMethod("getElementCountOnDisk", (Class<?>) null).invoke(stats, (Object[]) null).toString()
							);
					values.put(measurementUnit, name + SEPARATOR + region, "HitCount",
							statsClass.getMethod("getHitCount", (Class<?>) null).invoke(stats, (Object[]) null).toString()
							);
					values.put(measurementUnit, name + SEPARATOR + region, "MissCount",
							statsClass.getMethod("getMissCount", (Class<?>) null).invoke(stats, (Object[]) null).toString()
							);
					values.put(measurementUnit, name + SEPARATOR + region, "PutCount",
							statsClass.getMethod("getPutCount", (Class<?>) null).invoke(stats, (Object[]) null).toString()
							);
					values.put(measurementUnit, name + SEPARATOR + region, "SizeInMemory",
							statsClass.getMethod("getSizeInMemory", (Class<?>) null).invoke(stats, (Object[]) null).toString()
							);					
				} catch (SecurityException ex) {
					throw new TypeHandlerException(ex);
				} catch (NoSuchMethodException ex) {
					throw new TypeHandlerException(ex);
				} catch (IllegalArgumentException ex) {
					throw new TypeHandlerException(ex);
				} catch (IllegalAccessException ex) {
					throw new TypeHandlerException(ex);
				} catch (InvocationTargetException ex) {
					throw new TypeHandlerException(ex);
				}
			}
		 }
	}

	/**
	 * Loops through the whole JNDI tree to find all Hibernate Session Factories and EJB3 EntityMangers
	 * and get all their SecondLevelCacheRegions
	 * 
	 * @param jndiName
	 * @return
	 * @throws TypeHandlerException
	 */
	private List<String> searchJNDI(String jndiName) throws TypeHandlerException {
		ArrayList<String> answer = new ArrayList<String>();
		
		boolean debug = LOG.isDebugEnabled();
		
		try {
			NamingEnumeration<NameClassPair> jndiContent = new InitialContext().list(jndiName);
			while (jndiContent.hasMore()) {
				NameClassPair node = jndiContent.nextElement();
				if (debug) {
					LOG.debug("Checking class: " + node.getClassName());
				}				
				if (node.getClassName().equals("org.jnp.interfaces.NamingContext")) {
					answer.addAll(searchJNDI(jndiName + "/" + node.getName()));
				}
				String className = node.getClassName(); 
				if (className.equals("org.hibernate.impl.SessionFactoryImpl")							// Hibernate
						|| className.equals("org.jboss.ejb3.entity.InjectedEntityManagerFactory")		// JBoss 4.2.3
						|| className.equals("org.jboss.jpa.injection.InjectedEntityManagerFactory")		// JBoss 5,1 6,1
						|| className.equals("org.hibernate.ejb.EntityManagerFactoryImpl")				// JBoss 7
						) {
					if (debug) {
						LOG.debug("Hibernate sessionfactory found: " + node.getName());
					}
					
					// Add all second level regions?
					SessionFactory sessionFactory;
					
					Object o;
					try {
						o = new InitialContext().lookup(node.getName());
					} catch (NamingException e) {
						throw new TypeHandlerException(e);
					} 
					if (o.getClass().getName().equals("org.hibernate.ejb.EntityManagerFactoryImpl")) {
						// Hibernate 4
						sessionFactory = ((EntityManagerFactoryImpl) o).getSessionFactory();
					} else {			
						// Hibernate 3
						sessionFactory = (SessionFactory) o;
					}

					String regions[] = sessionFactory.getStatistics().getSecondLevelCacheRegionNames();
					for (int i=0; i<regions.length; i++) {
						if (debug) {
							LOG.debug("Found second level cache region: " + regions[i]);
						}
						answer.add(jndiName + "/" + node.getName() + SEPARATOR + regions[i]);	
					}
				}				
			}
			return answer;			
		} catch (NamingException e) {
			throw new TypeHandlerException(e);
		}		
	}

	public List<String> getMeasurableUnits() throws TypeHandlerException {
		// Simple search for all Hibernate Session Factories and EJB3 EntityMangers
		// and return their SecondLevelCacheRegions as unique names		
		return searchJNDI("");
	}
}
