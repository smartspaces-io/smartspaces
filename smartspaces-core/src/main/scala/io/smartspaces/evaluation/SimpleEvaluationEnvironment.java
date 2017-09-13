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

import java.util.ArrayList;
import java.util.List;

/**
 * A simple implemenation of an {@link EvaluationEnvironment}.
 *
 * @author Keith M. Hughes
 */
public class SimpleEvaluationEnvironment implements EvaluationEnvironment {

  /**
   * The symbol tables that have been added to the evaluation environment.
   */
  private List<SymbolTable<String>> symbolTables = new ArrayList<>();

  /**
   * The root symbol table.
   */
  private SymbolTable<String> rootSymbolTable;

  /**
   * The function definitions for this environment.
   */
  private List<FunctionDefinition> functionDefinitions = new ArrayList<>();

  /**
   * Construct a new environment.
   */
  public SimpleEvaluationEnvironment() {
    rootSymbolTable = new SimpleSymbolTable<>();

    symbolTables.add(rootSymbolTable);
  }

  @Override
  public EvaluationEnvironment addSymbolTable(SymbolTable<String> symbolTable) {

    symbolTables.add(symbolTable);

    return this;
  }

  @Override
  public String lookupSymbolValue(String symbolName) {
    for (SymbolTable<String> symbolTable : symbolTables) {
      String value = symbolTable.lookupSymbolValue(symbolName);
      if (value != null) {
        return value;
      }
    }

    return null;
  }

  @Override
  public void setSymbolValue(String symbolName, String value) {
    rootSymbolTable.setSymbolValue(symbolName, value);
  }

  @Override
  public Object evaluateFunctionCall(FunctionCall functionCall)
      throws EvaluationSmartSpacesException {
    FunctionDefinition functionDefinition = findFunctionDefintionForCall(functionCall);
    if (functionDefinition != null) {
      return functionDefinition.evaluateFunctionCall(functionCall);
    } else {
      throw new EvaluationSmartSpacesException(
          "Could not fund function definition for function " + functionCall.functionName());
    }
  }

  @Override
  public EvaluationEnvironment addFunctionDefinition(FunctionDefinition functionDefinition) {
    functionDefinitions.add(functionDefinition);

    return this;
  }

  /**
   * Find a function definition for the current function.
   * 
   * @param functionCall
   *          the function call
   *          
   * @return an appropriate function definition, or {@code null} if none found
   */
  private FunctionDefinition findFunctionDefintionForCall(FunctionCall functionCall) {
    for (FunctionDefinition functionDefinition : functionDefinitions) {
      if (functionDefinition.canEvaluateCall(functionCall)) {
        return functionDefinition;
      }
    }

    return null;
  }
}
