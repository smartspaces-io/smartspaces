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

/**
 * A handler for markers seen that are unknown.
 * 
 * @author Keith M. Hughes
 */
trait UnknownMarkerHandler {

  /**
   * An unknown sensor ID was received.
   *
   * @param markerId
   *          the ID of the unknown marker
   * @param timestamp
   *          timestamp when the marker was seen
   */
  def handleUnknownMarker(markerId: String, timestamp: Long): Unit

  /**
   * Remove an unknown marker ID from the handler.
   *
   * <p>
   * This will usually be because it has finally been added to the known
   * markers.
   *
   * @param markerId
   *          the known marker
   */
  def removeUnknownMarkerId(markerId: String): Unit

  /**
   * Get all of the unknown marker IDs.
   *
   * @return all of the unknown marker IDs
   */
  def getAllUnknownMarkerIds(): Set[String]
}