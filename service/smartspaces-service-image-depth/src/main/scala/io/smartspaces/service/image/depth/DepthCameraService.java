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

package io.smartspaces.service.image.depth;

import java.util.List;

import org.apache.commons.logging.Log;

import io.smartspaces.logging.ExtendedLog;
import io.smartspaces.service.SupportedService;

/**
 * A service for working with depth cameras.
 *
 * @author Keith M. Hughes
 */
public interface DepthCameraService extends SupportedService {

  /**
   * The name for the service.
   */
  String SERVICE_NAME = "image.depth";

  /**
   * Get all depth cameras available.
   *
   * @return identifiers for all cameras available
   */
  List<String> getDepthCamerasAvailable();

  /**
   * Get a new depth camera endpoint. Pick the first camera found.
   *
   * @param log
   *          the logger to use
   *
   * @return a new depth camera endpoint
   */
  UserTrackerDepthCameraEndpoint newUserTrackerDepthCameraEndpoint(ExtendedLog log);

  /**
   * Get a new depth camera endpoint with the specified ID.
   *
   * @param cameraId
   *          the ID for the camera
   * @param log
   *          the logger to use
   *
   * @return a new depth camera endpoint
   */
  UserTrackerDepthCameraEndpoint newUserTrackerDepthCameraEndpoint(String cameraId, ExtendedLog log);

}
