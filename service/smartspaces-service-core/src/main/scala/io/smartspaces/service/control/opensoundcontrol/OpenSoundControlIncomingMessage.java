/*
 * Copyright (C) 2016 Keith M. Hughes
 * Copyright (C) 2014 Google Inc.
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

package io.smartspaces.service.control.opensoundcontrol;

import io.smartspaces.SmartSpacesException;

import java.net.InetSocketAddress;

/**
 * An incoming Open Sound Control message.
 *
 * @author Keith M. Hughes
 */
public interface OpenSoundControlIncomingMessage {

  /**
   * Get the address of the sender that sent the incoming message.
   *
   * @return the sender address
   */
  InetSocketAddress getSenderAddress();

  /**
   * Get the OSC address for the packet.
   *
   * @return the OSC address
   */
  String getAddress();

  /**
   * Get the number of arguments in the message.
   *
   * @return the number of arguments in the message
   */
  int getNumberArguments();

  /**
   * Get the arguments in the packet.
   *
   * <p>
   * This is the raw array. Modify at your own risk.
   *
   * @return the arguments in the packet
   */
  Object[] getArguments();

  /**
   * Get an int argument.
   *
   * @param arg
   *          the argument position
   *
   * @return the value
   *
   * @throws SmartSpacesException
   *           either the argument position is out of bounds or the argument is
   *           not an int
   */
  int getIntArgument(int arg) throws SmartSpacesException;

  /**
   * Get a long argument.
   *
   * @param arg
   *          the argument position
   *
   * @return the value
   *
   * @throws SmartSpacesException
   *           either the argument position is out of bounds or the argument is
   *           not a long
   */
  long getLongArgument(int arg) throws SmartSpacesException;

  /**
   * Get a float argument.
   *
   * @param arg
   *          the argument position
   *
   * @return the value
   *
   * @throws SmartSpacesException
   *           either the argument position is out of bounds or the argument is
   *           not a float
   */
  float getFloatArgument(int arg) throws SmartSpacesException;

  /**
   * Get a double argument.
   *
   * @param arg
   *          the argument position
   *
   * @return the value
   *
   * @throws SmartSpacesException
   *           either the argument position is out of bounds or the argument is
   *           not a double
   */
  double getDoubleArgument(int arg) throws SmartSpacesException;

  /**
   * Get a string argument.
   *
   * @param arg
   *          the argument position
   *
   * @return the value
   *
   * @throws SmartSpacesException
   *           either the argument position is out of bounds or the argument is
   *           not a string
   */
  String getStringArgument(int arg) throws SmartSpacesException;

  /**
   * Get a blob argument.
   *
   * @param arg
   *          the argument position
   *
   * @return the value
   *
   * @throws SmartSpacesException
   *           either the argument position is out of bounds or the argument is
   *           not a blob
   */
  byte[] getBlobArgument(int arg) throws SmartSpacesException;

  /**
   * Is the specified argument an int?
   *
   * @param arg
   *          the argument position
   *
   * @return {@code true} if the argument is an int
   *
   * @throws SmartSpacesException
   *           the argument position is out of bounds
   */
  boolean isIntArgument(int arg) throws SmartSpacesException;

  /**
   * Is the specified argument a long?
   *
   * @param arg
   *          the argument position
   *
   * @return {@code true} if the argument is a long
   *
   * @throws SmartSpacesException
   *           the argument position is out of bounds
   */
  boolean isLongArgument(int arg) throws SmartSpacesException;

  /**
   * Is the specified argument a float?
   *
   * @param arg
   *          the argument position
   *
   * @return {@code true} if the argument is an float
   *
   * @throws SmartSpacesException
   *           the argument position is out of bounds
   */
  boolean isFloatArgument(int arg) throws SmartSpacesException;

  /**
   * Is the specified argument a double?
   *
   * @param arg
   *          the argument position
   *
   * @return {@code true} if the argument is a double
   *
   * @throws SmartSpacesException
   *           the argument position is out of bounds
   */
  boolean isDoubleArgument(int arg) throws SmartSpacesException;

  /**
   * Is the specified argument a string?
   *
   * @param arg
   *          the argument position
   *
   * @return {@code true} if the argument is a string
   *
   * @throws SmartSpacesException
   *           the argument position is out of bounds
   */
  boolean isStringArgument(int arg) throws SmartSpacesException;

  /**
   * Is the specified argument a blob?
   *
   * @param arg
   *          the argument position
   *
   * @return {@code true} if the argument is a blob
   *
   * @throws SmartSpacesException
   *           the argument position is out of bounds
   */
  boolean isBlobArgument(int arg) throws SmartSpacesException;
}
