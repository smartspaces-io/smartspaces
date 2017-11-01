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

package io.smartspaces.master.api.master;

import io.smartspaces.resource.managed.ManagedResource;

/**
 * A communication manager that provides access to the Master API.
 *
 * @author Keith M. Hughes
 */
public interface MasterApiCommunicationManager extends ManagedResource {

  /**
   * Prefix for the master API websocket endpoint.
   */
  String MASTERAPI_WEBSOCKET_URI_PREFIX = "/masterapi/websocket";

  /**
   * URI prefix for activity uploads.
   */
  String MASTERAPI_PATH_PREFIX_ACTIVITY_UPLOAD = "/masterapi/activity/upload";
}
