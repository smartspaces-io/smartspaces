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

package io.smartspaces.expression.language.ssel

import io.smartspaces.evaluation.SimpleEvaluationEnvironment

import org.junit.Test
import org.scalatest.junit.JUnitSuite

/**
 * Test for the SmartSpaces Expression Language evaluator.
 */
class SselExpressionEvaluatorTest  extends JUnitSuite {
  
  @Test def testEvaluation(): Unit = {
    val value = "glorp"
    val env = new SimpleEvaluationEnvironment
    env.setSymbolValue("a.b.c", value)
    val evaluator = new SselExpressionEvaluator()
    evaluator.setEvaluationEnvironment(env)
    
    val result = evaluator.evaluateStringExpression("$a.b.c")
    println(result)
  }
}
