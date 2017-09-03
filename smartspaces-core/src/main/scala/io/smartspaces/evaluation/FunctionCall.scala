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

package io.smartspaces.evaluation

import scala.collection.mutable.ListBuffer

/**
 * A function call to be evaluated.
 * 
 * @author Keith M. Hughes
 */
class FunctionCall {
  
  /**
   * The name of the function.
   */
  var functionName: String = _
  
  /**
   * The arguments in the function call.
   */
  private val args: ListBuffer[Any] = new ListBuffer
  
  /**
   * Add in a new argument.
   * 
   * @param arg
   *         the new argument
   */
  def addArg(arg: Any): FunctionCall = {
    args.append(arg)
    
    this
  }
  
  override def toString(): String = {
    s"FunctionCall[ functionName=${functionName}, args=${args}]"
  }
}