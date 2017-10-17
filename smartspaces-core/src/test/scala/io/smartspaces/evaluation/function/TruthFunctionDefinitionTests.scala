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

package io.smartspaces.evaluation.function

import org.junit.Assert
import org.junit.Test
import org.scalatest.junit.JUnitSuite

import io.smartspaces.evaluation.FunctionCall

/**
 * A set of unit tests for various truth based function definitions.
 * 
 * @author Keith M. Hughes
 */
class TruthFunctionDefinitionTests extends JUnitSuite {
  
  /**
   * Test that equals correctly determines things are equal.
   */
  @Test def testEqualsEquals(): Unit = {
    val function = new EqualsFunctionDefinition()
    
    val functionCall = new FunctionCall()
    functionCall.functionName = "equals"
    functionCall.addArg("foo")
    functionCall.addArg("foo")
    
    Assert.assertTrue(function.evaluateFunctionCall(functionCall).asInstanceOf[Boolean])
  }
  
  /**
   * Test that equals correctly determines things aren't equal.
   */
  @Test def testEqualsNotEquals(): Unit = {
    val function = new EqualsFunctionDefinition()
    
    val functionCall = new FunctionCall()
    functionCall.functionName = "equals"
    functionCall.addArg("foo")
    functionCall.addArg(new Integer(1))
    
    Assert.assertFalse(function.evaluateFunctionCall(functionCall).asInstanceOf[Boolean])
  }
  
  /**
   * Test that allOf determines that all true is true.
   */
  @Test def testAllOfTrue(): Unit = {
    val function = new AllOfFunctionDefinition()
    
    val functionCall = new FunctionCall()
    functionCall.functionName = "allOf"
    functionCall.addArg(java.lang.Boolean.TRUE)
    functionCall.addArg("true")
    
    Assert.assertTrue(function.evaluateFunctionCall(functionCall).asInstanceOf[Boolean])
  }
  
  /**
   * Test that allOf is false if at least one item is false.
   */
  @Test def testAllOfFalse(): Unit = {
    val function = new AllOfFunctionDefinition()
    
    val functionCall = new FunctionCall()
    functionCall.functionName = "allOf"
    functionCall.addArg(java.lang.Boolean.TRUE)
    functionCall.addArg("false")
    
    Assert.assertFalse(function.evaluateFunctionCall(functionCall).asInstanceOf[Boolean])
  }
  
  /**
   * Test that noneOf determines that all false is true.
   */
  @Test def testNoneOfTrue(): Unit = {
    val function = new NoneOfFunctionDefinition()
    
    val functionCall = new FunctionCall()
    functionCall.functionName = "noneOf"
    functionCall.addArg(java.lang.Boolean.FALSE)
    functionCall.addArg("false")
    
    Assert.assertTrue(function.evaluateFunctionCall(functionCall).asInstanceOf[Boolean])
  }
  
  /**
   * Test that noneOf is false if at least one item is true.
   */
  @Test def testNoneOfFalse(): Unit = {
    val function = new AllOfFunctionDefinition()
    
    val functionCall = new FunctionCall()
    functionCall.functionName = "noneOf"
    functionCall.addArg(java.lang.Boolean.TRUE)
    functionCall.addArg("false")
    
    Assert.assertFalse(function.evaluateFunctionCall(functionCall).asInstanceOf[Boolean])
  }
  
  /**
   * Test that someOf determines that not all false is true.
   */
  @Test def testSomeOfTrue(): Unit = {
    val function = new SomeOfFunctionDefinition()
    
    val functionCall = new FunctionCall()
    functionCall.functionName = "someOf"
    functionCall.addArg(java.lang.Boolean.TRUE)
    functionCall.addArg("false")
    
    Assert.assertTrue(function.evaluateFunctionCall(functionCall).asInstanceOf[Boolean])
  }
  
  /**
   * Test that someOf is false if all are false.
   */
  @Test def testSomeOfFalse(): Unit = {
    val function = new SomeOfFunctionDefinition()
    
    val functionCall = new FunctionCall()
    functionCall.functionName = "someOf"
    functionCall.addArg(java.lang.Boolean.FALSE)
    functionCall.addArg("false")
    
    Assert.assertFalse(function.evaluateFunctionCall(functionCall).asInstanceOf[Boolean])
  }
  
  /**
   * Test that cond gives the value of the one item that is true.
   */
  @Test def testCondOneItem(): Unit = {
    val function = new CondFunctionDefinition()
    
    val functionCall = new FunctionCall()
    functionCall.functionName = "cond"
    functionCall.addArg(java.lang.Boolean.TRUE)
    functionCall.addArg("yada")
    functionCall.addArg("jabber")
    
    Assert.assertEquals("yada", function.evaluateFunctionCall(functionCall).asInstanceOf[String])
  }
  
  /**
   * Test that cond gives the value of the one item and the following item are both true,
   * but only first is chosen.
   */
  @Test def testCondTwoItems(): Unit = {
    val function = new CondFunctionDefinition()
    
    val functionCall = new FunctionCall()
    functionCall.functionName = "cond"
    functionCall.addArg(java.lang.Boolean.TRUE)
    functionCall.addArg("yada")
    functionCall.addArg(java.lang.Boolean.TRUE)
    functionCall.addArg("yada2")
    functionCall.addArg("jabber")
    
    Assert.assertEquals("yada", function.evaluateFunctionCall(functionCall).asInstanceOf[String])
  }
  
  /**
   * Test that cond gives the value of a central item because the things before it were false.
   */
  @Test def testCondSecondItem(): Unit = {
    val function = new CondFunctionDefinition()
    
    val functionCall = new FunctionCall()
    functionCall.functionName = "cond"
    functionCall.addArg(java.lang.Boolean.FALSE)
    functionCall.addArg("yada")
    functionCall.addArg(java.lang.Boolean.TRUE)
    functionCall.addArg("yada2")
    functionCall.addArg("jabber")
    
    Assert.assertEquals("yada2", function.evaluateFunctionCall(functionCall).asInstanceOf[String])
  }
  
  /**
   * Test that cond gives the value of the last item because all things before it were false.
   */
  @Test def testCondDefaultItem(): Unit = {
    val function = new CondFunctionDefinition()
    
    val functionCall = new FunctionCall()
    functionCall.functionName = "cond"
    functionCall.addArg(java.lang.Boolean.FALSE)
    functionCall.addArg("yada")
    functionCall.addArg(java.lang.Boolean.FALSE)
    functionCall.addArg("yada2")
    functionCall.addArg("jabber")
    
    Assert.assertEquals("jabber", function.evaluateFunctionCall(functionCall).asInstanceOf[String])
  }
 
}