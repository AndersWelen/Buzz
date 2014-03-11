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
package net.welen.buzz.utils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * A simple tree structure
 * 
 * @author welle
 */
public class TreeNode {
	
	private String name;
	private Map<String, Object> data = new HashMap<String, Object>();
	private TreeNode parent = null;
	private Map<String, TreeNode> children = new HashMap<String, TreeNode>();
	
	public TreeNode(TreeNode parent, String name) {
		this.parent = parent;
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public TreeNode getParent() {
		return parent;
	}
	
	public Map<String, TreeNode> getChildren() {
		return Collections.unmodifiableMap(children);
	}
	
	public TreeNode getChild(String name) {
		return children.get(name);
	}
	
	public TreeNode getChild(String path[]) {
		TreeNode node = this;
		for (int i=0; i<path.length; i++) {
			node = node.getChild(path[i]);
			if (node == null) {
				return null;
			}
		}
		return node;
	}
	
	public TreeNode getOrCreateChild(String path[]) {
		TreeNode node = this;
		TreeNode child = null;
		for (int i=0; i<path.length; i++) {
			child = node.getChild(path[i]);
			if (child == null) {
				child = new TreeNode(node, path[i]);
				node.addChild(child);
			}
			node = child;
		}
		return node;
	}
	
	public void addChild(TreeNode child) {
		child.parent = this;
		children.put(child.getName(), child);
	}

	public Map<String, Object> getData() {
		return data;
	}

	public void setData(Map<String, Object> data) {
		this.data = data;
	}

	public void removeChild(String name) {
		TreeNode child = getChild(name);
		if (child != null) {
			child.parent = null;
			children.remove(name);
		}
	}

}
