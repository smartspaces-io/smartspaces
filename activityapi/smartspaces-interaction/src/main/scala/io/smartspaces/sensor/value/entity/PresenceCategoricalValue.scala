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

package io.smartspaces.sensor.value.entity

import io.smartspaces.data.entity.DynamicCategoricalValue
import io.smartspaces.data.entity.BaseCategoricalValueInstance
import io.smartspaces.data.entity.CategoricalValue
import io.smartspaces.data.entity.BaseCategoricalValue
import io.smartspaces.data.entity.CategoricalValueInstance

object Test {
  import PresenceCategoricalValueInstances._
  def main(args: Array[String]): Unit = {
    var foo = PresenceCategoricalValue.fromLabel("PRESENT")
    
    println(foo.get.id)
    test(foo.get)
    
    PresenceCategoricalValue.values.foreach { println(_) }
    
    var bar = DynamicCategoricalValue.fromLabels("foo", List("foo", "bar"))
    println(bar.fromLabel("bar"))
    println(bar.fromLabel("foo"))
    println(bar.fromLabel("fo"))
  }
  
  def test(foo: PresenceCategoricalValueInstance): Unit = {
    foo match {
      case PRESENT => println("is present")
      case NOT_PRESENT => println("not present")
    }
    
  }
}

/**
 * The presence categorical value.
 * 
 * @author Keith M. Hughes
 */
final object PresenceCategoricalValue extends BaseCategoricalValue[PresenceCategoricalValueInstances.PresenceCategoricalValueInstance]( 
    "presence", List(PresenceCategoricalValueInstances.PRESENT, PresenceCategoricalValueInstances.NOT_PRESENT)) {  
}

/**
 * All categorical value instances for whether an object is present or not.
 * 
 * @author Keith M. Hughes
 */
object PresenceCategoricalValueInstances {
  
  /**
   * Base class for the presence categorical variable instances.
   * 
   * @author Keith M. Hughes
   */
  sealed abstract class PresenceCategoricalValueInstance(override val id: Int, override val label: String) extends BaseCategoricalValueInstance {
    override val value: CategoricalValue[CategoricalValueInstance] = PresenceCategoricalValue
  }
  
  /**
   * Something is present.
   */
  final object PRESENT extends PresenceCategoricalValueInstance(0, "PRESENT")
  
  /**
   * Something is not present.
   */
  final object NOT_PRESENT extends PresenceCategoricalValueInstance(1, "NOT_PRESENT")
}

