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
package net.welen.buzz.protocols;

import java.util.Map;
import java.util.Map.Entry;

import net.welen.buzz.utils.TreeNode;

import org.apache.log4j.Logger;

/**
 * Basic Java structure of Buzz answer values.
 * <P>
 * Category-Name-Attribute-Value
 * 
 * @author welle
 */
public class BuzzAnswer {

	// Preferred delimiter to use between "nodes" in various toString in subclasses
	public final static String DELIMITER = "/";
	
	private static final Logger LOG = Logger.getLogger(BuzzAnswer.class);

	public static final String UNKNOWN = "UNKNOWN";

	protected TreeNode data = new TreeNode(null, "");	
		
	/**
	 * Saves an entry.
	 * 
	 * @param category
	 * @param name
	 * @param attribute
	 * @param value
	 */	
	public void put(String category, String name, String attribute, Object value){
		if (LOG.isDebugEnabled()) {
			LOG.debug("Adding value \"" + value + "\" at \"" + category + DELIMITER + name + DELIMITER + attribute + "\"");
		}
		
		// Make sure that the DELIMITER is not a part of the name
		String categoryFixed = category.replace(DELIMITER, "_");
		String nameFixed = name.replace(DELIMITER, "_");
		
		TreeNode node = data.getOrCreateChild(new String[]{categoryFixed, nameFixed});
		node.getData().put(attribute, value);		
	}
	
	/**
	 * Removes an entry
	 * 
	 * @param category
	 * @param name
	 * @param attribute
	 * @param value
	 */
	public void remove(String category, String name, String attribute) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("Removing entry at \"" + category + DELIMITER + name + DELIMITER + attribute + "\"");
		}
		
		// Make sure that the DELIMITER is not a part of the name
		String categoryFixed = category.replace(DELIMITER, "_");
		String nameFixed = name.replace(DELIMITER, "_");
		
		TreeNode node = data.getOrCreateChild(new String[]{categoryFixed, nameFixed});
		node.getData().remove(attribute);		
	}

	public Map<String, Object> getAttributes(String category, String name) {
		TreeNode child = data.getChild(new String[]{category, name});
		if (child == null) {
			return null;
		}
		return child.getData();
	}
	
	public Object getIndividualValue(String category, String name, String key) {
		TreeNode child = data.getChild(new String[]{category, name});
		if (child == null) {
			return null;
		}
		Map<String, Object> map = child.getData();
		if (map == null) {
			return null;
		}
		return map.get(key);
	}
	
	public int size() {
		return data.getChildren().size();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		
		for (Entry<String, TreeNode> categoryEntry : data.getChildren().entrySet()) {
			String category = categoryEntry.getKey();
			TreeNode categoryNode = categoryEntry.getValue();
			
			for (Entry<String, TreeNode> nameEntry : categoryNode.getChildren().entrySet()) {
				String name = nameEntry.getKey();
				TreeNode nameNode = nameEntry.getValue(); 
				
				for (Entry<String, Object> entry : nameNode.getData().entrySet()) {
					buffer.append("Category=" + category + ", Name=" + name + ", Attribute=" +
							entry.getKey() + ", Value=" + entry.getValue() + "\n");
				}
			}
		}
		
		return buffer.toString();
	}

}
