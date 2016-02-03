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

package io.smartspaces.network.client;

import io.smartspaces.network.NetworkNodeInformation;
import io.smartspaces.network.NetworkTopicInformation;

import java.util.List;

/**
 * A client for getting information about the Smart Spaces network.
 *
 * @author Keith M. Hughes
 */
public interface NetworkInformationClient {

  /**
   * Start the client up.
   */
  void startup();

  /**
   * Shut the client down.
   */
  void shutdown();

  /**
   * Get a list of all topics in the network.
   *
   * @return list of topics sorted by name.
   */
  List<NetworkTopicInformation> getTopics();

  /**
   * Get a list of all nodes in the network.
   *
   * @return list of nodes sorted by name.
   */
  List<NetworkNodeInformation> getNodes();
}