/*
 * Copyright (C) 2016 Keith M. Hughes
 * Copyright (C) 2012 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package io.smartspaces.evaluation;

/**
 * An {@link ExpressionEvaluator} which only evaluates variable expressions.
 *
 * @author Keith M. Hughes
 */
public class SimpleExpressionEvaluator extends BaseExpressionEvaluator {

  @Override
  public String evaluateStringExpression(String initial) {
    // I don't know if the short-circuit is needed, but will leave for now
    // and check by profiling  later.
    int exprPos = initial.indexOf("${");
    if (exprPos == -1) {
      return initial;
    } else {
      // Store the first part of the string that has no variables.
      StringBuffer buffer = new StringBuffer();

      // For now there will never be a ${ or } in the middle of an
      // expression.
      int endExpr = 0;
      do {
        buffer.append(initial.substring(endExpr, exprPos));
        exprPos += 2;

        endExpr = initial.indexOf("}", endExpr);
        if (endExpr == -1) {
          throw new EvaluationSmartSpacesException(String.format(
              "Expression in string doesn't end with }: %s", initial.substring(exprPos)));
        }

        String internalExpression = initial.substring(exprPos, endExpr);
        Object value = evaluateSymbolValue(internalExpression);
        if (value == null || value.equals(internalExpression))
          buffer.append("${$").append(internalExpression).append("}");
        else
          buffer.append(value.toString());

        endExpr++;
        exprPos = initial.indexOf("${", endExpr);
      } while (exprPos != -1);

      buffer.append(initial.substring(endExpr));

      return buffer.toString();
    }
  }

  /**
   * Evaluate a symbol value.
   *
   * @param expression
   *          the expression to evaluate in whatever expression language is
   *          being supported.
   *
   * @return The value of the expression.
   *
   * @throws EvaluationSmartSpacesException
   *           An evaluation error of some sort occurred.
   */
  private String evaluateSymbolValue(String expression) throws EvaluationSmartSpacesException {
    String rawValue = environment.lookupSymbolValue(expression.substring(1));
    return evaluateStringExpression(rawValue);
  }
}
