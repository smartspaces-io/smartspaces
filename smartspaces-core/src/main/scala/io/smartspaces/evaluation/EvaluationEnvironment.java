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
 * An environment for evaluating expressions.
 *
 * @author Keith M. Hughes
 */
public interface EvaluationEnvironment extends SymbolTable<String> {
	
	/**
	 * Clear all symbol tables from the environment.
	 * 
	 * @return this environment
	 */
	EvaluationEnvironment clearSymbolTables();

	/**
	 * Add a symbol table to the evaluation environment. It is added at the end.
	 * 
	 * @param symbolTable
	 *            the symbol table to add
	 * 
	 * @return this environment
	 */
	EvaluationEnvironment addSymbolTable(SymbolTable<String> symbolTable);

    /**
     * Add a symbol table to the evaluation environment. It is added to the front of the tables.
     * 
     * @param symbolTable
     *            the symbol table to add
     * 
     * @return this environment
     */
    EvaluationEnvironment addSymbolTableFront(SymbolTable<String> symbolTable);

	/**
	 * Evaluate a function call.
	 * 
	 * @param functionCall
	 *            the function call to evaluate
	 * 
	 * @return the value of the call
	 * 
	 * @throws EvaluationSmartSpacesException
	 *             something bad happened during evaluation
	 */
	Object evaluateFunctionCall(FunctionCall functionCall) throws EvaluationSmartSpacesException;

	/**
	 * Add a function definition.
	 * 
	 * @param functionDefinition
	 *            the function definition to add
	 * 
	 * @return this environment
	 */
	EvaluationEnvironment addFunctionDefinition(FunctionDefinition functionDefinition);
}
