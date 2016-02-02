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

package io.smartspaces.domain.basic.pojo;

import io.smartspaces.SimpleSmartSpacesException;
import io.smartspaces.SmartSpacesException;
import io.smartspaces.domain.basic.GroupLiveActivity;
import io.smartspaces.domain.basic.LiveActivity;
import io.smartspaces.domain.basic.LiveActivityGroup;
import io.smartspaces.domain.basic.GroupLiveActivity.GroupLiveActivityDependency;
import io.smartspaces.domain.pojo.SimpleObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * A POJO implementation of a {@link LiveActivityGroup}.
 *
 * @author Keith M. Hughes
 */
public class SimpleLiveActivityGroup extends SimpleObject implements LiveActivityGroup {

  /**
   * For serialization.
   */
  private static final long serialVersionUID = 7892829844156195326L;

  /**
   * The name of the group.
   */
  private String name;

  /**
   * The description of the group.
   */
  private String description;

  /**
   * All activities installed in the activity group.
   */
  private List<GroupLiveActivity> activities;

  /**
   * The meta data for this live activity group.
   */
  private Map<String, Object> metadata = Maps.newHashMap();

  /**
   * Construct a new group.
   */
  public SimpleLiveActivityGroup() {
    activities = Lists.newArrayList();
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public void setName(String name) {
    this.name = name;
  }

  @Override
  public String getDescription() {
    return description;
  }

  @Override
  public void setDescription(String description) {
    this.description = description;
  }

  @Override
  public List<GroupLiveActivity> getLiveActivities() {
    synchronized (activities) {
      return new ArrayList<GroupLiveActivity>(activities);
    }
  }

  @Override
  public LiveActivityGroup addLiveActivity(LiveActivity activity) throws SmartSpacesException {
    return addLiveActivity(activity, GroupLiveActivityDependency.REQUIRED);
  }

  @Override
  public LiveActivityGroup addLiveActivity(LiveActivity activity,
      GroupLiveActivityDependency dependency) throws SmartSpacesException {
    for (GroupLiveActivity ga : activities) {
      if (ga.getActivity().equals(activity)) {
        throw new SimpleSmartSpacesException("Group already contains activity");
      }
    }

    GroupLiveActivity gactivity = new SimpleGroupLiveActivity();
    gactivity.setActivity(activity);
    gactivity.setActivityGroup(this);
    gactivity.setDependency(dependency);

    synchronized (activities) {
      activities.add(gactivity);
    }

    return this;
  }

  @Override
  public void removeLiveActivity(LiveActivity activity) {
    synchronized (activities) {
      for (GroupLiveActivity gactivity : activities) {
        if (activity.equals(gactivity.getActivity())) {
          activities.remove(activity);

          return;
        }
      }
    }
  }

  @Override
  public void clearActivities() {
    activities.clear();
  }

  @Override
  public void setMetadata(Map<String, Object> metadata) {
    this.metadata = metadata;
  }

  @Override
  public Map<String, Object> getMetadata() {
    return metadata;
  }
}
