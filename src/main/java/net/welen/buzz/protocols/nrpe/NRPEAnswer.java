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
package net.welen.buzz.protocols.nrpe;

import java.util.Map.Entry;

import org.apache.log4j.Logger;

import net.welen.buzz.protocols.BuzzAnswer;
import net.welen.buzz.utils.TreeNode;

/**
 * @author welle
 *
 */
public class NRPEAnswer extends BuzzAnswer {
		
	private static final Logger LOG = Logger.getLogger(NRPEAnswer.class);
	
	public String toString() {
		StringBuffer buffer = new StringBuffer();

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
					buffer.append(category + DELIMITER + name + DELIMITER + entry.getKey() + "=" + entry.getValue());
				}
			}
		}
		
		return buffer.toString();
	}
	
}