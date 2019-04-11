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

package io.smartspaces.resource.managed

/**
 * An initializer that confirms if initialization should happen.
 * 
 * @author Keith M. Hughes
 */
trait ConditionalInitializer[T] {
  
  /**
   * Should initialization take place?
   * 
   * @param context
   *        the object against which initialization should happen
   *        
   * @return {@code true} if initialization should happen
   */
  def shouldInitialize(context: T): Boolean
  
  /**
   * Perform the initialization.
   * 
   * @param context
   *        the object against which initialization should happen
   */
  def initialize(context: T): Unit
  
  /**
   * Display an error from the initializer.
   * 
   * @param context
   *        the object against which initialization should happen
   * @param e
   *        the error
   */
  def displayError(context: T, e: Throwable): Unit
}

/**
  * A mixin for conditional initializers.
  *
  * @tparam T
  *
  * @author Keith M. Hughes
  */
trait ConditionalInitializerMixin[T] {

  /**
    * Run the initializers.
    *
    * @param initializers
    *        the initializers to run if needed
    */
    def runInitializers(initializers: ConditionalInitializer[T]*): Unit
}

/**
  * A base mixin for conditional initializers.
  *
  * @tparam T
  *
  * @author Keith M. Hughes
  */
trait BaseConditionalInitializerMixin[T] extends ConditionalInitializerMixin[T] {
  override def runInitializers(initializers: ConditionalInitializer[T]*): Unit = {
    if (initializers != null) {
      initializers.foreach { initializer =>
        try {
          if (initializer.shouldInitialize(this.asInstanceOf[T])) {
            initializer.initialize(this.asInstanceOf[T])
          }
        } catch {
          case e: Throwable =>
            initializer.displayError(this.asInstanceOf[T], e)
        }
      }
    }
  }
}
