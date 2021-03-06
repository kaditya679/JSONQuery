package com.kcthota.JSONQuery.expressions;

import com.fasterxml.jackson.databind.JsonNode;
/**
 * Evaluates Not Equal
 * @author Krishna Chaitanya Thota
 * Apr 26, 2015 12:03:07 AM
 */
public class NeExpression extends SimpleComparisonExpression {

	public NeExpression(ValueExpression expression, JsonNode value) {
		super(expression, value);
	}
	
	@Override
	public boolean evaluate(JsonNode node) {
		return !expression().evaluate(node).equals(value());
	}
}
