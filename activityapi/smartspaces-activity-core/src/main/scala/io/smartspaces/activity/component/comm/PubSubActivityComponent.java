/*
 * Copyright (C) 2017 Keith M. Hughes
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

package io.smartspaces.activity.component.comm;

import io.smartspaces.activity.component.ActivityComponent;

/**
 * An {@link ActivityComponent} that gives pubsub functionality.
 *
 * @author Keith M. Hughes
 */
public interface PubSubActivityComponent extends ActivityComponent {

  /**
   * Configuration property for specifying the Smart Spaces ROS node name
   * for the activity.
   */
  String CONFIGURATION_NAME_ACTIVITY_PUBSUB_NODE_NAME = "space.activity.pubsub.node.name";
  
  /**
   * The separator for topic components.
   */
  String TOPIC_COMPONENT_SEPARATOR = "/";
}