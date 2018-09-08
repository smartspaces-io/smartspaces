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

package io.smartspaces.sensor.event

import io.smartspaces.sensor.model.SensorEntityModel
import io.smartspaces.sensor.model.SensorChannelEntityModel

/**
 * An event indicating a sensor going offline.
 *
 * @author Keith M. Hughes
 */
object SensorOfflineEvent {

  /**
   * The name of the event.
   */
  val EVENT_TYPE = "sensor.offline"
}

/**
 * An event indicating a sensor going offline.
 *
 * @author Keith M. Hughes
 */
class SensorOfflineEvent(val sensorModel: SensorEntityModel, val timestampOffline: Long)

/**
 * An event indicating a sensor going online.
 *
 * @author Keith M. Hughes
 */
object SensorOnlineEvent {

  /**
   * The name of the event.
   */
  val EVENT_TYPE = "sensor.online"
}

/**
 * An event indicating a sensor going online.
 *
 * @author Keith M. Hughes
 */
class SensorOnlineEvent(val sensorModel: SensorEntityModel, val timestampOnline: Long)

/**
 * An event indicating a sensor channel going offline.
 *
 * @author Keith M. Hughes
 */
object SensorChannelOfflineEvent {

  /**
   * The name of the event.
   */
  val EVENT_TYPE = "sensor.channel.offline"
}

/**
 * An event indicating a sensor channel going offline.
 *
 * @author Keith M. Hughes
 */
class SensorChannelOfflineEvent(val sensorChannelModel: SensorChannelEntityModel, val timestampOffline: Long)

/**
 * An event indicating a sensor channel going online.
 *
 * @author Keith M. Hughes
 */
object SensorChannelOnlineEvent {

  /**
   * The name of the event.
   */
  val EVENT_TYPE = "sensor.channel.online"
}

/**
 * An event indicating a sensor channel going online.
 *
 * @author Keith M. Hughes
 */
class SensorChannelOnlineEvent(val sensorChannelModel: SensorChannelEntityModel, val timestampOnline: Long)

/**
 * An event for a new sensor heartbeat.
 *
 * @author Keith M. Hughes
 */
object SensorHeartbeatEvent {

  /**
   * The type of the event.
   */
  val EVENT_TYPE = "sensor.heartbeat"
}

/**
 * An event for a new sensor heartbeat.
 *
 * @author Keith M. Hughes
 */
class SensorHeartbeatEvent(
    
  /**
   * The entity that was sensed.
   */
  val sensorModel: SensorEntityModel,

  /**
   * The value that was sensed.
   */
  val timestampHeartbeat: Long)

