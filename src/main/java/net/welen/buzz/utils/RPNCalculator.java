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

import java.util.Stack;

import org.apache.log4j.Logger;

/**
 * Reverse Polish Notation calculator
 * 
 * @author welle
 * 
 */
public class RPNCalculator {

	private static final Logger LOG = Logger.getLogger(RPNCalculator.class);
	
	public static double calculate(String expr) {
		Stack<Double> stack = new Stack<Double>();
		
		String pieces[] = expr.split(",");
		
		for (int i=0; i<pieces.length; i++) {
			String value = pieces[i].trim();			
			if (value.equals("+")) {
				Double lastValue = stack.pop();
				Double lastLastValue = stack.pop();				
				stack.push(lastLastValue + lastValue);
			} else if (value.equals("-")) {
				Double lastValue = stack.pop();
				Double lastLastValue = stack.pop();				
				stack.push(lastLastValue - lastValue);
			} else if (value.equals("*")) {		
				Double lastValue = stack.pop();
				Double lastLastValue = stack.pop();
				stack.push(lastLastValue * lastValue);
			} else if (value.equals("/")) {
				Double lastValue = stack.pop();
				Double lastLastValue = stack.pop();
				stack.push(lastLastValue / lastValue);
			} else {
				stack.push(Double.valueOf(pieces[i]));
			}			
		}

		double answer = stack.pop();
		if (LOG.isDebugEnabled()) {
			LOG.debug("Expression: " + expr + " = " + answer);
		}
		return answer;
	}
	
}
