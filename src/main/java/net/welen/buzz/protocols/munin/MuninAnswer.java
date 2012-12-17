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
package net.welen.buzz.protocols.munin;

import java.util.Map.Entry;

import net.welen.buzz.protocols.BuzzAnswer;
import net.welen.buzz.utils.TreeNode;

import org.apache.log4j.Logger;

/**
 * Java structure of the Munin protocol answers using
 * the "cap multigraph" approach.
 * 
 * @author welle
 */
public class MuninAnswer extends BuzzAnswer {

	private static final Logger LOG = Logger.getLogger(MuninAnswer.class);
			
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuffer answer = new StringBuffer();
			
		boolean debug = LOG.isDebugEnabled();

		for (Entry<String, TreeNode> categoryEntry : data.getChildren().entrySet()) {
			TreeNode categoryNode = categoryEntry.getValue();
			
			for (Entry<String, TreeNode> nameEntry : categoryNode.getChildren().entrySet()) {
				String name = nameEntry.getKey();
				TreeNode nameNode = nameEntry.getValue(); 

				String multigraphName = fixNameToMuninLimitations(name);
				answer.append("multigraph " + multigraphName + "\n");

				for (Entry<String, Object> entry : nameNode.getData().entrySet()) {
					if (debug) {
						LOG.debug("Adding: " + entry.getKey().replaceAll("\\s", "_") + ".value " + entry.getValue().toString().replaceAll("%n", multigraphName) + "\n");
					}
					
					// Change boolean values to numeric as Munin can't handle "true"/"false"
					if (entry.getValue() instanceof Boolean) {
						if ((Boolean) entry.getValue()) {
							entry.setValue("1");
						} else {
							entry.setValue("0");
						}
					}
					
					// Munin doesn't allow spaces in attributes
					answer.append(entry.getKey().replaceAll("\\s", "_") + ".value " + entry.getValue().toString().replaceAll("%n", multigraphName) + "\n");
				}
			}
		}
		answer.append(".\n");		

		if (debug) {
			LOG.debug("Munin config:\n" + answer.toString());
		}
		return answer.toString();
	}

	/**
	 * Utility method used to make sure that the naming standard
	 * of Munin is implemented 
	 * <p>
	 * See: http://munin.projects.linpro.no/wiki/notes_on_datasource_names
	 * 
	 * @param name
	 * @return A fixed Munin string
	 */
	protected static String fixNameToMuninLimitations(String name) {
		return name.replaceAll("%..", "_").replaceAll("^([^A-Za-z_])", "_$1").replaceAll("[^A-Za-z0-9_]", "_");
	}

	public void remove(String category, String unit) {
		TreeNode node = data.getChild(new String[]{category, unit});
		if (node != null) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("Removing: " + category + " " + unit);
			}
			node.getParent().removeChild(unit);
		}
	}

}
