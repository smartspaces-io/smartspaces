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

package io.smartspaces.service.image.gesture.leapmotion;

import io.smartspaces.logging.ExtendedLog;
import io.smartspaces.service.BaseSupportedService;
import io.smartspaces.service.image.gesture.GestureEndpoint;
import io.smartspaces.service.image.gesture.GestureService;

/**
 * A gesture service using the Leap Motion hardware.
 *
 * @author Keith M. Hughes
 */
public class LeapMotionGestureService extends BaseSupportedService implements GestureService {

  @Override
  public String getName() {
    return GestureService.SERVICE_NAME;
  }

  @Override
  public GestureEndpoint newGestureEndpoint(ExtendedLog log) {
    return new LeapMotionGestureEndpoint(getSpaceEnvironment(), log);
  }

  @Override
  public GestureEndpoint newGestureEndpoint(String host, int port, ExtendedLog log) {
    return new LeapMotionGestureEndpoint(host, port, getSpaceEnvironment(), log);
  }
}
