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

package io.smartspaces.master.server.services;

import io.smartspaces.domain.basic.ActivityConfiguration;
import io.smartspaces.domain.basic.ConfigurationParameter;
import io.smartspaces.domain.basic.GroupLiveActivity;
import io.smartspaces.domain.basic.LiveActivity;
import io.smartspaces.domain.basic.LiveActivityGroup;
import io.smartspaces.domain.basic.SpaceController;
import io.smartspaces.domain.space.Space;

import com.google.common.collect.Maps;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Clone Smart Spaces domain objects.
 *
 * <p>
 * An instance of this class should be used for only 1 cloning operation as it
 * keeps much state.
 *
 * @author Keith M. Hughes
 */
public class StandardSmartSpacesDomainCloner implements SmartSpacesDomainCloner {

  /**
   * The activity repository to use during cloning.
   */
  private ActivityRepository activityRepository;

  /**
   * A map from the old live activity to the cloned live activity.
   */
  private Map<String, LiveActivity> liveActivityClones = new HashMap<>();

  /**
   * A map from the old live activity group to the cloned live activity group.
   */
  private Map<String, LiveActivityGroup> liveActivityGroupClones = new HashMap<>();

  /**
   * A map from the old space to the cloned space.
   */
  private Map<String, Space> spaceClones = Maps.newLinkedHashMap();

  /**
   * A map from the old controller to the controller it should go to.
   */
  private Map<SpaceController, SpaceController> controllerMap;

  /**
   * The name prefix to put on all cloned items.
   */
  private String namePrefix;

  /**
   * Construct a cloner.
   *
   * @param activityRepository
   *          the activity repository to use during cloning
   */
  public StandardSmartSpacesDomainCloner(ActivityRepository activityRepository) {
    this.activityRepository = activityRepository;
  }

  @Override
  public void setNamePrefix(String namePrefix) {
    this.namePrefix = namePrefix;
  }

  @Override
  public void setControllerMap(Map<SpaceController, SpaceController> controllerMap) {
    this.controllerMap = controllerMap;
  }

  @Override
  public void saveClones() {
    for (Entry<String, LiveActivity> liveActivity : liveActivityClones.entrySet()) {
      liveActivity.setValue(activityRepository.saveLiveActivity(liveActivity.getValue()));
    }
    for (Entry<String, LiveActivityGroup> liveActivityGroup : liveActivityGroupClones.entrySet()) {
      liveActivityGroup.setValue(activityRepository.saveLiveActivityGroup(liveActivityGroup
          .getValue()));
    }
    for (Entry<String, Space> space : spaceClones.entrySet()) {
      space.setValue(activityRepository.saveSpace(space.getValue()));
    }
  }

  @Override
  public LiveActivity cloneLiveActivity(LiveActivity src) {
    String id = src.getId();
    LiveActivity clone = liveActivityClones.get(id);
    if (clone != null) {
      return clone;
    }

    clone = activityRepository.newLiveActivity();
    liveActivityClones.put(id, clone);

    clone.setName(namePrefix + " " + src.getName());
    clone.setDescription(src.getDescription());
    clone.setActivity(src.getActivity());
    clone.setController(getController(src.getController()));
    clone.setMetadata(src.getMetadata());
    clone.setConfiguration(cloneConfiguration(src.getConfiguration()));

    return clone;
  }

  @Override
  public LiveActivity getClonedLiveActivity(String srcId) {
    return liveActivityClones.get(srcId);
  }

  @Override
  public ActivityConfiguration cloneConfiguration(ActivityConfiguration src) {
    if (src == null) {
      return null;
    }

    ActivityConfiguration clone = activityRepository.newActivityConfiguration();
    String name = src.getName();
    if (name != null) {
      clone.setName(namePrefix + " " + name);
      clone.setDescription(src.getDescription());
    }

    clone.setDescription(src.getDescription());

    for (ConfigurationParameter parameter : src.getParameters()) {
      ConfigurationParameter clonedParameter =
          activityRepository.newActivityConfigurationParameter();
      clonedParameter.setName(parameter.getName());
      clonedParameter.setValue(parameter.getValue());
      clone.addParameter(clonedParameter);
    }

    return clone;
  }

  @Override
  public LiveActivityGroup cloneLiveActivityGroup(LiveActivityGroup src) {
    String id = src.getId();
    LiveActivityGroup clone = liveActivityGroupClones.get(id);
    if (clone != null) {
      return clone;
    }

    clone = activityRepository.newLiveActivityGroup();
    liveActivityGroupClones.put(id, clone);

    clone.setName(namePrefix + " " + src.getName());
    clone.setDescription(src.getDescription());
    clone.setMetadata(src.getMetadata());

    for (GroupLiveActivity activity : src.getLiveActivities()) {
      LiveActivity activityClone = cloneLiveActivity(activity.getActivity());
      clone.addLiveActivity(activityClone, activity.getDependency());
    }

    return clone;
  }

  @Override
  public LiveActivityGroup getClonedLiveActivityGroup(String srcId) {
    return liveActivityGroupClones.get(srcId);
  }

  @Override
  public Space cloneSpace(Space src) {
    String id = src.getId();
    Space clone = spaceClones.get(id);
    if (clone != null) {
      return clone;
    }

    clone = activityRepository.newSpace();

    clone.setName(namePrefix + " " + src.getName());
    clone.setDescription(src.getDescription());
    clone.setMetadata(src.getMetadata());

    for (Space subspace : src.getSpaces()) {
      Space subspaceClone = cloneSpace(subspace);
      clone.addSpace(subspaceClone);
    }

    for (LiveActivityGroup group : src.getActivityGroups()) {
      LiveActivityGroup groupClone = cloneLiveActivityGroup(group);
      clone.addActivityGroup(groupClone);
    }

    spaceClones.put(id, clone);

    return clone;
  }

  @Override
  public Space getClonedSpace(String srcId) {
    return spaceClones.get(srcId);
  }

  /**
   * Get the controller to replace the old controller.
   *
   * @param oldController
   *          the controller from the source elements
   *
   * @return the controller which should be used
   */
  private SpaceController getController(SpaceController oldController) {
    if (controllerMap != null) {
      SpaceController newController = controllerMap.get(oldController);
      if (newController != null) {
        return newController;
      }
    }

    return oldController;
  }
}
