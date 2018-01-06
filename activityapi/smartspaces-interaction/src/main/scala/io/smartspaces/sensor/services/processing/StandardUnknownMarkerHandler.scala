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

package io.smartspaces.sensor.services.processing

import scala.collection.mutable.HashSet
import scala.collection.mutable.Set
import io.smartspaces.sensor.event.UnknownEntitySeenEvent

/**
 * The standard handler for markers seen that are unknown.
 * 
 * @author Keith M. Hughes
 */
class StandardUnknownMarkerHandler(val eventEmitter: SensorProcessingEventEmitter) extends UnknownMarkerHandler {

  /**
   * The set of unknown marker IDs.
   */
  private val unknownMarkerIds: Set[String] = new HashSet[String]

  override def handleUnknownMarker(markerId: String, timestamp: Long): Unit = {
    synchronized {
      if (unknownMarkerIds.add(markerId)) {
        eventEmitter.broadcastUnknownMarkerSeenEvent(new UnknownEntitySeenEvent(markerId, timestamp))
      }
    }
  }

  override def removeUnknownMarkerId(markerId: String): Unit = {
    synchronized {
      unknownMarkerIds.remove(markerId)
    }
  }

  override def getAllUnknownMarkerIds(): scala.collection.immutable.Set[String] = {
    synchronized {
      unknownMarkerIds.toSet
    }
  }
}