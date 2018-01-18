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

import io.smartspaces.sensor.event.PhysicalSpaceOccupancyLiveEvent
import io.smartspaces.sensor.event.RawSensorLiveEvent
import io.smartspaces.sensor.event.SensorOfflineEvent
import io.smartspaces.sensor.event.UnknownEntitySeenEvent

/**
 * An emitter of events from sensor processors.
 * 
 * @author Keith M. Hughes
 */
trait SensorProcessingEventEmitter {
  
  /**
   * Broadcast a raw sensor event.
   *
   * @param event
   * 		the event to broadcast
   */
  def broadcastRawSensorEvent(event:RawSensorLiveEvent): Unit
  
  /**
   * Broadcast a physical location occupancy event.
   *
   * @param event
   * 		the event to broadcast
   */
  def broadcastOccupancyEvent(event: PhysicalSpaceOccupancyLiveEvent): Unit

  /**
   * Broadcast a sensor offline event.
   *
   * @param event
   * 		the event to broadcast
   */
  def broadcastSensorOfflineEvent(event: SensorOfflineEvent): Unit

  /**
   * Broadcast an unknown marker event.
   *
   * @param event
   * 		the event to broadcast
   */
  def broadcastUnknownMarkerSeenEvent(event: UnknownEntitySeenEvent): Unit
}