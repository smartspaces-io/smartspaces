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

package io.smartspaces.service

import java.util.Collections
import java.util.Map

/**
 * The standard implemntation of a service description.
 * 
 * @author Keith M. Hughes
 */
class StandardServiceDescription(override val name: String, override val metadata: Map[String, Object]) extends ServiceDescription with Equals {
  
  /**
   * Construct a description without metadata.
   */
  def this(name: String) = {
    this(name, Collections.emptyMap())
  }

  def canEqual(other: Any) = {
    other.isInstanceOf[io.smartspaces.service.StandardServiceDescription]
  }

  override def equals(other: Any) = {
    other match {
      case that: StandardServiceDescription => that.canEqual(StandardServiceDescription.this) && name == that.name && metadata == that.metadata
      case _ => false
    }
  }

  override def hashCode() = {
    val prime = 41
    prime * (prime + name.hashCode) + metadata.hashCode
  }
}