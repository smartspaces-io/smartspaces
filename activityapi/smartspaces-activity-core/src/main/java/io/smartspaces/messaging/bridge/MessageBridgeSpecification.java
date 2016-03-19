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

package io.smartspaces.messaging.bridge;

/**
 * An executable specification of a bridge between two message types.
 *
 * @param <SourceMessage>
 *          the class of the source message
 * @param <DestinationMessage>
 *          the class of the destination message
 *
 * @author Keith M. Hughes
 */

public interface MessageBridgeSpecification<SourceMessage, DestinationMessage> {

  /**
   * Get the source topic name.
   *
   * @return the source topic name
   */
  String getSourceTopicName();

  /**
   * Get the destination topic name.
   *
   * @return the destination topic name
   */
  String getDestinationTopicName();

  /**
   * Execute the bridge.
   *
   * @param source
   *          the source message to translate from
   * @param destination
   *          the destination message to translate to
   */
  void execute(SourceMessage source, DestinationMessage destination);
}
