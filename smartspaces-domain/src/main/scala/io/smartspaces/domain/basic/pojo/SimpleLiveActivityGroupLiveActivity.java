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
  private LiveActivityGroup activityGroup;

  /**
   * The activity this represents.
   */
  private LiveActivity activity;

  /**
   * How the activity group depends on the activity.
   */
  private LiveActivityGroupLiveActivityDependency dependency;

  @Override
  public LiveActivityGroup getLiveActivityGroup() {
    return activityGroup;
  }

  @Override
  public void setLiveActivityGroup(LiveActivityGroup activityGroup) {
    this.activityGroup = activityGroup;
  }

  @Override
  public LiveActivity getLiveActivity() {
    return activity;
  }

  @Override
  public void setLiveActivity(LiveActivity activity) {
    this.activity = activity;
  }

  @Override
  public LiveActivityGroupLiveActivityDependency getDependency() {
    return dependency;
  }

  @Override
  public void setDependency(LiveActivityGroupLiveActivityDependency dependency) {
    this.dependency = dependency;
  }

}
