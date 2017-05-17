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

package io.smartspaces.sensor.processing

import io.smartspaces.sensor.entity.model.event.SensorOfflineEvent
import io.smartspaces.sensor.entity.model.event.UnknownMarkerSeenEvent
import io.smartspaces.sensor.entity.model.event.PhysicalSpaceOccupancyLiveEvent

/**
 * An emitter of events from sensor processors.
 * 
 * @author Keith M. Hughes
 */
trait SensorProcessingEventEmitter {
  
  /**
   * Broadcast a physical location occupancy event.
   *
   * @param event
   * 		the event to broadcast
   */
  def broadcastOccupanyEvent(event: PhysicalSpaceOccupancyLiveEvent): Unit

  /**
   * Broadcast a sensor offline event.
   *
   * @param event
   * 		the event to broadcast
   */
  def broadcastSensorOfflineEvent(event: SensorOfflineEvent): Unit

  /**
   * Broadcast a sensor offline event.
   *
   * @param event
   * 		the event to broadcast
   */
  def broadcastUnknownMarkerSeenEvent(event: UnknownMarkerSeenEvent): Unit
}