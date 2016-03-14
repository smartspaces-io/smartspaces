/*
 * Copyright (C) 2016 Keith M. Hughes
 * Copyright (C) 2015 Google Inc.
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

package io.smartspaces.util.ros;

import io.smartspaces.SimpleSmartSpacesException;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.ros.internal.node.topic.SubscriberIdentifier;
import org.ros.node.ConnectedNode;
import org.ros.node.topic.Publisher;
import org.ros.node.topic.PublisherListener;

/**
 * A collection of ROS publishers for a given message type.
 *
 * @param <T>
 *          the message type
 *
 * @author Keith M. Hughes
 */
public class StandardRosPublishers<T> implements RosPublishers<T> {

  /**
   * All publishers registered.
   */
  private final List<Publisher<T>> publishers = new ArrayList<>();

  /**
   * All publisher listeners registered.
   */
  private final List<PublisherListener<T>> publisherListeners = new ArrayList<>();

  /**
   * Logger for this collection.
   */
  private final Log log;

  /**
   * Construct a new publishers collection.
   *
   * @param log
   *          the logger to use
   */
  public StandardRosPublishers(Log log) {
    this.log = log;
  }

  @Override
  public synchronized void addPublisherListener(PublisherListener<T> listener) {
    publisherListeners.add(listener);

    for (Publisher<T> publisher : publishers) {
      publisher.addListener(listener);
    }
  }

  @Override
  public synchronized void addPublishers(ConnectedNode node, String messageType,
      Set<String> topicNames) {
    log.debug(String.format("Adding publishers for topic names %s with message type %s",
        topicNames, messageType));

    for (String topicName : topicNames) {
      boolean latch = false;
      int semiPos = topicName.indexOf(';');
      if (semiPos != -1) {
        String extra = topicName.substring(0, semiPos);
        topicName = topicName.substring(semiPos + 1);

        String[] pair = extra.split("=");
        if (pair.length > 1) {
          if ("latch".equals(pair[0].trim())) {
            latch = "true".equals(pair[1].trim());
          }
        }
      }

      log.debug(String.format("Adding publisher topic %s", topicName));
      Publisher<T> publisher = node.newPublisher(topicName, messageType);
      log.debug(String.format("Added publisher topic %s", topicName));
      publisher.addListener(this);

      for (PublisherListener<T> listener : publisherListeners) {
        publisher.addListener(listener);
      }

      publisher.setLatchMode(latch);
      publishers.add(publisher);
    }
  }

  @Override
  public synchronized void publishMessage(T message) {
    for (Publisher<T> publisher : publishers) {
      publisher.publish(message);
    }
  }

  @Override
  public synchronized T newMessage() {
    if (!publishers.isEmpty()) {
      return publishers.get(0).newMessage();
    } else {
      throw new SimpleSmartSpacesException("No publishers found to create a message");
    }
  }

  @Override
  public synchronized void shutdown() {
    for (Publisher<T> publisher : publishers) {
      publisher.shutdown();
    }
  }

  @Override
  public void onMasterRegistrationFailure(Publisher<T> publisher) {
    log.warn(String.format("Publisher for topic %s has failed to register with the master",
        publisher.getTopicName()));
  }

  @Override
  public void onMasterRegistrationSuccess(Publisher<T> publisher) {
    log.debug(String.format("Publisher for topic %s has successfully registered with the master",
        publisher.getTopicName()));
  }

  @Override
  public void onMasterUnregistrationFailure(Publisher<T> publisher) {
    log.warn(String.format("Publisher for topic %s has failed to unregister with the master",
        publisher.getTopicName()));
  }

  @Override
  public void onMasterUnregistrationSuccess(Publisher<T> publisher) {
    log.debug(String.format("Publisher for topic %s has successfully unregistered with the master",
        publisher.getTopicName()));
  }

  @Override
  public void onNewSubscriber(Publisher<T> publisher, SubscriberIdentifier subscriberIdentifier) {
    log.debug(String.format("Publisher for topic %s has a new subscriber", publisher.getTopicName()));
  }

  @Override
  public void onShutdown(Publisher<T> publisher) {
    log.debug(String.format("Publisher for topic %s has shut down", publisher.getTopicName()));
  }
}
