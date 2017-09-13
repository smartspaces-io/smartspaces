/*
 * Copyright (C) 2017 Keith M. Hughes
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

package io.smartspaces.expression.language.ssel;

import io.smartspaces.SmartSpacesException;
import io.smartspaces.evaluation.BaseExpressionEvaluator;
import io.smartspaces.evaluation.EvaluationSmartSpacesException;
import io.smartspaces.evaluation.FunctionCall;
import io.smartspaces.evaluation.SimpleEvaluationEnvironment;
import io.smartspaces.evaluation.function.ConcatStringFunctionDefinition;
import io.smartspaces.evaluation.function.ReplaceAllStringFunctionDefinition;
import io.smartspaces.expression.language.ssel.SmartspacesexpressionlanguageParserParser.ExpressionContext;
import io.smartspaces.expression.language.ssel.SmartspacesexpressionlanguageParserParser.FunctionArgumentContext;
import io.smartspaces.expression.language.ssel.SmartspacesexpressionlanguageParserParser.FunctionCallContext;
import io.smartspaces.expression.language.ssel.SmartspacesexpressionlanguageParserParser.FunctionNameContext;
import io.smartspaces.expression.language.ssel.SmartspacesexpressionlanguageParserParser.IntegerContext;
import io.smartspaces.expression.language.ssel.SmartspacesexpressionlanguageParserParser.StringContext;
import io.smartspaces.expression.language.ssel.SmartspacesexpressionlanguageParserParser.SymbolContext;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.TokenStream;

import java.util.Stack;

/**
 * An evaluator for the Smart Spaces Expression Language.
 * 
 * @author Keith M. Hughes
 */
public class SselExpressionEvaluator extends BaseExpressionEvaluator {

  public static void main(String[] args) {
    SimpleEvaluationEnvironment env = new SimpleEvaluationEnvironment();
    env.setSymbolValue("a.b.c", "45dbf5e6-5cf3-11e7-953d-c48e8ff54e35");
    env.addFunctionDefinition(new ConcatStringFunctionDefinition());
    env.addFunctionDefinition(new ReplaceAllStringFunctionDefinition());

    SselExpressionEvaluator evaluator = new SselExpressionEvaluator();
    evaluator.setEvaluationEnvironment(env);

    String result = evaluator.evaluateStringExpression(
        " foo ${concat(\"s\", replaceAll($a.b.c, \"-\", \"_s\"))} bar ${$a.b.c}");
    System.out.println(result);
  }

  @Override
  public String evaluateStringExpression(String expression) {
    // I don't know if the short-circuit is needed, but will leave for now
    // and check by profiling later.
    int exprPos = expression.indexOf("${");
    if (exprPos == -1) {
      return expression;
    } else {
      // Store the first part of the string that has no variables.
      StringBuffer buffer = new StringBuffer();

      // For now there will never be a ${ or } in the middle of an
      // expression.
      int endExpr = 0;
      do {
        buffer.append(expression.substring(endExpr, exprPos));
        exprPos += 2;

        endExpr = expression.indexOf("}", endExpr);
        if (endExpr == -1) {
          throw new EvaluationSmartSpacesException(String.format(
              "Expression in string doesn't end with }: %s", expression.substring(exprPos)));
        }

        String internalExpression = expression.substring(exprPos, endExpr);
        Object value = evaluateSselExpression(internalExpression);
        if (value == null || value.equals(expression))
          buffer.append("${").append(internalExpression).append("}");
        else
          buffer.append(value.toString());

        endExpr++;
        exprPos = expression.indexOf("${", endExpr);
      } while (exprPos != -1);

      buffer.append(expression.substring(endExpr));

      return buffer.toString();
    }
  }

  /**
   * Evaluate an SSEL expression.
   * 
   * @param expression
   *          the expression
   * 
   * @return the value
   */
  private Object evaluateSselExpression(String expression) {
    ANTLRInputStream inputStream = new ANTLRInputStream(expression);

    SmartspacesexpressionlanguageParserLexer lexer =
        new SmartspacesexpressionlanguageParserLexer(inputStream);
    TokenStream tokenStream = new CommonTokenStream(lexer);
    SmartspacesexpressionlanguageParserParser parser =
        new SmartspacesexpressionlanguageParserParser(tokenStream);

    ExpressionContext expressionContext = parser.expression();

    if (expressionContext.exception == null) {
      ExpressionVisitor visitor = new ExpressionVisitor();
      Object value = visitor.visit(expressionContext);
      return value.toString();
    } else {
      throw new SmartSpacesException("Could not parse expression " + expression,
          expressionContext.exception);
    }
  }

  /**
   * Evaluate a symbol value.
   *
   * @param symbolName
   *          the expression to evaluate in whatever expression language is
   *          being supported.
   *
   * @return The value of the expression.
   *
   * @throws EvaluationSmartSpacesException
   *           An evaluation error of some sort occurred.
   */
  private String evaluateSymbolValue(String symbolName) throws EvaluationSmartSpacesException {
    String rawValue = environment.lookupSymbolValue(symbolName);
    if (rawValue != null) {
      return evaluateStringExpression(rawValue);
    } else {
      return null;
    }
  }

  /**
   * The ANTLR 4 visitor that walks an expression tree.
   * 
   * @author Keith M. Hughes
   */
  private class ExpressionVisitor extends SmartspacesexpressionlanguageParserBaseVisitor<Object> {

    /**
     * The stack of function calls.
     */
    private Stack<FunctionCall> functions = new Stack<>();

    @Override
    public Object visitSymbol(SymbolContext context) {

      String symbolName = context.getText().substring(1);

      return evaluateSymbolValue(symbolName);
    }

    @Override
    public Object visitFunctionCall(FunctionCallContext context) {

      functions.push(new FunctionCall());

      visitChildren(context);

      FunctionCall builtCall = functions.pop();
      Object result = getEvaluationEnvironment().evaluateFunctionCall(builtCall);

      return result;
    }

    @Override
    public Object visitFunctionName(FunctionNameContext context) {

      String functionName = context.getText();

      functions.peek().functionName_$eq(functionName);

      return null;
    }

    @Override
    public Object visitFunctionArgument(FunctionArgumentContext context) {

      functions.peek().addArg(visitChildren(context));

      return null;
    }

    @Override
    public Object visitInteger(IntegerContext context) {
      return Long.parseLong(context.getText());
    }

    @Override
    public Object visitString(StringContext context) {
      String value = context.getText();
      value = value.substring(1);
      value = value.substring(0, value.length() - 1);

      return value;
    }
  }
}
