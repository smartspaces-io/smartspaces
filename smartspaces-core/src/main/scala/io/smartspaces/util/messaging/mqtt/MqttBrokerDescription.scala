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

import io.smartspaces.SmartSpacesException
import io.smartspaces.util.data.json.StandardJsonMapper

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

      val brokerDescription = new MqttBrokerDescription(brokerHost, brokerPort, isSsl)

      val paramString = matcher.group(4)
      if (paramString != null) {
        val params = StandardJsonMapper.INSTANCE.parseObject(paramString.substring(1))

        brokerDescription.username = Option(params.get("username").asInstanceOf[String])
        brokerDescription.password = Option(params.get("password").asInstanceOf[String])
        brokerDescription.keystorePath = Option(params.get("keystorePath").asInstanceOf[String])
        brokerDescription.keystorePassword = Option(params.get("keystorePassword").asInstanceOf[String])
        brokerDescription.caCertPath = Option(params.get("caCertPath").asInstanceOf[String])
        brokerDescription.clientCertPath = Option(params.get("clientCertPath").asInstanceOf[String])
        brokerDescription.clientKeyPath = Option(params.get("clientKeyPath").asInstanceOf[String])
        brokerDescription.autoreconnect = Option(params.get("autoreconnect").asInstanceOf[Boolean])
        brokerDescription.persistencePath = Option(params.get("persistencePath").asInstanceOf[String])
      }

      brokerDescription
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
class MqttBrokerDescription(val brokerHost: String, val brokerPort: Integer, val isSsl: Boolean) {

  /**
   * The network address of the broker.
   */
  val brokerAddress = (if (isSsl) "ssl" else "tcp") + "://" + brokerHost + ":" + brokerPort

  /**
   * The username for the broker.
   */
  var username: Option[String] = None

  /**
   * The password for the broker.
   */
  var password: Option[String] = None

  /**
   * File path to the keystore if using SSL.
   */
  var keystorePath: Option[String] = None

  /**
   * Password for the keystore if using SSL.
   */
  var keystorePassword: Option[String] = None

  /**
   * File path to the certificate authority cert if using client certificate SSL.
   */
  var caCertPath: Option[String] = None

  /**
   * File path to the client cert if using client certificate SSL.
   */
  var clientCertPath: Option[String] = None

  /**
   * File path to the client private key if using client certificate SSL.
   */
  var clientKeyPath: Option[String] = None

  /**
   * {@code true} if should reconnect automatically back to the broker.
   */
  var autoreconnect: Option[Boolean] = None
  
  /**
   * The path to the persistence for MQTT retained messages.
   */
  var persistencePath: Option[String] = None
}
