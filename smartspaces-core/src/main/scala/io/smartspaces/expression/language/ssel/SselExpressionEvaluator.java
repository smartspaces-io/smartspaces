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
import io.smartspaces.evaluation.EvaluationEnvironment;
import io.smartspaces.evaluation.FunctionCall;
import io.smartspaces.evaluation.SimpleEvaluationEnvironment;
import io.smartspaces.expression.language.ssel.SmartspacesexpressionlanguageParserParser.ExpressionContext;
import io.smartspaces.expression.language.ssel.SmartspacesexpressionlanguageParserParser.FunctionArgumentContext;
import io.smartspaces.expression.language.ssel.SmartspacesexpressionlanguageParserParser.FunctionCallContext;
import io.smartspaces.expression.language.ssel.SmartspacesexpressionlanguageParserParser.FunctionNameContext;
import io.smartspaces.expression.language.ssel.SmartspacesexpressionlanguageParserParser.IntegerContext;
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
public class SselExpressionEvaluator {

  public static void main(String[] args) {
    SimpleEvaluationEnvironment env = new SimpleEvaluationEnvironment();
    env.set("a.b.c", "glorp");
    SselExpressionEvaluator evaluator = new SselExpressionEvaluator(env);

    Object result = evaluator.evaluate("foo($a.b.c,1)");
    System.out.println(result);
  }

  private EvaluationEnvironment evaluationEnvironment;
  
  
  public SselExpressionEvaluator(EvaluationEnvironment evaluationEnvironment) {
    this.evaluationEnvironment = evaluationEnvironment;
  }

  public Object evaluate(String expression) {
    ANTLRInputStream inputStream = new ANTLRInputStream(expression);

    SmartspacesexpressionlanguageParserLexer lexer =
        new SmartspacesexpressionlanguageParserLexer(inputStream);
    TokenStream tokenStream = new CommonTokenStream(lexer);
    SmartspacesexpressionlanguageParserParser parser =
        new SmartspacesexpressionlanguageParserParser(tokenStream);

    ExpressionContext expressionContext = parser.expression();

    if (expressionContext.exception == null) {

      ExpressionVisitor visitor = new ExpressionVisitor();
      return visitor.visit(expressionContext);
    } else {
      throw new SmartSpacesException("Could not parse expression " + expression,
          expressionContext.exception);
    }
  }

  class ExpressionVisitor extends SmartspacesexpressionlanguageParserBaseVisitor<Object> {
    
    /**
     * The stack of function calls.
     */
    private Stack<FunctionCall> functions = new Stack<>();
    
    @Override
    public Object
        visitSymbol(SymbolContext context) {
      
      String variableName = context.getText().substring(1);
      System.out.println("Looking up " + variableName);
      
      return evaluationEnvironment.lookupVariableValue(variableName);
    }
    
    @Override
    public Object visitFunctionCall(FunctionCallContext context) {
      
      functions.push(new FunctionCall());
      
      visitChildren(context);
      
      FunctionCall builtCall = functions.pop();
      System.out.println(builtCall);
      
      return null;
    }
    
    @Override
    public Object visitFunctionName(FunctionNameContext context) {
      
      String functionName = context.getText();
      System.out.println("Function name " + functionName);
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
  }
}
