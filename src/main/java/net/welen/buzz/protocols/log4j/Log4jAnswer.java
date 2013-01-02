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
package net.welen.buzz.protocols.log4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import net.welen.buzz.protocols.BuzzAnswer;
import net.welen.buzz.utils.TreeNode;

/**
 * Java structure of the Log4j protocol
 * 
 * @author welle
 */
public class Log4jAnswer extends BuzzAnswer {
	
	private static final Logger LOG = Logger.getLogger(Log4jAnswer.class);
	
	public List<String> getLogLines() {
		List<String> answer = new ArrayList<String>();
		
		boolean debug = LOG.isDebugEnabled();
		
		for (Entry<String, TreeNode> categoryEntry : data.getChildren().entrySet()) {
			String category = categoryEntry.getKey();
			TreeNode categoryNode = categoryEntry.getValue();

			for (Entry<String, TreeNode> nameEntry : categoryNode.getChildren().entrySet()) {
				String name = nameEntry.getKey();
				TreeNode nameNode = nameEntry.getValue(); 

				for (Entry<String, Object> entry : nameNode.getData().entrySet()) {
		
					if (debug) {
						LOG.debug("Adding Category=" + category + ", Name=" + name + ", Attribute=" +
								entry.getKey() + ", Value=" + entry.getValue());
					}
					answer.add("Category=" + category + ", Name=" + name + ", Attribute=" +
							entry.getKey() + ", Value=" + entry.getValue());
				}
			}
		}
		return answer;
	}
}
