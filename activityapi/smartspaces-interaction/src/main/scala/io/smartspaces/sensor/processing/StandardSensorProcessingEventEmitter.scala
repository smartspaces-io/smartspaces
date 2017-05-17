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
import io.smartspaces.event.observable.EventPublisherSubject
import io.smartspaces.event.observable.ObservableCreator
import io.smartspaces.logging.ExtendedLog
import io.smartspaces.system.SmartSpacesEnvironment

/**
 * An emitter of events from sensor processors.
 * 
 * @author Keith M. Hughes
 */
class StandardSensorProcessingEventEmitter(private val spaceEnvironment: SmartSpacesEnvironment, private val log: ExtendedLog) extends SensorProcessingEventEmitter {
  
  /**
   * The event registry. used only for object construction.
   */
  val eventObservableRegistry = spaceEnvironment.getEventObservableRegistry

  /**
   * The creator for physical occupancy observables.
   */
  private val physicalLocationOccupancyEventCreator: ObservableCreator[EventPublisherSubject[PhysicalSpaceOccupancyLiveEvent]] =
    new ObservableCreator[EventPublisherSubject[PhysicalSpaceOccupancyLiveEvent]]() {
      override def newObservable(): EventPublisherSubject[PhysicalSpaceOccupancyLiveEvent] = {
        EventPublisherSubject.create(log)
      }
    }

  /**
   * The subject for physical location occupancy events
   */
  private var physicalLocationOccupancyEventSubject: EventPublisherSubject[PhysicalSpaceOccupancyLiveEvent] =
      eventObservableRegistry.getObservable(PhysicalSpaceOccupancyLiveEvent.EVENT_TYPE,
        physicalLocationOccupancyEventCreator)

  /**
   * The creator for sensor offline observables.
   */
  private val sensorOfflineEventCreator: ObservableCreator[EventPublisherSubject[SensorOfflineEvent]] =
    new ObservableCreator[EventPublisherSubject[SensorOfflineEvent]]() {
      override def newObservable(): EventPublisherSubject[SensorOfflineEvent] = {
        EventPublisherSubject.create(log)
      }
    }

  /**
   * The subject for sensor offline events
   */
  private var sensorOfflineEventSubject: EventPublisherSubject[SensorOfflineEvent] =
      eventObservableRegistry.getObservable(SensorOfflineEvent.EVENT_TYPE,
        sensorOfflineEventCreator)

  /**
   * The creator for unknown marker seen observables.
   */
  private val unknownMarkerSeenEventCreator: ObservableCreator[EventPublisherSubject[UnknownMarkerSeenEvent]] =
    new ObservableCreator[EventPublisherSubject[UnknownMarkerSeenEvent]]() {
      override def newObservable(): EventPublisherSubject[UnknownMarkerSeenEvent] = {
        EventPublisherSubject.create(log)
      }
    }

  /**
   * The subject for unknown marker seen events
   */
  private var unknownMarkerSeenEventSubject: EventPublisherSubject[UnknownMarkerSeenEvent] =
      eventObservableRegistry.getObservable(UnknownMarkerSeenEvent.EVENT_TYPE,
        unknownMarkerSeenEventCreator)

  override def broadcastOccupanyEvent(event: PhysicalSpaceOccupancyLiveEvent): Unit = {
    physicalLocationOccupancyEventSubject.onNext(event)
  }

  override def broadcastSensorOfflineEvent(event: SensorOfflineEvent): Unit = {
    log.warn(s"Broadcasting sensor offline event ${event.sensorModel.sensorEntityDescription}")
    sensorOfflineEventSubject.onNext(event)
  }

  override def broadcastUnknownMarkerSeenEvent(event: UnknownMarkerSeenEvent): Unit = {
    log.warn(s"Broadcasting unknown marker seen event ${event.markerId}")
    unknownMarkerSeenEventSubject.onNext(event)
  }
}