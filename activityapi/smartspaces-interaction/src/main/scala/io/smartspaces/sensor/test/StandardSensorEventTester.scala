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

package io.smartspaces.sensor.test

import io.smartspaces.event.observable.EventPublisherSubject
import io.smartspaces.event.observable.ObservableCreator
import io.smartspaces.system.SmartSpacesEnvironment

import io.smartspaces.sensor.entity.model.event._
import io.smartspaces.sensor.entity.model.event.SensorOfflineEvent
import io.smartspaces.sensor.entity.model.event.PhysicalSpaceOccupancyLiveEvent
import io.smartspaces.event.observable.ObservableCreator


class StandardSensorEventTester(val spaceEnvironment: SmartSpacesEnvironment) {
  
  /**
   * The subject for physical location occupancy events
   */
  private var physicalLocationOccupancyEventSubject: EventPublisherSubject[PhysicalSpaceOccupancyLiveEvent] = null

  /**
   * The creator for physical occupancy observables.
   */
  private val physicalLocationOccupancyEventCreator: ObservableCreator[EventPublisherSubject[PhysicalSpaceOccupancyLiveEvent]] =
    new ObservableCreator[EventPublisherSubject[PhysicalSpaceOccupancyLiveEvent]]() {
      override def newObservable(): EventPublisherSubject[PhysicalSpaceOccupancyLiveEvent] = {
        EventPublisherSubject.create(spaceEnvironment.getLog)
      }
    }

  /**
   * The subject for sensor offline events
   */
  private var sensorOfflineEventSubject: EventPublisherSubject[SensorOfflineEvent] = null

  /**
   * The creator for sensor offline observables.
   */
  private val sensorOfflineEventCreator: ObservableCreator[EventPublisherSubject[SensorOfflineEvent]] =
    new ObservableCreator[EventPublisherSubject[SensorOfflineEvent]]() {
      override def newObservable(): EventPublisherSubject[SensorOfflineEvent] = {
        EventPublisherSubject.create(spaceEnvironment.getLog)
      }
    }

  def startup(): Unit = {
    val eventObservableRegistry = spaceEnvironment.getEventObservableRegistry

    physicalLocationOccupancyEventSubject =
      eventObservableRegistry.getObservable(PhysicalSpaceOccupancyLiveEvent.EVENT_TYPE,
        physicalLocationOccupancyEventCreator)

    sensorOfflineEventSubject =
      eventObservableRegistry.getObservable(SensorOfflineEvent.EVENT_TYPE,
        sensorOfflineEventCreator)
  }
  
  
  def broadcastOccupanyEvent(event: PhysicalSpaceOccupancyLiveEvent): Unit = {
    physicalLocationOccupancyEventSubject.onNext(event)
  }

  def broadcastSensorOfflineEvent(event: SensorOfflineEvent): Unit = {
    sensorOfflineEventSubject.onNext(event)
  }

}