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

package io.smartspaces.activity.image.depth.usertrack;

import java.util.List;

import io.smartspaces.activity.impl.route.BaseRoutableActivity;
import io.smartspaces.interaction.model.entity.TrackedEntity;
import io.smartspaces.interaction.model.entity.TrackedEntityListener;
import io.smartspaces.util.data.dynamic.DynamicObjectBuilder;
import io.smartspaces.util.data.dynamic.StandardDynamicObjectBuilder;
import io.smartspaces.util.geometry.Vector3;

/**
 * An activity that tracks users in front of a depth camera.
 *
 * @author Keith M. Hughes
 */
public class ImageDepthUserTrackerExampleActivity extends BaseRoutableActivity {

  /**
   * Route channel to write on.
   */
  public static final String ROUTE_CHANNEL = "output1";

  /**
   * The message property giving an array of detected entities.
   */
  public static final String MESSAGE_PROPERTY_ENTITIES = "entities";

  /**
   * The message property giving the ID for a particular entity position.
   */
  public static final String MESSAGE_PROPERTY_ENTITY_ID = "id";

  /**
   * The message property giving the x coordinate for a particular entity
   * position.
   */
  public static final String MESSAGE_PROPERTY_ENTITY_X = "x";

  /**
   * The message property giving the y coordinate for a particular entity
   * position.
   */
  public static final String MESSAGE_PROPERTY_ENTITY_Y = "y";

  /**
   * The message property giving the z coordinate for a particular entity
   * position.
   */
  public static final String MESSAGE_PROPERTY_ENTITY_Z = "z";

  @Override
  public void onActivitySetup() {
    getLog().info("Depth camera usertrack activity starting!");

    DepthCameraService service = getSpaceEnvironment().getServiceRegistry()
        .getRequiredService(DepthCameraService.SERVICE_NAME);

    UserTrackerDepthCameraEndpoint endpoint = service.newUserTrackerDepthCameraEndpoint(getLog());
    endpoint.addTrackedEntityListener(new TrackedEntityListener<Vector3>() {

      @Override
      public void onTrackedEntityUpdate(List<TrackedEntity<Vector3>> entities) {
        handleTrackedEntityUpdate(entities);
      }
    });

    addManagedResource(endpoint);
  }

  /**
   * Handle a tracked entity update.
   *
   * @param entities
   *          the entities
   */
  private void handleTrackedEntityUpdate(List<TrackedEntity<Vector3>> entities) {

    if (isActivated()) {
      DynamicObjectBuilder message = new StandardDynamicObjectBuilder();

      message.newArray(MESSAGE_PROPERTY_ENTITIES);

      for (TrackedEntity<Vector3> entity : entities) {
        message.newObject();

        message.setProperty(MESSAGE_PROPERTY_ENTITY_ID, entity.getId());

        Vector3 position = entity.getPosition();
        message.setProperty(MESSAGE_PROPERTY_ENTITY_X, position.getV0());
        message.setProperty(MESSAGE_PROPERTY_ENTITY_Y, position.getV1());
        message.setProperty(MESSAGE_PROPERTY_ENTITY_Z, position.getV2());

        message.up();
      }

      getLog().debug(String.format("Entities detected: %s", message));

      sendRouteMessage(ROUTE_CHANNEL, message);
    }
  }
}
