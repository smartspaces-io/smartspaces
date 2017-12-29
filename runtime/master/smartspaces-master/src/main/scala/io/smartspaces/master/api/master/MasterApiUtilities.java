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

package io.smartspaces.master.api.master;

import java.util.Comparator;

import io.smartspaces.domain.basic.Activity;
import io.smartspaces.domain.basic.LiveActivity;
import io.smartspaces.domain.basic.LiveActivityGroup;
import io.smartspaces.domain.basic.LiveActivityGroupLiveActivity;
import io.smartspaces.domain.basic.Resource;
import io.smartspaces.domain.basic.Space;
import io.smartspaces.domain.basic.SpaceController;
import io.smartspaces.domain.system.NamedScript;
import io.smartspaces.master.server.services.model.ActiveSpaceController;

/**
 * Utilities for the master side for working with the master.
 *
 * @author Keith M. Hughes
 */
public class MasterApiUtilities {

  /**
   * A comparator for controllers that orders by name.
   */
  public static final SpaceControllerByNameComparator SPACE_CONTROLLER_BY_NAME_COMPARATOR =
      new SpaceControllerByNameComparator();

  /**
   * A comparator for activities that orders by name.
   */
  public static final ActivityByNameAndVersionComparator ACTIVITY_BY_NAME_AND_VERSION_COMPARATOR =
      new ActivityByNameAndVersionComparator();

  /**
   * A comparator for live activities that orders by name.
   */
  public static final LiveActivityByNameComparator LIVE_ACTIVITY_BY_NAME_COMPARATOR =
      new LiveActivityByNameComparator();

  /**
   * A comparator for live activity groups that orders by name.
   */
  public static final LiveActivityGroupByNameComparator LIVE_ACTIVITY_GROUP_BY_NAME_COMPARATOR =
      new LiveActivityGroupByNameComparator();

  /**
   * A comparator for live activities for a live activity group that orders by
   * name.
   */
  public static final LiveActivityGroupLiveActivityByNameComparator LIVE_ACTIVITY_GROUP_LIVE_ACTIVITY_BY_NAME_COMPARATOR =
      new LiveActivityGroupLiveActivityByNameComparator();

  /**
   * A comparator for spaces that orders by name.
   */
  public static final SpaceByNameComparator SPACE_BY_NAME_COMPARATOR = new SpaceByNameComparator();

  /**
   * A comparator for active controllers that orders by name.
   */
  public static final ActiveControllerByNameComparator ACTIVE_CONTROLLER_BY_NAME_COMPARATOR =
      new ActiveControllerByNameComparator();

  /**
   * A comparator for resources that orders by name.
   */
  public static final ResourceByNameAndVersionComparator RESOURCE_BY_NAME_AND_VERSION_COMPARATOR =
      new ResourceByNameAndVersionComparator();

  /**
   * A comparator for named scripts that orders by name.
   */
  public static final NamedScriptByNameComparator NAMED_SCRIPT_BY_NAME_COMPARATOR =
      new NamedScriptByNameComparator();

  /**
   * A comparator for installed activity that orders by name.
   *
   * @author Keith M. Hughes
   */
  private static class SpaceControllerByNameComparator implements Comparator<SpaceController> {
    @Override
    public int compare(SpaceController o1, SpaceController o2) {
      return o1.getName().compareToIgnoreCase(o2.getName());
    }
  }

  /**
   * A comparator for resources that orders by name and then version.
   *
   * @author Keith M. Hughes
   */
  private static class ResourceByNameAndVersionComparator implements Comparator<Resource> {
    @Override
    public int compare(Resource o1, Resource o2) {
      int compare = o1.getIdentifyingName().compareToIgnoreCase(o2.getIdentifyingName());

      if (compare == 0) {
        compare = o1.getVersion().compareTo(o2.getVersion());
      }

      return compare;
    }
  }

  /**
   * A comparator for activities that orders by name first then subsorts by
   * version.
   *
   * @author Keith M. Hughes
   */
  private static class ActivityByNameAndVersionComparator implements Comparator<Activity> {
    @Override
    public int compare(Activity o1, Activity o2) {
      int compare = o1.getName().compareToIgnoreCase(o2.getName());

      if (compare == 0) {
        compare = o1.getVersion().compareTo(o2.getVersion());
      }

      return compare;
    }
  }

  /**
   * A comparator for live activities that orders by name.
   *
   * @author Keith M. Hughes
   */
  private static class LiveActivityByNameComparator implements Comparator<LiveActivity> {
    @Override
    public int compare(LiveActivity o1, LiveActivity o2) {
      return o1.getName().compareToIgnoreCase(o2.getName());
    }
  }

  /**
   * A comparator for live activity groups that orders by name.
   *
   * @author Keith M. Hughes
   */
  private static class LiveActivityGroupByNameComparator implements Comparator<LiveActivityGroup> {
    @Override
    public int compare(LiveActivityGroup o1, LiveActivityGroup o2) {
      return o1.getName().compareToIgnoreCase(o2.getName());
    }
  }

  /**
   * A comparator for live activities in a live activity group that orders by
   * name.
   *
   * @author Keith M. Hughes
   */
  private static class LiveActivityGroupLiveActivityByNameComparator
      implements Comparator<LiveActivityGroupLiveActivity> {
    @Override
    public int compare(LiveActivityGroupLiveActivity o1, LiveActivityGroupLiveActivity o2) {
      return o1.getLiveActivity().getName().compareToIgnoreCase(o2.getLiveActivity().getName());
    }
  }

  /**
   * A comparator for spaces that orders by name.
   *
   * @author Keith M. Hughes
   */
  private static class SpaceByNameComparator implements Comparator<Space> {
    @Override
    public int compare(Space o1, Space o2) {
      return o1.getName().compareToIgnoreCase(o2.getName());
    }
  }

  /**
   * A comparator for active controllers that orders by name.
   *
   * @author Keith M. Hughes
   */
  private static class ActiveControllerByNameComparator
      implements Comparator<ActiveSpaceController> {
    @Override
    public int compare(ActiveSpaceController o1, ActiveSpaceController o2) {
      return o1.spaceController().getName().compareToIgnoreCase(o2.spaceController().getName());
    }
  }

  /**
   * A comparator for named scripts that orders by name.
   *
   * @author Keith M. Hughes
   */
  private static class NamedScriptByNameComparator implements Comparator<NamedScript> {
    @Override
    public int compare(NamedScript o1, NamedScript o2) {
      return o1.getName().compareToIgnoreCase(o2.getName());
    }
  }
}
