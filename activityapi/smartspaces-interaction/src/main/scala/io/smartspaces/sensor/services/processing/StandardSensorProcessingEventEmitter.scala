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

import io.smartspaces.event.observable.EventPublisherSubject
import io.smartspaces.event.observable.ObservableCreator
import io.smartspaces.logging.ExtendedLog
import io.smartspaces.sensor.event.PhysicalSpaceOccupancyLiveEvent
import io.smartspaces.sensor.event.RawSensorLiveEvent
import io.smartspaces.sensor.event.SensorOfflineEvent
import io.smartspaces.sensor.event.UnknownEntitySeenEvent
import io.smartspaces.sensor.event.UnknownMarkerSeenEvent
import io.smartspaces.system.SmartSpacesEnvironment
import io.smartspaces.sensor.event.SensorHeartbeatEvent
import io.smartspaces.sensor.event.SensorOnlineEvent
import io.smartspaces.sensor.event.SensorChannelOnlineEvent
import io.smartspaces.sensor.event.SensorChannelOfflineEvent

/**
 * An emitter of events from sensor processors.
 *
 * @author Keith M. Hughes
 */
class StandardSensorProcessingEventEmitter(
  private val nameScope: Option[String],
  private val spaceEnvironment: SmartSpacesEnvironment,
  private val log: ExtendedLog) extends SensorProcessingEventEmitter {

  /**
   * The event registry. used only for object construction.
   */
  val eventObservableRegistry = spaceEnvironment.getEventObservableRegistry

  /**
   * The creator for raw sensor observables.
   */
  private val rawSensorEventCreator: ObservableCreator[EventPublisherSubject[RawSensorLiveEvent]] =
    new ObservableCreator[EventPublisherSubject[RawSensorLiveEvent]]() {
      override def newObservable(): EventPublisherSubject[RawSensorLiveEvent] = {
        EventPublisherSubject.create(log)
      }
    }

  /**
   * The creator for raw sensor observables.
   */
  private val rawSensorEventSubject: EventPublisherSubject[RawSensorLiveEvent] =
    eventObservableRegistry.getObservable(RawSensorLiveEvent.EVENT_TYPE, nameScope,
      rawSensorEventCreator)

  /**
   * The creator for sensor heartbeat observables.
   */
  private val sensorHeartbeatEventCreator: ObservableCreator[EventPublisherSubject[SensorHeartbeatEvent]] =
    new ObservableCreator[EventPublisherSubject[SensorHeartbeatEvent]]() {
      override def newObservable(): EventPublisherSubject[SensorHeartbeatEvent] = {
        EventPublisherSubject.create(log)
      }
    }

  /**
   * The creator for raw sensor observables.
   */
  private val sensorHeartbeatEventSubject: EventPublisherSubject[SensorHeartbeatEvent] =
    eventObservableRegistry.getObservable(SensorHeartbeatEvent.EVENT_TYPE, nameScope,
      sensorHeartbeatEventCreator)

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
  private val physicalLocationOccupancyEventSubject: EventPublisherSubject[PhysicalSpaceOccupancyLiveEvent] =
    eventObservableRegistry.getObservable(PhysicalSpaceOccupancyLiveEvent.EVENT_TYPE, nameScope,
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
  private val sensorOfflineEventSubject: EventPublisherSubject[SensorOfflineEvent] =
    eventObservableRegistry.getObservable(SensorOfflineEvent.EVENT_TYPE, nameScope,
      sensorOfflineEventCreator)

  /**
   * The creator for sensor online observables.
   */
  private val sensorOnlineEventCreator: ObservableCreator[EventPublisherSubject[SensorOnlineEvent]] =
    new ObservableCreator[EventPublisherSubject[SensorOnlineEvent]]() {
      override def newObservable(): EventPublisherSubject[SensorOnlineEvent] = {
        EventPublisherSubject.create(log)
      }
    }

  /**
   * The subject for sensor online events
   */
  private val sensorOnlineEventSubject: EventPublisherSubject[SensorOnlineEvent] =
    eventObservableRegistry.getObservable(SensorOnlineEvent.EVENT_TYPE, nameScope,
      sensorOnlineEventCreator)

  /**
   * The creator for sensor channel offline observables.
   */
  private val sensorChannelOfflineEventCreator: ObservableCreator[EventPublisherSubject[SensorChannelOfflineEvent]] =
    new ObservableCreator[EventPublisherSubject[SensorChannelOfflineEvent]]() {
      override def newObservable(): EventPublisherSubject[SensorChannelOfflineEvent] = {
        EventPublisherSubject.create(log)
      }
    }

  /**
   * The subject for sensor channel offline events
   */
  private val sensorChannelOfflineEventSubject: EventPublisherSubject[SensorChannelOfflineEvent] =
    eventObservableRegistry.getObservable(SensorChannelOfflineEvent.EVENT_TYPE, nameScope,
      sensorChannelOfflineEventCreator)

  /**
   * The creator for sensor chanel online observables.
   */
  private val sensorChannelOnlineEventCreator: ObservableCreator[EventPublisherSubject[SensorChannelOnlineEvent]] =
    new ObservableCreator[EventPublisherSubject[SensorChannelOnlineEvent]]() {
      override def newObservable(): EventPublisherSubject[SensorChannelOnlineEvent] = {
        EventPublisherSubject.create(log)
      }
    }

  /**
   * The subject for sensor channel online events
   */
  private val sensorChannelOnlineEventSubject: EventPublisherSubject[SensorChannelOnlineEvent] =
    eventObservableRegistry.getObservable(SensorChannelOnlineEvent.EVENT_TYPE, nameScope,
      sensorChannelOnlineEventCreator)

  /**
   * The creator for unknown marker seen observables.
   */
  private val unknownMarkerSeenEventCreator: ObservableCreator[EventPublisherSubject[UnknownEntitySeenEvent]] =
    new ObservableCreator[EventPublisherSubject[UnknownEntitySeenEvent]]() {
      override def newObservable(): EventPublisherSubject[UnknownEntitySeenEvent] = {
        EventPublisherSubject.create(log)
      }
    }

  /**
   * The subject for unknown marker seen events
   */
  private var unknownMarkerSeenEventSubject: EventPublisherSubject[UnknownEntitySeenEvent] =
    eventObservableRegistry.getObservable(UnknownMarkerSeenEvent.EVENT_TYPE, nameScope,
      unknownMarkerSeenEventCreator)

  /**
   * Construct an emitter with no name scope.
   *
   * @param spaceEnvironment
   *        the space environment
   * @param log
   *        the log
   */
  def this(spaceEnvironment: SmartSpacesEnvironment, log: ExtendedLog) {
    this(None, spaceEnvironment, log)
  }

  override def broadcastRawSensorEvent(event: RawSensorLiveEvent): Unit = {
    rawSensorEventSubject.onNext(event)
  }

  override def broadcastSensorHeartbeatEvent(event: SensorHeartbeatEvent): Unit = {
    sensorHeartbeatEventSubject.onNext(event)
  }

  override def broadcastOccupancyEvent(event: PhysicalSpaceOccupancyLiveEvent): Unit = {
    physicalLocationOccupancyEventSubject.onNext(event)
  }

  override def broadcastSensorOfflineEvent(event: SensorOfflineEvent): Unit = {
    sensorOfflineEventSubject.onNext(event)
  }

  override def broadcastSensorOnlineEvent(event: SensorOnlineEvent): Unit = {
    sensorOnlineEventSubject.onNext(event)
  }

  override def broadcastSensorChannelOfflineEvent(event: SensorChannelOfflineEvent): Unit = {
    sensorChannelOfflineEventSubject.onNext(event)
  }

  override def broadcastSensorChannelOnlineEvent(event: SensorChannelOnlineEvent): Unit = {
    sensorChannelOnlineEventSubject.onNext(event)
  }

  override def broadcastUnknownMarkerSeenEvent(event: UnknownEntitySeenEvent): Unit = {
    unknownMarkerSeenEventSubject.onNext(event)
  }
}