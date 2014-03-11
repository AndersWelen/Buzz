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
package net.welen.buzz.protocols;

import junit.framework.Assert;

import org.junit.Test;



/**
 * JUnit testcase
 * 
 * @author welle
 */
public class BuzzAnswerTest {
	
	@Test
	public void testAnswer() {
		BuzzAnswer answer = new BuzzAnswer();
		Assert.assertNull(answer.getIndividualValue("category", "name", "attribute"));
		answer.put("category", "name", "attribute", "value");
		Assert.assertEquals("value", answer.getIndividualValue("category", "name", "attribute"));
		Assert.assertEquals(1, answer.size());
		Assert.assertEquals("Category=category, Name=name, Attribute=attribute, Value=value\n", answer.toString());
	}
	
}
