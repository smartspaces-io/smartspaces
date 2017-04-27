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

package io.smartspaces.messaging.dynamic;

import io.smartspaces.SmartSpacesExceptionUtils;
import io.smartspaces.util.data.dynamic.DynamicObjectBuilder;
import io.smartspaces.util.data.dynamic.StandardDynamicObjectBuilder;

import java.util.HashMap;
import java.util.Map;

/**
 * A set of methods and constants to provide uniform Smart Spaces message
 * support.
 *
 * @author Keith M. Hughes
 */
public class SmartSpacesMessagesSupport {

  /**
   * Get the simple version of a Smart Spaces message success response.
   *
   * @return a success response
   */
  public static Map<String, Object> getSimpleSuccessResponse() {
    Map<String, Object> response = new HashMap<>();

    response.put(SmartSpacesMessages.MESSAGE_ENVELOPE_RESULT,
        SmartSpacesMessages.MESSAGE_ENVELOPE_VALUE_RESULT_SUCCESS);

    return response;
  }

  /**
   * Get a Smart Spaces message success response with data.
   *
   * @param data
   *          the data field for the responses
   *
   * @return a success Smart Spaces message response with data
   */
  public static Map<String, Object> getSuccessResponse(Object data) {
    Map<String, Object> response = getSimpleSuccessResponse();

    response.put(SmartSpacesMessages.MESSAGE_ENVELOPE_DATA, data);

    return response;
  }

  /**
   * Get a failure Smart Spaces message response.
   *
   * @param reason
   *          the reason for the failure
   * @param detail
   *          details about the failure
   *
   * @return the Smart Spaces message response object
   */
  public static Map<String, Object> getFailureResponse(String reason, String detail) {
    return getFailureResponse(null, reason, detail);
  }

  /**
   * Get a failure Smart Spaces message response.
   *
   * @param type
   *          the type of the message, can be {@code null}
   * @param reason
   *          the reason for the failure, can be {@code null}
   * @param detail
   *          details about the failure, can be {@code null}
   *
   * @return the Smart Spaces message response object
   */
  public static Map<String, Object> getFailureResponse(String type, String reason, String detail) {
    Map<String, Object> response = new HashMap<>();

    if (type != null) {
      response.put(SmartSpacesMessages.MESSAGE_ENVELOPE_TYPE, type);
    }
    
    response.put(SmartSpacesMessages.MESSAGE_ENVELOPE_RESULT,
        SmartSpacesMessages.MESSAGE_ENVELOPE_VALUE_RESULT_FAILURE);

    if (reason != null) {
      response.put(SmartSpacesMessages.MESSAGE_ENVELOPE_REASON, reason);
    }

    if (detail != null) {
      response.put(SmartSpacesMessages.MESSAGE_ENVELOPE_DETAIL, detail);
    }

    return response;
  }

  /**
   * Get a failure Smart Spaces message response.
   *
   * @param reason
   *          the reason for the failure
   * @param throwable
   *          the throwable that caused the error
   *
   * @return the Smart Spaces message response object
   */
  public static Map<String, Object> getFailureResponse(String reason, Throwable throwable) {
    return getFailureResponse(reason, SmartSpacesExceptionUtils.getExceptionDetail(throwable));
  }

  /**
   * Is the response a success response?
   *
   * @param response
   *          a Smart Spaces message response
   *
   * @return {@code true} if the response was a success
   */
  public static boolean isSuccessResponse(Map<String, Object> response) {
    return SmartSpacesMessages.MESSAGE_ENVELOPE_VALUE_RESULT_SUCCESS
        .equals(response.get(SmartSpacesMessages.MESSAGE_ENVELOPE_RESULT));
  }

  /**
   * Was the reason for the response the reason given?
   *
   * @param response
   *          the Smart Spaces message response
   * @param reason
   *          the reason given
   *
   * @return {@code true} if the reason given is the reason in the response
   */
  public static boolean isResponseReason(Map<String, Object> response, String reason) {
    return reason.equals(response.get(SmartSpacesMessages.MESSAGE_ENVELOPE_REASON));
  }

  /**
   * Get the response detail.
   *
   * @param response
   *          the Master API response
   *
   * @return the detail, or {@code null} if none
   */
  public static String getResponseDetail(Map<String, Object> response) {
    return (String) response.get(SmartSpacesMessages.MESSAGE_ENVELOPE_DETAIL);
  }

  /**
   * Get the data field as a map from a response object.
   *
   * @param response
   *          the response object
   *
   * @return the data map
   */
  public static Map<String, Object> getResponseDataMap(Map<String, Object> response) {
    @SuppressWarnings("unchecked")
    Map<String, Object> data =
        (Map<String, Object>) response.get(SmartSpacesMessages.MESSAGE_ENVELOPE_DATA);
    return data;
  }
  

  /**
   * Get a failure Smart Spaces message response.
   *
   * @param reason
   *          the reason for the failure
   * @param detail
   *          details about the failure
   *
   * @return the Smart Spaces message response object
   */
  public static DynamicObjectBuilder getFailureResponseAsBuilder(String reason, String detail) {
    return getFailureResponseAsBuilder(null, reason, detail);
  }

  /**
   * Get a failure Smart Spaces message response.
   *
   * @param type
   *          the type of the message, can be {@code null}
   * @param reason
   *          the reason for the failure, can be {@code null}
   * @param detail
   *          details about the failure, can be {@code null}
   *
   * @return the Smart Spaces message response object
   */
  public static DynamicObjectBuilder getFailureResponseAsBuilder(String type, String reason, String detail) {
    DynamicObjectBuilder response = new StandardDynamicObjectBuilder();

    if (type != null) {
      response.setProperty(SmartSpacesMessages.MESSAGE_ENVELOPE_TYPE, type);
    }
    
    response.setProperty(SmartSpacesMessages.MESSAGE_ENVELOPE_RESULT,
        SmartSpacesMessages.MESSAGE_ENVELOPE_VALUE_RESULT_FAILURE);

    if (reason != null) {
      response.setProperty(SmartSpacesMessages.MESSAGE_ENVELOPE_REASON, reason);
    }

    if (detail != null) {
      response.setProperty(SmartSpacesMessages.MESSAGE_ENVELOPE_DETAIL, detail);
    }

    return response;
  }

  /**
   * Get a failure Smart Spaces message response.
   *
   * @param reason
   *          the reason for the failure
   * @param throwable
   *          the throwable that caused the error
   *
   * @return the Smart Spaces message response object
   */
  public static DynamicObjectBuilder getFailureResponseAsBuilder(String reason, Throwable throwable) {
    return getFailureResponseAsBuilder(reason, SmartSpacesExceptionUtils.getExceptionDetail(throwable));
  }
}
