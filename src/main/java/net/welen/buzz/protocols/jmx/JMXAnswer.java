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
package net.welen.buzz.protocols.jmx;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import net.welen.buzz.protocols.BuzzAnswer;
import net.welen.buzz.utils.TreeNode;

/**
 * @author welle
 *
 */
public class JMXAnswer extends BuzzAnswer {
	
	private static final Logger LOG = Logger.getLogger(JMXAnswer.class);
	
	public TreeNode getPayload() {
		return data;
	}
	
	public void setPayload(TreeNode payload) {
		data = payload;
	}

	/**
	 * Get all data as a Map
	 */
	public Map<String, Object> getMap() {
		Map<String, Object> answer = new HashMap<String, Object>();
		
		boolean debug = LOG.isDebugEnabled();
		
		for (Entry<String, TreeNode> categoryEntry : data.getChildren().entrySet()) {
			String category = categoryEntry.getKey();
			TreeNode categoryNode = categoryEntry.getValue();
			
			for (Entry<String, TreeNode> nameEntry : categoryNode.getChildren().entrySet()) {
				String name = nameEntry.getKey();
				TreeNode nameNode = nameEntry.getValue(); 

				for (Entry<String, Object> entry : nameNode.getData().entrySet()) {
					if (debug) {
						LOG.debug("Adding: " + category + DELIMITER + name + DELIMITER + entry.getKey() + "=" + entry.getValue());
					}
					answer.put(category + DELIMITER + name + DELIMITER + entry.getKey(), entry.getValue());
				}
			}
		}

		return answer;
	}

}