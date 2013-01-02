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

import org.junit.Test;

public class RPNCalculatorTest {
		
	@Test
	public void testNoToCaclulate() {
		assertTrue((double) 10 == RPNCalculator.calculate("10"));
	}

	@Test
	public void testAddition() {
		assertEquals((double) 10, RPNCalculator.calculate("8,2,+"), 0);
		assertEquals((double) 10, RPNCalculator.calculate("4,4,+,2,+"), 0);
		assertEquals((double) 10, RPNCalculator.calculate("2,2,+,4,+,2,+"), 0);
	}

	@Test
	public void testSubstraction() {
		assertEquals((double) 10, RPNCalculator.calculate("12,2,-"), 0);
		assertEquals((double) 10, RPNCalculator.calculate("16,4,-,2,-"), 0);
		assertEquals((double) 10, RPNCalculator.calculate("18,2,-,4,-,2,-"), 0);
	}

	@Test
	public void testMultiplication() {
		assertEquals((double) 10, RPNCalculator.calculate("5,2,*"), 0);
		assertEquals((double) 20, RPNCalculator.calculate("5,2,*,2,*"), 0);
		assertEquals((double) 200, RPNCalculator.calculate("5,2,*,2,*,10,*"), 0);
	}

	@Test
	public void testDevision() {
		assertEquals((double) 10, RPNCalculator.calculate("20,2,/"), 0);
		assertEquals((double) 5, RPNCalculator.calculate("20,2,/,2,/"), 0);
		assertEquals((double) 2.5, RPNCalculator.calculate("20,2,/,2,/,2,/"), 0);
	}

	@Test
	public void testMixed() {
		assertEquals((double) 10, RPNCalculator.calculate("10,8,+,3,-,2,*,3,/"), 0);
	}

	@Test
	public void testPushed() {
		assertEquals((double) 2000, RPNCalculator.calculate("10,2,*,10,10,*,*"), 0);
	}

}
