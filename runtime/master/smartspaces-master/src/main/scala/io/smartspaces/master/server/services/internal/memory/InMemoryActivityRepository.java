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

package io.smartspaces.master.server.services.internal.memory;

import io.smartspaces.domain.basic.Activity;
import io.smartspaces.domain.basic.ActivityConfiguration;
import io.smartspaces.domain.basic.ActivityDependency;
import io.smartspaces.domain.basic.ConfigurationParameter;
import io.smartspaces.domain.basic.LiveActivity;
import io.smartspaces.domain.basic.LiveActivityGroup;
import io.smartspaces.domain.basic.Space;
import io.smartspaces.domain.basic.SpaceController;
import io.smartspaces.domain.basic.pojo.SimpleActivity;
import io.smartspaces.domain.basic.pojo.SimpleActivityConfiguration;
import io.smartspaces.domain.basic.pojo.SimpleActivityDependency;
import io.smartspaces.domain.basic.pojo.SimpleConfigurationParameter;
import io.smartspaces.domain.basic.pojo.SimpleLiveActivity;
import io.smartspaces.domain.basic.pojo.SimpleLiveActivityGroup;
import io.smartspaces.expression.FilterExpression;
import io.smartspaces.master.server.services.BaseActivityRepository;

import com.google.common.collect.Lists;
import org.apache.openjpa.util.UnsupportedException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * All activities installed anywhere in the the environment.
 *
 * @author Keith M. Hughes
 */
public class InMemoryActivityRepository extends BaseActivityRepository {

  /**
   * A map of activities keyed by their ID.
   */
  private final Map<String, Activity> activitiesById = new HashMap<>();

  /**
   * A map of installed activities keyed by their ID.
   */
  private final Map<String, LiveActivity> liveActivitiesById = new HashMap<>();

  /**
   * A map of installed activities keyed by their UUID.
   */
  private final Map<String, LiveActivity> liveActivitiesByUuid = new HashMap<>();

  /**
   * A map of activity groups keyed by their ID.
   */
  private final Map<String, LiveActivityGroup> activityGroupsById = new HashMap<>();

  @Override
  public Activity newActivity() {
    return new SimpleActivity();
  }

  @Override
  public ActivityDependency newActivityDependency() {
    return new SimpleActivityDependency();
  }

  @Override
  public ActivityConfiguration newActivityConfiguration() {
    return new SimpleActivityConfiguration();
  }

  @Override
  public ConfigurationParameter newActivityConfigurationParameter() {
    return new SimpleConfigurationParameter();
  }

  @Override
  public long getNumberActivities() {
    synchronized (activitiesById) {
      return activitiesById.size();
    }
  }

  @Override
  public List<Activity> getAllActivities() {
    synchronized (activitiesById) {
      return Lists.newArrayList(activitiesById.values());
    }
  }

  @Override
  public List<Activity> getActivities(FilterExpression filter) {
    List<Activity> result = new ArrayList<>();
    List<Activity> toBeFiltered;

    synchronized (activitiesById) {
      toBeFiltered = Lists.newArrayList(activitiesById.values());
    }

    for (Activity activity : toBeFiltered) {
      if (filter.accept(activity)) {
        result.add(activity);
      }
    }

    return result;
  }

  @Override
  public Activity getActivityById(String id) {
    synchronized (activitiesById) {
      return activitiesById.get(id);
    }
  }

  @Override
  public Activity getActivityByNameAndVersion(String identifyingName, String version) {
    synchronized (activitiesById) {
      for (Activity activity : activitiesById.values()) {
        if (activity.getIdentifyingName().equals(identifyingName)
            && activity.getVersion().equals(version)) {
          return activity;
        }
      }
      return null;
    }
  }

  @Override
  public Activity saveActivity(Activity activity) {
    synchronized (activitiesById) {
      activitiesById.put(activity.getId(), activity);
    }

    return activity;
  }

  @Override
  public void deleteActivity(Activity activity) {
    synchronized (activitiesById) {
      activitiesById.remove(activity.getId());
    }
  }

  @Override
  public LiveActivity newLiveActivity() {
    return new SimpleLiveActivity();
  }

  @Override
  public LiveActivity newAndSaveLiveActivity(SimpleLiveActivity liveActivityTemplate) {
    LiveActivity newLiveActivity = newLiveActivity();

    newLiveActivity.setActivity(liveActivityTemplate.getActivity());
    newLiveActivity.setController(liveActivityTemplate.getController());
    newLiveActivity.setName(liveActivityTemplate.getName());
    newLiveActivity.setDescription(liveActivityTemplate.getDescription());

    return saveLiveActivity(newLiveActivity);
  }

  @Override
  public List<LiveActivity> getAllLiveActivities() {
    synchronized (liveActivitiesById) {
      return Lists.newArrayList(liveActivitiesById.values());
    }
  }

  @Override
  public List<LiveActivity> getLiveActivities(FilterExpression filter) {
    List<LiveActivity> result = new ArrayList<>();
    List<LiveActivity> toBeFiltered;

    synchronized (liveActivitiesById) {
      toBeFiltered = Lists.newArrayList(liveActivitiesById.values());
    }

    for (LiveActivity activity : toBeFiltered) {
      if (filter.accept(activity)) {
        result.add(activity);
      }
    }

    return result;
  }

  @Override
  public LiveActivity getLiveActivityById(String id) {
    synchronized (liveActivitiesById) {
      return liveActivitiesById.get(id);
    }
  }

  @Override
  public LiveActivity getLiveActivityByUuid(String uuid) {
    synchronized (liveActivitiesById) {
      return liveActivitiesByUuid.get(uuid);
    }
  }

  @Override
  public List<LiveActivity> getLiveActivitiesByController(SpaceController controller) {
    List<LiveActivity> results = new ArrayList<>();

    synchronized (liveActivitiesById) {
      for (LiveActivity iactivity : liveActivitiesById.values()) {
        if (iactivity.getController() == controller) {
          results.add(iactivity);
        }
      }
    }

    return results;
  }

  @Override
  public List<LiveActivity> getLiveActivitiesByActivity(Activity activity) {
    List<LiveActivity> results = new ArrayList<>();

    synchronized (liveActivitiesById) {
      for (LiveActivity iactivity : liveActivitiesById.values()) {
        if (iactivity.getActivity() == activity) {
          results.add(iactivity);
        }
      }
    }

    return results;
  }

  @Override
  public LiveActivity saveLiveActivity(LiveActivity activity) {
    synchronized (liveActivitiesById) {
      liveActivitiesById.put(activity.getId(), activity);
      liveActivitiesByUuid.put(activity.getUuid(), activity);
    }

    return activity;
  }

  @Override
  public void deleteLiveActivity(LiveActivity activity) {
    synchronized (liveActivitiesById) {
      liveActivitiesById.remove(activity.getId());
      liveActivitiesByUuid.remove(activity.getUuid());
    }
  }

  @Override
  public LiveActivityGroup newLiveActivityGroup() {
    return new SimpleLiveActivityGroup();
  }

  @Override
  public List<LiveActivityGroup> getAllLiveActivityGroups() {
    synchronized (activityGroupsById) {
      return new ArrayList<LiveActivityGroup>(activityGroupsById.values());
    }
  }

  @Override
  public List<LiveActivityGroup> getLiveActivityGroups(FilterExpression filter) {
    List<LiveActivityGroup> result = new ArrayList<>();
    List<LiveActivityGroup> toBeFiltered;

    synchronized (activitiesById) {
      toBeFiltered = new ArrayList<LiveActivityGroup>(activityGroupsById.values());
    }

    for (LiveActivityGroup group : toBeFiltered) {
      if (filter.accept(group)) {
        result.add(group);
      }
    }

    return result;
  }

  @Override
  public LiveActivityGroup getLiveActivityGroupById(String id) {
    synchronized (activityGroupsById) {
      return activityGroupsById.get(id);
    }
  }

  @Override
  public List<LiveActivityGroup> getLiveActivityGroupsByLiveActivity(LiveActivity liveActivity) {
    // TODO(keith): Implement this.
    throw new UnsupportedOperationException("Not implemented yet");
  }

  @Override
  public LiveActivityGroup saveLiveActivityGroup(LiveActivityGroup activityGroup) {
    synchronized (activityGroupsById) {
      activityGroupsById.put(activityGroup.getId(), activityGroup);
    }

    return activityGroup;
  }

  @Override
  public void deleteLiveActivityGroup(LiveActivityGroup activityGroup) {
    synchronized (activityGroupsById) {
      activityGroupsById.remove(activityGroup.getId());
    }
  }

  @Override
  public long getNumberLiveActivitiesByController(SpaceController controller) {
    throw new UnsupportedException("Not currently supported");
  }

  @Override
  public long getNumberLiveActivitiesByActivity(Activity activity) {
    throw new UnsupportedException("Not currently supported");
  }

  @Override
  public long getNumberLiveActivityGroupsByLiveActivity(LiveActivity liveActivity) {
    throw new UnsupportedException("Not currently supported");
  }

  @Override
  public Space newSpace() {
    throw new UnsupportedException("Not currently supported");
  }

  @Override
  public List<Space> getAllSpaces() {
    throw new UnsupportedException("Not currently supported");
  }

  @Override
  public List<Space> getSpaces(FilterExpression filter) {
    throw new UnsupportedException("Not currently supported");
  }

  @Override
  public Space getSpaceById(String id) {
    throw new UnsupportedException("Not currently supported");
  }

  @Override
  public List<Space> getSpacesByLiveActivityGroup(LiveActivityGroup liveActivityGroup) {
    throw new UnsupportedException("Not currently supported");
  }

  @Override
  public List<Space> getSpacesBySubspace(Space subspace) {
    throw new UnsupportedException("Not currently supported");
  }

  @Override
  public long getNumberSpacesBySubspace(Space subspace) {
    throw new UnsupportedException("Not currently supported");
  }

  @Override
  public Space saveSpace(Space space) {
    throw new UnsupportedException("Not currently supported");
  }

  @Override
  public void deleteSpace(Space space) {
    throw new UnsupportedException("Not currently supported");
  }

  @Override
  public long getNumberSpacesByLiveActivityGroup(LiveActivityGroup liveActivityGroup) {
    throw new UnsupportedException("Not currently supported");
  }
}
