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
import io.smartspaces.domain.basic.LiveActivityGroupLiveActivity;
import io.smartspaces.domain.basic.LiveActivityGroupLiveActivity.LiveActivityGroupLiveActivityDependencyType;
import io.smartspaces.domain.basic.LiveActivity;
import io.smartspaces.domain.basic.LiveActivityGroup;
import io.smartspaces.domain.pojo.SimpleObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
  private List<LiveActivityGroupLiveActivity> activities;

  /**
   * The meta data for this live activity group.
   */
  private Map<String, Object> metadata = new HashMap<>();

  /**
   * Construct a new group.
   */
  public SimpleLiveActivityGroup() {
    activities = new ArrayList<>();
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
  public List<LiveActivityGroupLiveActivity> getLiveActivities() {
    synchronized (activities) {
      return new ArrayList<LiveActivityGroupLiveActivity>(activities);
    }
  }

  @Override
  public LiveActivityGroup addLiveActivity(LiveActivity activity) throws SmartSpacesException {
    return addLiveActivity(activity, LiveActivityGroupLiveActivityDependencyType.REQUIRED);
  }

  @Override
  public LiveActivityGroup addLiveActivity(LiveActivity activity,
      LiveActivityGroupLiveActivityDependencyType dependency) throws SmartSpacesException {
    for (LiveActivityGroupLiveActivity ga : activities) {
      if (ga.getLiveActivity().equals(activity)) {
        throw new SimpleSmartSpacesException("Group already contains activity");
      }
    }

    LiveActivityGroupLiveActivity gactivity = new SimpleLiveActivityGroupLiveActivity();
    gactivity.setLiveActivity(activity);
    gactivity.setLiveActivityGroup(this);
    gactivity.setDependencyType(dependency);

    synchronized (activities) {
      activities.add(gactivity);
    }

    return this;
  }

  @Override
  public void removeLiveActivity(LiveActivity activity) {
    synchronized (activities) {
      for (LiveActivityGroupLiveActivity gactivity : activities) {
        if (activity.equals(gactivity.getLiveActivity())) {
          activities.remove(activity);

          return;
        }
      }
    }
  }

  @Override
  public void clearLiveActivities() {
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
