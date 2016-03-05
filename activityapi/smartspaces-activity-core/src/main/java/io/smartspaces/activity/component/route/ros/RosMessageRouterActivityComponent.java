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

package io.smartspaces.activity.component.route.ros;

import io.smartspaces.activity.component.ActivityComponent;
import io.smartspaces.activity.component.ros.RosActivityComponent;
import io.smartspaces.activity.component.route.MessageRouterActivityComponent;

import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.Map;

/**
 * An {@link ActivityComponent} instance which supports multiple named message
 * topics which can send and receive a given ROS message.
 *
 * @param <T>
 *          the type of messages handled by the component
 *
 * @author Keith M. Hughes
 */
public interface RosMessageRouterActivityComponent extends MessageRouterActivityComponent {

  /**
   * Name of the component.
   */
  String COMPONENT_NAME = "comm.router.ros";

  /**
   * Description of this component.
   */
  String COMPONENT_DESCRIPTION = "ROS Message Router";

  /**
   * Dependencies for the component.
   */
  List<String> BASE_COMPONENT_DEPENDENCIES = ImmutableList.of(RosActivityComponent.COMPONENT_NAME);
}
