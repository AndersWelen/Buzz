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
package net.welen.buzz.utils;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class TreeNodeTest {
		
	@Test
	public void testNoParent() {
		TreeNode treeNode = new TreeNode(null, "root");
		
		assertNull(treeNode.getParent());
	}

	@Test
	public void testParent() {
		TreeNode parent = new TreeNode(null, "root");
		TreeNode treeNode = new TreeNode(parent, "child");
		
		assertEquals(parent, treeNode.getParent());
	}

	@Test
	public void testName() {
		TreeNode treeNode = new TreeNode(null, "root");
		
		assertEquals("root", treeNode.getName());
	}

	@Test
	public void testNoChildren() {
		TreeNode treeNode = new TreeNode(null, "root");
		
		assertEquals(0, treeNode.getChildren().size());
	}

	@Test
	public void testChildren() {
		TreeNode parent = new TreeNode(null, "root");
		TreeNode treeNode1 = new TreeNode(parent, "child1");
		TreeNode treeNode2 = new TreeNode(parent, "child2");
		TreeNode treeNode3 = new TreeNode(treeNode1, "child3");
		parent.addChild(treeNode1);
		assertEquals(1, parent.getChildren().size());
		parent.addChild(treeNode2);
		assertEquals(2, parent.getChildren().size());
		
		assertEquals(treeNode1, parent.getChild("child1"));
		assertEquals(treeNode2, parent.getChild("child2"));
		assertNull(parent.getChild("noChild"));
		
		treeNode1.addChild(treeNode3);
		String[] path1 = {"child1"};
		String[] path2 = {"child1", "child3"};
		String[] path3 = {"child1", "noChild"};
		assertEquals(treeNode1, parent.getChild(path1));
		assertEquals(treeNode3, parent.getChild(path2));
		assertNull(parent.getChild(path3));
	}

	@Test
	public void testGetOrCreateChild() {
		TreeNode parent = new TreeNode(null, "root");
		
		assertEquals(0, parent.getChildren().size());
		String[] path = {"child1"};
		parent.getOrCreateChild(path);
		assertEquals(1, parent.getChildren().size());
	}

	@Test
	public void testdata() {
		TreeNode parent = new TreeNode(null, "root");
		
		assertEquals(0, parent.getData().size());
		Map<String, Object> map = new HashMap<String, Object>();
		parent.setData(map);
		assertEquals(0, parent.getData().size());
		map.put("Data", "data");
		assertEquals(1, parent.getData().size());
	}

	@Test
	public void testRemoveChild() {
		TreeNode parent = new TreeNode(null, "root");
		TreeNode childNode = new TreeNode(parent, "child1");
		
		parent.addChild(childNode);
		assertEquals(1, parent.getChildren().size());
		parent.removeChild("child1");
		assertEquals(0, parent.getChildren().size());
	}
	
}