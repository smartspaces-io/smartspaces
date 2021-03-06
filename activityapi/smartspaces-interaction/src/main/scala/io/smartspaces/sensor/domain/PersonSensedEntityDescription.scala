/*
 * Copyright (C) 2016 Keith M. Hughes
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

package io.smartspaces.sensor.domain

/**
 * An entity description of a person.
 * 
 * @author Keith M. Hughes
 */
trait PersonSensedEntityDescription
    extends SensedEntityDescription with MarkableEntityDescription {

}

/**
 * The standard person entity description.
 *
 * @author Keith M. Hughes
 */
class SimplePersonSensedEntityDescription(
    id: String, externalId: String, 
    displayName: String, 
    displayDescription: Option[String]) extends SimpleEntityDescription(id, externalId, displayName, displayDescription)
    with PersonSensedEntityDescription {

  override def toString(): String = {
    "SimplePersonSensedEntityDescription [id=" + id + ", displayName=" +
      displayName + ", displayDescription=" + displayDescription + "]";
  }
}
