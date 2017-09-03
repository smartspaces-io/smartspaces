package io.smartspaces.expression.language.ssel

import io.smartspaces.evaluation.SimpleEvaluationEnvironment

import org.junit.Test
import org.scalatest.junit.JUnitSuite

class SselExpressionEvaluatorTest  extends JUnitSuite {
  
  @Test def testEvaluation(): Unit = {
    val value = "glorp"
    val env = new SimpleEvaluationEnvironment
    env.set("a.b.c", value)
    val evaluator = new SselExpressionEvaluator(env)
    
    val result = evaluator.evaluate("$a.b.c")
    println(result)
  }
}
