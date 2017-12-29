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

package io.smartspaces.master.server.services.internal.jpa.domain;

import io.smartspaces.domain.basic.LiveActivityGroupLiveActivity;
import io.smartspaces.domain.basic.LiveActivity;
import io.smartspaces.domain.basic.LiveActivityGroup;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Version;

/**
 * A JPA implementation of a {@link LiveActivityGroupLiveActivity}.
 *
 * @author Keith M. Hughes
 */
@Entity
@Table(name = "live_activity_group_live_activities")
public class JpaLiveActivityGroupLiveActivity implements LiveActivityGroupLiveActivity {

  /**
   * For serialization.
   */
  private static final long serialVersionUID = -420426607253663965L;

  /**
   * The activity group this activity is part of.
   */
  @ManyToOne(optional = false, fetch = FetchType.EAGER)
  private JpaLiveActivityGroup liveActivityGroup;

  /**
   * The activity this represents.
   */
  @ManyToOne(optional = true, fetch = FetchType.EAGER)
  private JpaLiveActivity liveActivity;

  /**
   * How the activity group depends on the activity.
   */
  @Enumerated(EnumType.STRING)
  private LiveActivityGroupLiveActivityDependencyType dependencyType;

  /**
   * The database version. Used for detecting concurrent modifications.
   */
  @Version
  private long databaseVersion;

  public JpaLiveActivityGroupLiveActivity() {
  }

  JpaLiveActivityGroupLiveActivity(JpaLiveActivityGroup liveActivityGroup, JpaLiveActivity liveActivity,
      LiveActivityGroupLiveActivityDependencyType dependencyType) {
    this.liveActivityGroup = liveActivityGroup;
    this.liveActivity = liveActivity;
    this.dependencyType = dependencyType;
  }

  @Override
  public LiveActivityGroup getLiveActivityGroup() {
    return liveActivityGroup;
  }

  @Override
  public void setLiveActivityGroup(LiveActivityGroup activityGroup) {
    this.liveActivityGroup = (JpaLiveActivityGroup) activityGroup;
  }

  @Override
  public LiveActivity getLiveActivity() {
    return liveActivity;
  }

  @Override
  public void setLiveActivity(LiveActivity activity) {
    this.liveActivity = (JpaLiveActivity) activity;
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
    return "JpaLiveActivityGroupLiveActivity [liveActivity=" + liveActivity + ", dependencyType=" + dependencyType + "]";
  }
}
