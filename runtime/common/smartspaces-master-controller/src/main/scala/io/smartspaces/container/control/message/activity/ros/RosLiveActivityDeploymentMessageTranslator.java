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

package io.smartspaces.container.control.message.activity.ros;

import io.smartspaces.container.control.message.activity.LiveActivityDeploymentRequest;
import io.smartspaces.container.control.message.container.resource.deployment.ContainerResourceDeploymentCommitRequest;
import io.smartspaces.container.control.message.container.resource.deployment.ContainerResourceDeploymentItem;
import io.smartspaces.container.control.message.container.resource.deployment.ContainerResourceDeploymentQueryRequest;
import io.smartspaces.resource.ResourceDependency;
import io.smartspaces.resource.ResourceDependencyReference;
import io.smartspaces.resource.Version;
import io.smartspaces.resource.VersionRange;
import io.smartspaces.system.resources.ContainerResourceLocation;

import org.ros.message.MessageFactory;
import smartspaces_msgs.ContainerResourceCommitRequestMessage;
import smartspaces_msgs.ContainerResourceQueryItem;
import smartspaces_msgs.ContainerResourceQueryRequestMessage;
import smartspaces_msgs.LiveActivityDeployRequestMessage;
import smartspaces_msgs.LocatableResourceDescription;

import java.util.ArrayList;
import java.util.List;

/**
 * A translator from internal messages to and from ROS messages.
 *
 * @author Keith M. Hughes
 */
public class RosLiveActivityDeploymentMessageTranslator {

  /**
   * The value that the detail should have in the ROS message for container
   * resource deployment commit responses if there is no detail.
   */
  public static final String CONTAINER_RESOURCE_DEPLOYMENT_COMMIT_RESPONSE_DETAIL_NONE = "";

  /**
   * The value that the detail should have in the ROS message for container live
   * activity deployment responses if there is no detail.
   */
  public static final String CONTAINER_LIVE_ACTIVITY_DEPLOYMENT_RESPONSE_DETAIL_NONE = "";

  /**
   * Serialize an activity deployment request into a ROS message.
   *
   * @param request
   *          the deployment request
   * @param rosRequest
   *          the ROS message
   */
  public static void serializeActivityDeploymentRequest(LiveActivityDeploymentRequest request,
      LiveActivityDeployRequestMessage rosRequest) {
    rosRequest.setTransactionId(request.getTransactionId());
    rosRequest.setUuid(request.getUuid());
    rosRequest.setIdentifyingName(request.getIdentifyingName());
    rosRequest.setVersion(request.getVersion().toString());
    rosRequest.setActivitySourceUri(request.getActivitySourceUri());
  }

  /**
   * Deserialize an activity deployment request from a ROS message.
   *
   * @param rosMessage
   *          the ROS message
   *
   * @return the deserialized message
   */
  public static LiveActivityDeploymentRequest deserializeActivityDeploymentRequest(
      LiveActivityDeployRequestMessage rosMessage) {
    return new LiveActivityDeploymentRequest(rosMessage.getTransactionId(), rosMessage.getUuid(),
        rosMessage.getIdentifyingName(), Version.parseVersion(rosMessage.getVersion()),
        rosMessage.getActivitySourceUri());
  }

  /**
   * Serialize a resource deployment query.
   *
   * @param query
   *          the query
   * @param rosMessage
   *          the ROS message
   * @param messageFactory
   *          the ROS message factory to use for building components
   */
  public static void serializeResourceDeploymentQuery(
      ContainerResourceDeploymentQueryRequest query,
      ContainerResourceQueryRequestMessage rosMessage, MessageFactory messageFactory) {
    rosMessage.setType(ContainerResourceQueryRequestMessage.TYPE_SPECIFIC_QUERY);
    rosMessage.setTransactionId(query.getTransactionId());

    List<ContainerResourceQueryItem> rosItems = new ArrayList<>();
    for (ResourceDependency dependency : query.getQueries()) {
      ContainerResourceQueryItem rosItem =
          messageFactory.newFromType(ContainerResourceQueryItem._TYPE);
      rosItem.setName(dependency.getName());
      rosItem.setVersionRange(dependency.getVersionRange().toString());

      rosItems.add(rosItem);
    }
    rosMessage.setItems(rosItems);
  }

  /**
   * Deserialize a resource deployment query from its ROS message.
   *
   * @param rosMessage
   *          the ROS message
   *
   * @return the query
   */
  public static ContainerResourceDeploymentQueryRequest
      deserializeContainerResourceDeploymentQuery(ContainerResourceQueryRequestMessage rosMessage) {
    ContainerResourceDeploymentQueryRequest query =
        new ContainerResourceDeploymentQueryRequest(rosMessage.getTransactionId());
    for (ContainerResourceQueryItem rosItem : rosMessage.getItems()) {
      query.addQuery(new ResourceDependencyReference(rosItem.getName(), VersionRange
          .parseVersionRange(rosItem.getVersionRange())));
    }
    return query;
  }

  /**
   * Serialize a resource deployment commit.
   *
   * @param request
   *          the request
   * @param rosMessage
   *          the ROS message
   * @param messageFactory
   *          the ROS message factory to use for building components
   */
  public static void serializeResourceDeploymentCommit(
      ContainerResourceDeploymentCommitRequest request,
      ContainerResourceCommitRequestMessage rosMessage, MessageFactory messageFactory) {
    rosMessage.setTransactionId(request.getTransactionId());

    List<smartspaces_msgs.ContainerResourceDeploymentItem> rosItems = new ArrayList<>();
    for (ContainerResourceDeploymentItem item : request.getItems()) {
      smartspaces_msgs.ContainerResourceDeploymentItem rosItem =
          messageFactory.newFromType(smartspaces_msgs.ContainerResourceDeploymentItem._TYPE);
      LocatableResourceDescription rosResource = rosItem.getResource();
      rosResource.setName(item.getName());
      rosResource.setVersion(item.getVersion().toString());
      rosResource.setSignature(item.getSignature());
      rosResource.setLocationUri(item.getResourceSourceUri());

      // TODO(keith): Translate this properly from the request.
      rosItem.getLocation().setMainLocation(
          smartspaces_msgs.ContainerResourceLocation.LOCATION_USER_BOOTSTRAP);

      rosItems.add(rosItem);
    }
    rosMessage.setItems(rosItems);
  }

  /**
   * Serialize a resource deployment commit.
   *
   * @param rosMessage
   *          the ROS message
   *
   * @return the deserialized request
   */
  public static ContainerResourceDeploymentCommitRequest deserializeResourceDeploymentCommit(
      ContainerResourceCommitRequestMessage rosMessage) {
    ContainerResourceDeploymentCommitRequest request =
        new ContainerResourceDeploymentCommitRequest(rosMessage.getTransactionId());
    rosMessage.setTransactionId(request.getTransactionId());

    for (smartspaces_msgs.ContainerResourceDeploymentItem rosItem : rosMessage.getItems()) {
      LocatableResourceDescription resource = rosItem.getResource();
      // TODO(keith): Translate resource locations from the query.
      request.addItem(new ContainerResourceDeploymentItem(resource.getName(), Version
          .parseVersion(resource.getVersion()), ContainerResourceLocation.USER_BOOTSTRAP, resource
          .getSignature(), resource.getLocationUri()));
    }

    return request;
  }
}
