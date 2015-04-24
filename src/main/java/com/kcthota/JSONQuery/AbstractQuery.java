/**
 * 
 */
package com.kcthota.JSONQuery;

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.kcthota.JSONQuery.exceptions.MissingNodeException;
import com.kcthota.JSONQuery.expressions.AndExpression;
import com.kcthota.JSONQuery.expressions.ComparisonExpression;
import com.kcthota.JSONQuery.expressions.EqExpression;
import com.kcthota.JSONQuery.expressions.Expression;
import com.kcthota.JSONQuery.expressions.GtExpression;
import com.kcthota.JSONQuery.expressions.IsNullExpression;
import com.kcthota.JSONQuery.expressions.MultiExpression;
import com.kcthota.JSONQuery.expressions.NeExpression;
import com.kcthota.JSONQuery.expressions.NotExpression;
import com.kcthota.JSONQuery.expressions.OrExpression;

/**
 * @author Krishna Chaitanya Thota
 * Apr 23, 2015 11:40:06 PM
 */
public abstract class AbstractQuery extends AbstractQueryHelpers {
	
	protected JsonNode node;
	
	public AbstractQuery(JsonNode node) {
		this.node = node;
	}

	public abstract boolean is(Expression expr);
	
	public abstract JsonNode value(String property);
	
	public abstract boolean isExist(String property);
	
	/**
	 * Evaluates the passed expression on the JsonNode
	 * @param expr
	 * @param currentResult
	 * @return
	 */
	protected boolean is(Expression expr, Boolean currentResult) {
		// at any time if currentResult is false, stop and return false;
		if (currentResult != null && !currentResult)
			return false;

		try {
			if (expr instanceof ComparisonExpression) {
				ComparisonExpression castedExpr = (ComparisonExpression) expr;
				if (castedExpr instanceof EqExpression || castedExpr instanceof IsNullExpression) {
					return getValue(node, castedExpr.property()).equals(castedExpr.value());
				} else if (expr instanceof NeExpression) {
					return !getValue(node, castedExpr.property()).equals(castedExpr.value());
				} else if (expr instanceof GtExpression) {
					JsonNode propValue = getValue(node, castedExpr.property());
					return doGt(castedExpr, propValue);
				}
			} else if (expr instanceof MultiExpression) {
				MultiExpression castedExpr = (MultiExpression) expr;
				List<Expression> expressions = castedExpr.getExpressions();
				if (castedExpr instanceof AndExpression) {
					for (Expression curExpr : expressions) {
						currentResult = is(curExpr, currentResult);
						if (!currentResult) {
							return false;
						}
					}
					return true;
				} else if (castedExpr instanceof OrExpression) {
					for (Expression curExpr : expressions) {
						currentResult = is(curExpr, currentResult);
						if (currentResult) {
							return true;
						}

						// reset currentResult for Or
						currentResult = null;
					}
					return false;
				}
			} else if (expr instanceof NotExpression) {
				NotExpression castedExpr = (NotExpression) expr;
				return !is(castedExpr.getExpression(), null);
			}
		} catch (MissingNodeException e) {
			return false;
		}
		
		return false;
	}
	
	
	
	
	
	/**
	 * Fetches the value for a property from the passed JsonNode. Throws MissingNodeException if the property doesn't exist
	 * @param node
	 * @param property
	 * @return
	 */
	protected JsonNode getValue(JsonNode node, String property) {
		if(node==null) {
			throw new MissingNodeException(property + " is missing");
		}
		
		JsonNode value = null;
		int index = getNextIndex(property, 0);
		if (index < 0) {
			value = node.path(formatPropertyName(property));
			if (value.isMissingNode()) {
				throw new MissingNodeException(property + " is missing");
			}
			return value;
		} else {
			String propertyName = property.substring(0, property.indexOf('.'));
			return getValue(node.get(propertyName), property.substring(index + 1));
		}
	}
	
	/**
	 * Gets next Index of (dot) in the passed property name. ignores dots preceded by \ 
	 * @param property
	 * @param startIndex
	 * @return
	 */
	protected int getNextIndex(String property, int startIndex) {
		int index = property.indexOf('.',startIndex);
		if(index>0 && property.charAt(index-1)=='\\') {
			return getNextIndex(formatPropertyName(property), index);
		}
		return index;
	}
	
	/**
	 * Formats the property name. Example: converts a\.b to a.b.
	 * @param property
	 * @return
	 */
	protected String formatPropertyName(String property) {
		return property.replace("\\.", ".");
	}
}
