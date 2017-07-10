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

import io.smartspaces.data.entity.BaseCategoricalValue
import io.smartspaces.data.entity.BaseCategoricalValueInstance
import io.smartspaces.data.entity.CategoricalValue
import io.smartspaces.data.entity.CategoricalValueInstance


/**
 * The activity categorical value.
 * 
 * @author Keith M. Hughes
 */
final object ActiveCategoricalValue extends BaseCategoricalValue[ActiveCategoricalValueInstances.ActiveCategoricalValueInstance]( 
    "active", List(ActiveCategoricalValueInstances.ACTIVE, ActiveCategoricalValueInstances.INACTIVE)) {  
}

/**
 * All categorical value instances for whether an object is present or not.
 * 
 * @author Keith M. Hughes
 */
object ActiveCategoricalValueInstances {
  
  /**
   * Base class for the presence categorical variable instances.
   * 
   * @author Keith M. Hughes
   */
  sealed abstract class ActiveCategoricalValueInstance(override val id: Int, override val label: String) extends BaseCategoricalValueInstance {
    override val value: CategoricalValue[CategoricalValueInstance] = ActiveCategoricalValue
  }
  
  /**
   * Something is present.
   */
  final object ACTIVE extends ActiveCategoricalValueInstance(0, "ACTIVE")
  
  /**
   * Something is not present.
   */
  final object INACTIVE extends ActiveCategoricalValueInstance(1, "INACTIVE")
}

