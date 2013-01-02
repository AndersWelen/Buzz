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
package net.welen.buzz.typehandler.impl.hibernate;

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
import org.hibernate.stat.Statistics;
import org.hibernate.ejb.EntityManagerFactoryImpl;

/**
 * Get Hibernate statistics (if enabled and bound in the JNDI)
 * 
 * @author welle
 */
public class HibernateStatisticsUnitHandler extends AbstractTypeHandler {
	
	private static final Logger LOG = Logger.getLogger(HibernateStatisticsUnitHandler.class);
	
	public HibernateStatisticsUnitHandler(String category, String measurementUnit, Properties typeHandlerProperties) {
		super(category, measurementUnit, typeHandlerProperties);
	}

	public void getValues(BuzzAnswer values) throws TypeHandlerException {		
		for (String name : getMeasurableUnits()) {
			boolean debug = LOG.isDebugEnabled();
			
			// Find the Hibernate Session Factory in JNDI
			SessionFactory sessionFactory = null;

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
			
			// Get all values
			Statistics stats = sessionFactory.getStatistics();
			String measurementUnit = getMeasurementUnit();
			values.put(measurementUnit, name, "SessionOpenCount", stats.getSessionOpenCount());
			values.put(measurementUnit, name, "SessionCloseCount", stats.getSessionCloseCount());
			values.put(measurementUnit, name, "FlushCount", stats.getFlushCount());			
			values.put(measurementUnit, name, "ConnectCount", stats.getConnectCount());
			values.put(measurementUnit, name, "PrepareStatementCount", stats.getPrepareStatementCount());
			values.put(measurementUnit, name, "CloseStatementCount", stats.getCloseStatementCount());
			values.put(measurementUnit, name, "EntityLoadCount", stats.getEntityLoadCount());
			values.put(measurementUnit, name, "EntityUpdateCount", stats.getEntityUpdateCount());
			values.put(measurementUnit, name, "EntityInsertCount", stats.getEntityInsertCount());
			values.put(measurementUnit, name, "EntityDeleteCount", stats.getEntityDeleteCount());
			values.put(measurementUnit, name, "EntityFetchCount", stats.getEntityFetchCount());			
			values.put(measurementUnit, name, "CollectionLoadCount", stats.getCollectionLoadCount());
			values.put(measurementUnit, name, "CollectionUpdateCount", stats.getCollectionUpdateCount());
			values.put(measurementUnit, name, "CollectionRemoveCount", stats.getCollectionRemoveCount());
			values.put(measurementUnit, name, "CollectionRecreateCount", stats.getCollectionRecreateCount());
			values.put(measurementUnit, name, "CollectionFetchCount", stats.getCollectionFetchCount());
			values.put(measurementUnit, name, "SecondLevelCacheHitCount", stats.getSecondLevelCacheHitCount());
			values.put(measurementUnit, name, "SecondLevelCacheMissCount", stats.getSecondLevelCacheMissCount());
			values.put(measurementUnit, name, "SecondLevelCachePutCount", stats.getSecondLevelCachePutCount());
			values.put(measurementUnit, name, "QueryExecutionCount", stats.getQueryExecutionCount());
			values.put(measurementUnit, name, "QueryExecutionMaxTime", stats.getQueryExecutionMaxTime());
			values.put(measurementUnit, name, "QueryCacheHitCount", stats.getQueryCacheHitCount());
			values.put(measurementUnit, name, "QueryCacheMissCount", stats.getQueryCacheMissCount());
			values.put(measurementUnit, name, "QueryCachePutCount", stats.getQueryCachePutCount());			
			values.put(measurementUnit, name, "TransactionCount", stats.getTransactionCount());
			values.put(measurementUnit, name, "OptimisticFailureCount", stats.getOptimisticFailureCount());
			
			// TODO What about?
			// sessionFactory.getStatistics().getEntityStatistics(<parameter from setup? OR loop?>).
			// sessionFactory.getStatistics().getCollectionStatistics(<parameter from setup? OR loop?>)
			// sessionFactory.getStatistics().getQueryStatistics(<<parameter from setup? OR loop?>)
		 }
	}

	/**
	 * Loops through the whole JNDI tree to find all Hibernate Session Factories and EJB3 EntityMangers
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
					answer.add(jndiName + "/" + node.getName());
				}				
			}
			return answer;			
		} catch (NamingException e) {
			throw new TypeHandlerException(e);
		}		
	}

	public List<String> getMeasurableUnits() throws TypeHandlerException {
		// Simple search for all Hibernate Session Factories and EJB3 EntityMangers
		// and return their JNDI names as unique names
		return searchJNDI("");
	}
}
