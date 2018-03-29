/*
 * Copyright (C) 2016 Keith M. Hughes
 * Copyright (C) 2013 Google Inc.
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

package io.smartspaces.service.web.server;

/**
 * An access manager determines if a user has access to a particular web
 * resource. This can include both urls and websocket messages.
 *
 * @author Dustin Barnard
 */
public interface WebResourceAccessManager {

  /**
   *
   * Checks to see if a user has access to a given resource. This should be used
   * for URL's.
   *
   * <p>
   * Implementations of this function should be thread safe.
   *
   * @param userId
   *        the identifier for the user request access to the resource
   * @param resource
   *        the identifier for the resource, should be a uri
   * @return {@code true} if the given user is allowed access to the given
   *         resource {@code false} otherwise
   */
  public boolean userHasAccess(String userId, String resource);

  /**
   * Checks to see if a user is allowed to make the given websocket call.
   *
   * <p>
   * Implementations of this function should be thread safe.
   *
   * @param userId
   *          the identifier for the user making the specified websocketCall
   * @param websocketCall
   *          the websocket call being attempted by the user.
   * @return {@code true} if the given user is allowed to make the given
   *         websocket call, {@code false} otherwise
   */
  public boolean allowWebsocketCall(String userId, String websocketCall);
}
