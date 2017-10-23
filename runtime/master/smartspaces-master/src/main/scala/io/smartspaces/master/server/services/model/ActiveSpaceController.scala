/*
 * Copyright (C) 2016 Keith M. Hughes
 * Copyright (C) 2012 Google Inc.
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

package io.smartspaces.master.server.services.model;

import io.smartspaces.domain.basic.SpaceController;
import io.smartspaces.master.server.services.internal.DataBundleState;
import io.smartspaces.spacecontroller.SpaceControllerState;
import io.smartspaces.time.provider.TimeProvider;

import scala.Option;

import java.util.Date;

/**
 * A space controller "active" in the space.
 *
 * <p>
 * "Active" means that the master knows about it.
 *
 * @author Keith M. Hughes
 */
class ActiveSpaceController(var spaceController: SpaceController,  timeProvider: TimeProvider) {

  /**
   * Current known state of the controller.
   */
  private var state: SpaceControllerState = SpaceControllerState.UNKNOWN

  /**
   * Timestamp of the last update.
   */
  private var lastStateUpdate: Option[Long] = None

  /**
   * Current known data bundle state of the controller.
   */
  private var dataBundleState: DataBundleState = DataBundleState.NO_REQUEST;

  /**
   * Timestamp of the last data bundle state update.
   */
  private var lastDataBundleStateUpdate:  Option[Long] = None

  /**
   * Last timestamp for a heartbeat.
   */
  private var lastHeartbeatTimestamp: Option[Long]  = None

  /**
   * Update the controller object contained within.
   *
   * <p>
   * This allows this object access to merged data.
   *
   * @param spaceController
   *          the potentially updated controller entity
   */
  def updateController( spaceController: SpaceController): Unit = {
    this.spaceController = spaceController;
  }

  /**
   * @return the state
   */
  def getState(): SpaceControllerState = {
    return state
  }

  /**
   * Set the state of the active space controller.
   *
   * @param state
   *          the state to set
   */
  def setState( state: SpaceControllerState): Unit = {
    this.state = state;

    lastStateUpdate = Some(timeProvider.getCurrentTime());
  }

  /**
   * Get the lastStateUpdate of the last update.
   *
   * @return the lastStateUpdate, will be {@code null} if the controller has
   *         never been updated
   */
  def  getLastStateUpdate(): Option[Long] = {
    lastStateUpdate
  }

  /**
   * Get the lastStateUpdate of the last update as a date.
   *
   * @return the lastStateUpdate, will be {@code null} if the controller has
   *         never been updated
   */
  def  getLastStateUpdateDate(): Option[Date] = {
    if (lastStateUpdate.isDefined) {
      Some(new Date(lastStateUpdate.get))
    } else {
      None
    }
  }

  /**
   * @return the data bundle state
   */
  def getDataBundleState(): DataBundleState = {
    dataBundleState
  }

  /**
   * @param dataBundleState
   *          the data bundle state to set
   */
  def setDataBundleState( dataBundleState: DataBundleState) = {
    this.dataBundleState = dataBundleState

    lastDataBundleStateUpdate = Some(timeProvider.getCurrentTime())
  }

  /**
   * Get the lastDataBundleStateUpdate of the last update.
   *
   * @return the lastDataBundleStateUpdate. Will be {@code null} if the
   *         controller has never been updated.
   */
 def getLastDataBundleStateUpdate(): Option[Long] = {
    lastDataBundleStateUpdate
  }

  /**
   * Get the lastDataBundleStateUpdate of the last update as a date.
   *
   * @return the lastDataBundleStateUpdate. Will be {@code null} if the
   *         controller has never been updated.
   */
  def getLastDataBundleStateUpdateDate(): Date = {
    if (lastDataBundleStateUpdate.isDefined) {
      return new Date(lastDataBundleStateUpdate.get);
    } else {
      return null;
    }
  }

  /**
   * Set the new heartbeat time.
   *
   * @param heartbeatTime
   *          the new heartbeat time
   */
  def updateHeartbeatTime(heartbeatTime: Long): Unit = {
    lastHeartbeatTimestamp = Some(heartbeatTime)
  }

  /**
   * Get the last heartbeat time.
   *
   * @return the last heartbeat time, or {@code null} if it has never been set
   */
  def getLastHeartbeatTime(): Option[Long] = {
    lastHeartbeatTimestamp
  }

  /**
   * Get the amount of time between the sample time and the last heartbeat.
   *
   * @param sampletime
   *          the sample time, in milliseconds
   *
   * @return the time difference in milliseconds
   */
  def timeSinceLastHeartbeat(sampletime: Long): Option[Long] = {
    if (lastHeartbeatTimestamp.isDefined) {
      return Some(sampletime - lastHeartbeatTimestamp.get);
    } else if (lastStateUpdate.isDefined) {
      // Since hasn't been a heartbeat, just go with the last time we had a
      // status update
      return Some(sampletime - lastStateUpdate.get)
    } else {
      // Assumption... some day someone will update the state, so just say
      // everything is fine
      return None;
    }
  }

  /**
   * Get a nice display name for the space controller.
   *
   * @return a nice display name for the space controller
   */
  def getDisplayName(): String = {
    s"UUID ${spaceController.getUuid}, host id ${spaceController.getHostId}, name ${spaceController.getName}"
  }
}
