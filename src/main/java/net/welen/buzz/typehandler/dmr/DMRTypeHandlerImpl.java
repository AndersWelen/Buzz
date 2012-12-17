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

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.as.controller.client.OperationBuilder;
import org.jboss.as.controller.client.helpers.ClientConstants;
import org.jboss.dmr.ModelNode;

import net.welen.buzz.protocols.BuzzAnswer;
import net.welen.buzz.typehandler.AbstractTypeHandler;
import net.welen.buzz.typehandler.TypeHandlerException;

/**
 * JBoss DMR typeHandler
 *  
 * @author welle
 */
public class DMRTypeHandlerImpl extends AbstractTypeHandler {

	private static final Logger LOG = Logger.getLogger(DMRTypeHandlerImpl.class);
	
	// JBoss7 client
	private ModelControllerClient client = null;
	
	/**
	 * @param category
	 * @param measurementUnit
	 * @param typeHandlerProperties
	 * @throws UnknownHostException 
	 */
	public DMRTypeHandlerImpl(String category, String measurementUnit, Properties typeHandlerProperties) throws UnknownHostException {
		super(category, measurementUnit, typeHandlerProperties);
		
		// get a inVM client
		client = ManagementService.getClient();
	}

	/* (non-Javadoc)
	 * @see net.welen.buzz.typehandler.TypeHandler#getMeasurableUnits()
	 */
	public List<String> getMeasurableUnits() throws TypeHandlerException {		
		List<String> answer = new ArrayList<String>();
		
		ModelNode request = new ModelNode();
	    request.get(ClientConstants.OP).set("read-resource");
	    request.get("recursive").set(false);
	    
	    String address[] = getProperties().get("type.param.address").toString().split("/");
	    String entry = null;
	    for (int i=0; i<address.length; i++) {
	    	// Ignore "empty" path entries
	    	if (address[i].length() == 0) {
	    		continue;
	    	}
	    	String keyValue[] = address[i].split("=");
	    	if (keyValue.length != 2) {
	    		throw new TypeHandlerException("Error parsing: " + getProperties().get("type.param.address"));
	    	}
	    	// Stop if we hit a wild card
	    	if (keyValue[1].contains("*")) {
	    		entry = keyValue[0];
	    		break;
	    	}
	    	request.get(ClientConstants.OP_ADDR).add(keyValue[0], keyValue[1]);
	    }
	 	    
	    ModelNode response;
		try {
		    if (LOG.isDebugEnabled()) {
		    	LOG.debug("Executing: " + request);
		    }
			response = client.execute(new OperationBuilder(request).build());
		    if (LOG.isDebugEnabled()) {
		    	LOG.debug("Respone: " + response);
		    }
		} catch (IOException e) {
			throw new TypeHandlerException(e);
		}

		if (entry != null) {
			ModelNode answerList = response.get(ClientConstants.RESULT).get(entry);
	 
		    if (answerList.isDefined()) {
		        for (ModelNode answerItem : answerList.asList()) {
		            answer.add(constructName(answerItem.asProperty().getName()));
		        }
		    }
		} else {			
			answer.add(constructName(response.get(ClientConstants.RESULT).asString()));
		}
		
	    if (LOG.isDebugEnabled()) {
	    	LOG.debug("Returning: " + answer);
	    }
		return answer;
	}

	/* (non-Javadoc)
	 * @see net.welen.buzz.typehandler.TypeHandler#getValues(net.welen.buzz.protocols.BuzzAnswer)
	 */
	public void getValues(BuzzAnswer values) throws TypeHandlerException {		
		for (String name : getMeasurableUnits()) {
			String attributes[] = ((String) getProperties().get("type.param.attributes")).split(",");			
			for (int i=0; i<attributes.length; i++) {
				String attributeName = attributes[i];
				
				// Get the value
				ModelNode request = new ModelNode();
				request.get(ClientConstants.OP).set("read-attribute");
				request.get(ClientConstants.NAME).set(attributeName);
				
				ModelNode addressNode = request.get(ClientConstants.OP_ADDR);

			    String address[] = getProperties().get("type.param.address").toString().split("/");
			    for (int j=0; j<address.length; j++) {
			    	// Ignore "empty" path entries
			    	if (address[j].length() == 0) {
			    		continue;
			    	}
			    	String keyValue[] = address[j].split("=");
			    	if (keyValue.length != 2) {
			    		throw new TypeHandlerException("Error parsing: " + getProperties().get("type.param.address"));
			    	}
			    	// Replace wildcards
			    	keyValue[1] = keyValue[1].replace("*", name);

			    	addressNode.add(keyValue[0], keyValue[1]);
			    }
				
				ModelNode answer;
				try {
				    if (LOG.isDebugEnabled()) {
				    	LOG.debug("Executing: " + request);
				    }
					answer = client.execute(new OperationBuilder(request).build());
				    if (LOG.isDebugEnabled()) {
				    	LOG.debug("Executing: " + answer);
				    }
				} catch (IOException e) {
					throw new TypeHandlerException(e);
				}	
				
				ModelNode answerValue = answer.get(ClientConstants.RESULT);
				if (!answerValue.isDefined()) {
					LOG.warn(request.toString() + " returned a not defined answer. Setting it to: " + BuzzAnswer.UNKNOWN);
					values.put(getCategory(), constructName(name), attributeName, BuzzAnswer.UNKNOWN);
				} else {
					String value = answer.get(ClientConstants.RESULT).asString();
					values.put(getCategory(), constructName(name), attributeName, value);
				}
			}
		}		

	}

	private String constructName(String name) {
	    if (LOG.isDebugEnabled()) {
	    	LOG.debug("Input: " + name);
	    }		

		Object o = getProperties().get("type.param.staticName");
		if (o != null) {
			return o.toString();
		}
		
		o = getProperties().get("type.param.namePrefix");
		if (o != null) {
			return o.toString() + name;
		}
		
	    if (LOG.isDebugEnabled()) {
	    	LOG.debug("Returning: " + name);
	    }		
		return name;
	}

}
