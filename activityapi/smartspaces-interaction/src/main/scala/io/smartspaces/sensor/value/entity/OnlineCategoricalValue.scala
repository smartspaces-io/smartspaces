/*
 * Copyright (C) 2018 Keith M. Hughes
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


import io.smartspaces.data.entity.BaseCategoricalValue
import io.smartspaces.data.entity.BaseCategoricalValueInstance
import io.smartspaces.data.entity.CategoricalValue
import io.smartspaces.data.entity.CategoricalValueInstance

/**
 * The online categorical value.
 * 
 * @author Keith M. Hughes
 */
final object OnlineCategoricalValue extends BaseCategoricalValue[OnlineCategoricalValueInstances.OnlineCategoricalValueInstance]( 
    "online", List(OnlineCategoricalValueInstances.ONLINE, OnlineCategoricalValueInstances.OFFLINE)) {  
}

/**
 * All categorical value instances for whether an object is present or not.
 * 
 * @author Keith M. Hughes
 */
object OnlineCategoricalValueInstances {
  
  /**
   * Base class for the presence categorical variable instances.
   * 
   * @author Keith M. Hughes
   */
  sealed abstract class OnlineCategoricalValueInstance(override val id: Int, override val label: String) extends BaseCategoricalValueInstance {
    override def value: CategoricalValue[CategoricalValueInstance] = OnlineCategoricalValue
  }
  
  /**
   * Something is online.
   */
  final object ONLINE extends OnlineCategoricalValueInstance(0, "ONLINE")
  
  /**
   * Something is not online.
   */
  final object OFFLINE extends OnlineCategoricalValueInstance(1, "OFFLINE")
}
