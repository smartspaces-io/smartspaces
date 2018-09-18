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

package io.smartspaces.util.messaging.mqtt

import java.lang.{ Integer => JInteger }
import io.smartspaces.SmartSpacesException
import io.smartspaces.util.data.mapper.StandardJsonDataMapper

import java.util.regex.Pattern

/**
 * The description of an MQTT broker.
 *
 * @author Keith M. Hughes
 */
object MqttBrokerDescription {

  /**
   * The value for memory persistence.
   */
  val VALUE_PERSISTENCE_PATH_MEMORY = "memory:"

  /**
   * The default for the path for the persistence for MQTT messages.
   */
  val DEFAULT_PERSISTENCE_PATH = VALUE_PERSISTENCE_PATH_MEMORY

  /**
   * The regular expression for the broker description.
   */
  val BROKER_DESCRIPTION_PATTERN =
    Pattern.compile("^([a-zA_Z]+)://(.+):([0-9]+)(@\\{.*\\})?$")

  /**
   * Parse a description string into an MQTT broker description.
   */
  def parse(description: String): MqttBrokerDescription = {
    val matcher = BROKER_DESCRIPTION_PATTERN.matcher(description.trim)
    if (matcher.matches) {
      val isSsl = "ssl" == matcher.group(1)

      val brokerHost = matcher.group(2)

      val brokerPort = matcher.group(3).toInt

      var username: Option[String] = None
      var password: Option[String] = None
      var keystorePath: Option[String] = None
      var keystorePassword: Option[String] = None
      var caCertPath: Option[String] = None
      var clientCertPath: Option[String] = None
      var clientKeyPath: Option[String] = None
      var autoreconnect: Option[Boolean] = None
      var persistencePath: Option[String] = None
      var brokerClientId: Option[String] = None
      var maxInFlight: Option[JInteger] = None

      val paramString = matcher.group(4)
      if (paramString != null) {
        val params = StandardJsonDataMapper.INSTANCE.parseObject(paramString.substring(1))

        username = Option(params.get("username").asInstanceOf[String])
        password = Option(params.get("password").asInstanceOf[String])
        keystorePath = Option(params.get("keystorePath").asInstanceOf[String])
        keystorePassword = Option(params.get("keystorePassword").asInstanceOf[String])
        caCertPath = Option(params.get("caCertPath").asInstanceOf[String])
        clientCertPath = Option(params.get("clientCertPath").asInstanceOf[String])
        clientKeyPath = Option(params.get("clientKeyPath").asInstanceOf[String])
        autoreconnect = Option(params.get("autoreconnect").asInstanceOf[Boolean])
        persistencePath = Option(params.get("persistencePath").asInstanceOf[String])
        brokerClientId = Option(params.get("brokerClientId").asInstanceOf[String])
        maxInFlight = Option(params.get("maxInFlight").asInstanceOf[JInteger])
      }

      new MqttBrokerDescription(brokerHost, brokerPort, isSsl, username, password, keystorePath, keystorePassword, caCertPath,
        clientCertPath, clientKeyPath, 
        autoreconnect, persistencePath, 
        brokerClientId, maxInFlight)
    } else {
      throw new SmartSpacesException(s"MQTT broker description has the wrong syntax: ${description}")
    }
  }
}

/**
 * The description of an MQTT broker.
 *
 * @author Keith M. Hughes
 */
class MqttBrokerDescription(
  val brokerHost: String,
  val brokerPort: Integer,
  val isSsl: Boolean,

  /**
   * The username for the broker.
   */
  val username: Option[String],

  /**
   * The password for the broker.
   */
  val password: Option[String],

  /**
   * File path to the keystore if using SSL.
   */
  val keystorePath: Option[String],

  /**
   * Password for the keystore if using SSL.
   */
  val keystorePassword: Option[String],

  /**
   * File path to the certificate authority cert if using client certificate SSL.
   */
  val caCertPath: Option[String],

  /**
   * File path to the client cert if using client certificate SSL.
   */
  val clientCertPath: Option[String],

  /**
   * File path to the client private key if using client certificate SSL.
   */
  val clientKeyPath: Option[String],

  /**
   * {@code true} if should reconnect automatically back to the broker.
   */
  val autoreconnect: Option[Boolean],

  /**
   * The path to the persistence for MQTT retained messages.
   */
  val persistencePath: Option[String],

  /**
   * The client ID for a client based on this broker description.
   *
   * used when the client should be shared amongst multiple users.
   */
  val brokerClientId: Option[String],

  /**
   * The maximum number of inflight publishes allowed.
   */
  val maxInFlight: Option[JInteger]) extends Equals {

  /**
   * The network address of the broker.
   */
  val brokerAddress = (if (isSsl) "ssl" else "tcp") + "://" + brokerHost + ":" + brokerPort

  override def toString(): String = {
    s"${getClass.getName()}[broker: ${brokerAddress}, brokerClientId=${brokerClientId}]"
  }

  def canEqual(other: Any) = {
    other.isInstanceOf[io.smartspaces.util.messaging.mqtt.MqttBrokerDescription]
  }

  override def equals(other: Any) = {
    other match {
      case that: io.smartspaces.util.messaging.mqtt.MqttBrokerDescription =>
        that.canEqual(MqttBrokerDescription.this) &&
          brokerHost == that.brokerHost &&
          brokerPort == that.brokerPort &&
          isSsl == that.isSsl &&
          brokerClientId == that.brokerClientId
      case _ => false
    }
  }

  override def hashCode() = {
    val prime = 41
    prime * (prime * (prime * (prime + brokerHost.hashCode) +
      brokerPort.hashCode) + isSsl.hashCode) +
      brokerClientId.hashCode
  }
}
