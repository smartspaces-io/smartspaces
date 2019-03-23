/*
 * Copyright (C) 2016 Keith M. Hughes
 * Copyright (C) 2015 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License ats
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package io.smartspaces.messaging.dynamic;

/**
 * Some common constants and functions for creating dynamic messages for
 * SmartSpaces to have a common format.
 * 
 * @author Keith M. Hughes
 */
public class SmartSpacesMessages {

  /**
   * The version field for a message envelope.
   */
  public static final String MESSAGE_ENVELOPE_VERSION = "version";

  /**
   * The type field for a message envelope.
   */
  public static final String MESSAGE_ENVELOPE_TYPE = "messageType";

  /**
   * The data field for a message envelope.
   */
  public static final String MESSAGE_ENVELOPE_DATA = "data";

  /**
   * Field in the message envelope giving the request ID.
   */
  public static final String MESSAGE_ENVELOPE_REQUEST_ID = "requestId";

  /**
   * Field in the message envelope giving the sender of the message.
   */
  public static final String MESSAGE_ENVELOPE_SENDER = "sender";

  /**
   * Field in the message envelope giving the destination of the message.
   */
  public static final String MESSAGE_ENVELOPE_DESTINATION = "destination";

  /**
   * Field in the message envelope giving the result.
   */
  public static final String MESSAGE_ENVELOPE_RESULT = "result";

  /**
   * The result given for a successful call.
   */
  public static final String MESSAGE_ENVELOPE_VALUE_RESULT_SUCCESS = "success";

  /**
   * The result given for a failed call.
   */
  public static final String MESSAGE_ENVELOPE_VALUE_RESULT_FAILURE = "failure";

  /**
   * Field in a response message envelope giving the reason for a response.
   */
  public static final String MESSAGE_ENVELOPE_REASON = "reason";

  /**
   * Field in a response message envelope giving the detail for a response.
   */
  public static final String MESSAGE_ENVELOPE_DETAIL = "detail";

}