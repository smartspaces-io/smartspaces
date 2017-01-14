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

package io.smartspaces.activity.impl.route;

import io.smartspaces.activity.Activity;
import io.smartspaces.messaging.bridge.MessageBridge;
import io.smartspaces.messaging.bridge.MessageBridgeFactory;
import io.smartspaces.messaging.bridge.ros.RosMessageBridgeFactory;
import io.smartspaces.util.io.FileSupport;
import io.smartspaces.util.io.FileSupportImpl;

import java.io.File;

/**
 * An {@link Activity} which provides a bridge between two Smart Spaces
 * topics.
 *
 * @author Keith M. Hughes
 */
public class TopicBridgeRoutableActivity extends BaseRoutableActivity {

  /**
   * Configuration property which gives the locations of the bridge
   * configuration files.
   *
   * <p>
   * Relative files are relative to the app install directory.
   */
  public static final String CONFIGURATION_NAME_BRIDGE_TOPIC_FILE = "space.bridge.topic";

  /**
   * The bridge between two topics.
   */
  private MessageBridge topicMessageBridge;

  /**
   * The file support to use.
   */
  private FileSupport fileSupport = FileSupportImpl.INSTANCE;

  @Override
  public void commonActivitySetup() {
	// TODO(keith): Make this work with the generalized routing
    MessageBridgeFactory messageBridgeFactory = new RosMessageBridgeFactory(/* getMainNode() */ null);

    File confFile =
        fileSupport.newFile(getActivityFilesystem().getInstallDirectory(), getConfiguration()
            .getRequiredPropertyString(CONFIGURATION_NAME_BRIDGE_TOPIC_FILE));

    topicMessageBridge =
        messageBridgeFactory.newMessageBridge(fileSupport.readFile(confFile), getLog());
    topicMessageBridge.startup();
  }

  @Override
  public void commonActivityCleanup() {
    if (topicMessageBridge != null) {
      topicMessageBridge.shutdown();
      topicMessageBridge = null;
    }
  }
}
