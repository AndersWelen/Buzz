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
package net.welen.buzz.protocols.munin;

import static org.junit.Assert.*;

import net.welen.buzz.protocols.munin.MuninAnswer;

import org.junit.Test;



/**
 * JUnit testcase
 * 
 * @author welle
 */
public class MuninAnswerTest {

	@Test
	public void testConfig() {
		MuninAnswer config = new MuninAnswer();
		assertEquals(".\n", config.toString());
		
		config.put("Category", "TestGraph", "Test.Key Example", "TestValue");
		String expected = "multigraph TestGraph\nTest.Key_Example.value TestValue\n";
		assertEquals(expected + ".\n", config.toString());

		config.put("Category","TestGraph", "Test.Key2", "TestValue2");
		expected += "Test.Key2.value TestValue2\n";
		assertEquals(expected + ".\n", config.toString());

		config.put("Category","TestGraph2", "Test.Key3", "TestValue3");
		String expected2 = "multigraph TestGraph2\nTest.Key3.value TestValue3\n";
		assertTrue(config.toString().contains(expected));
		assertTrue(config.toString().contains(expected2));
		assertTrue(config.toString().endsWith(".\n"));
	}
	
	@Test
	public void testfixNameToMuninLimitations() {
		assertEquals("abc12", MuninAnswer.fixNameToMuninLimitations("abc12"));
		assertEquals("_22abc", MuninAnswer.fixNameToMuninLimitations("22abc"));
		assertEquals("ab_c", MuninAnswer.fixNameToMuninLimitations("ab¤c"));
		assertEquals("ab_c", MuninAnswer.fixNameToMuninLimitations("ab c"));
		assertEquals("ab_c", MuninAnswer.fixNameToMuninLimitations("ab\tc"));		
	}

}
