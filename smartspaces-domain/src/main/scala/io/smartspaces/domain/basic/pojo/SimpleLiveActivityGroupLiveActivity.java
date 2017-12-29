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

import io.smartspaces.domain.basic.LiveActivityGroupLiveActivity;
import io.smartspaces.domain.basic.LiveActivity;
import io.smartspaces.domain.basic.LiveActivityGroup;

/**
 * A POJO implementation of a {@link LiveActivityGroupLiveActivity}.
 *
 * @author Keith M. Hughes
 */
public class SimpleLiveActivityGroupLiveActivity implements LiveActivityGroupLiveActivity {

  /**
   * For serialization.
   */
  private static final long serialVersionUID = 9103616927981825782L;

  /**
   * The activity group this activity is part of.
   */
  private LiveActivityGroup liveActivityGroup;

  /**
   * The activity this represents.
   */
  private LiveActivity liveActivity;

  /**
   * How the activity group depends on the activity.
   */
  private LiveActivityGroupLiveActivityDependencyType dependencyType;
  
  /**
   * Construct a new live activity group live activity.
   * 
   * @param liveActivityGroup
   *          the group this live activity is part of
   * @param liveActivity
   *          the live activity
   * @param dependencyType
   *          the dependency type of the live activity in the group
   */
  public SimpleLiveActivityGroupLiveActivity(LiveActivityGroup liveActivityGroup,
      LiveActivity liveActivity, LiveActivityGroupLiveActivityDependencyType dependencyType) {
    this.liveActivityGroup = liveActivityGroup;
    this.liveActivity = liveActivity;
    this.dependencyType = dependencyType;
  }

  /**
   * Construct a new live activity group live activity with no arguments.
   */
  public SimpleLiveActivityGroupLiveActivity() {
  }

  @Override
  public LiveActivityGroup getLiveActivityGroup() {
    return liveActivityGroup;
  }

  @Override
  public void setLiveActivityGroup(LiveActivityGroup activityGroup) {
    this.liveActivityGroup = activityGroup;
  }

  @Override
  public LiveActivity getLiveActivity() {
    return liveActivity;
  }

  @Override
  public void setLiveActivity(LiveActivity activity) {
    this.liveActivity = activity;
  }

  @Override
  public LiveActivityGroupLiveActivityDependencyType getDependencyType() {
    return dependencyType;
  }

  @Override
  public void setDependencyType(LiveActivityGroupLiveActivityDependencyType dependencyType) {
    this.dependencyType = dependencyType;
  }

  @Override
  public String toString() {
    return "SimpleLiveActivityGroupLiveActivity [liveActivityGroup=" + liveActivityGroup
        + ", liveActivity=" + liveActivity + ", dependencyType=" + dependencyType + "]";
  }
}
