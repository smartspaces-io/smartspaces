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

package io.smartspaces.interaction.entity.model.reactive

import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import io.smartspaces.activity.behavior.web.WebServerActivityBehavior
import io.smartspaces.sensor.entity.model.PersonSensedEntityModel
import io.smartspaces.sensor.entity.model.event.PhysicalSpaceOccupancyLiveEvent
import io.smartspaces.util.data.dynamic.StandardDynamicObjectBuilder
import io.smartspaces.event.observable.BaseObserver
import io.smartspaces.service.web.server.CompleteWebServerBehavior

/**
 * An RX Subscriber for physical location occupancy events that communicates over a web socket.
 *
 *
 * @author Keith M. Hughes
 */
class ObserverWebSocketNotifier(private val webServer: CompleteWebServerBehavior) extends BaseObserver[PhysicalSpaceOccupancyLiveEvent] {

  override def onNext(event: PhysicalSpaceOccupancyLiveEvent): Unit = {
    val entered = event.entered
    if (entered != null) {
      entered.foreach((person: PersonSensedEntityModel) => {
        val message = String.format("%s has entered %s", person.sensedEntityDescription.displayName,
          event.physicalSpace.sensedEntityDescription.displayName)

        val msg = new StandardDynamicObjectBuilder
        msg.setProperty("message", message).setProperty("author", "keith")

        webServer.sendWebSocketMessage(msg.toMap())
      })
    }

    val exited = event.exited
    if (exited != null) {
      exited.foreach((person: PersonSensedEntityModel) => {
        val message = String.format("%s has exited %s", person.sensedEntityDescription.displayName,
          event.physicalSpace.sensedEntityDescription.displayName)

        val msg = new StandardDynamicObjectBuilder
        msg.setProperty("message", message).setProperty("author", "keith")

        webServer.sendWebSocketMessage(msg.toMap())
      })
    }
  }
}