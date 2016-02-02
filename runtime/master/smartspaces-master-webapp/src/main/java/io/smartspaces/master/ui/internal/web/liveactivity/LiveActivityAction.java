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

package io.smartspaces.master.ui.internal.web.liveactivity;

import io.smartspaces.domain.basic.Activity;
import io.smartspaces.domain.basic.LiveActivity;
import io.smartspaces.domain.basic.SpaceController;
import io.smartspaces.domain.basic.pojo.SimpleLiveActivity;
import io.smartspaces.master.server.services.ActivityRepository;
import io.smartspaces.master.server.services.SpaceControllerRepository;
import io.smartspaces.master.ui.internal.web.BaseSpaceMasterController;
import io.smartspaces.master.ui.internal.web.WebSupport;

import java.io.Serializable;

import org.springframework.webflow.core.collection.MutableAttributeMap;
import org.springframework.webflow.execution.RequestContext;

/**
 * Webflow action for live activities.
 *
 * @author Keith M. Hughes
 */
public class LiveActivityAction extends BaseSpaceMasterController {

  /**
   * Repository for activities.
   */
  private ActivityRepository activityRepository;

  /**
   * Repository for space controllers.
   */
  private SpaceControllerRepository spaceControllerRepository;

  /**
   * Create a new live activity form.
   *
   * @return a new form
   */
  public LiveActivityForm newLiveActivity() {
    return new LiveActivityForm();
  }

  /**
   * Add entities to the flow context needed by the new entity page.
   *
   * @param context
   *          The Webflow context.
   */
  public void addNeededEntities(RequestContext context) {
    MutableAttributeMap viewScope = context.getViewScope();
    addGlobalModelItems(viewScope);

    viewScope.put("activities",
        WebSupport.getActivitySelections(activityRepository.getAllActivities()));
    viewScope.put("controllers",
        WebSupport.getControllerSelections(spaceControllerRepository.getAllSpaceControllers()));
  }

  /**
   * Save the new live activity.
   *
   * @param form
   *          the live activity form
   */
  public void saveLiveActivity(LiveActivityForm form) {
    SimpleLiveActivity liveactivity = form.getLiveActivity();

    LiveActivity finalLiveActivity = activityRepository.newLiveActivity();
    finalLiveActivity.setName(liveactivity.getName());
    finalLiveActivity.setDescription(liveactivity.getDescription());

    SpaceController controller =
        spaceControllerRepository.getSpaceControllerById(form.getControllerId());
    finalLiveActivity.setController(controller);

    Activity activity = activityRepository.getActivityById(form.getActivityId());
    finalLiveActivity.setActivity(activity);

    activityRepository.saveLiveActivity(finalLiveActivity);

    // So the ID gets copied out of the flow.
    liveactivity.setId(finalLiveActivity.getId());
  }

  /**
   * @param activityRepository
   *          the activityRepository to set
   */
  public void setActivityRepository(ActivityRepository activityRepository) {
    this.activityRepository = activityRepository;
  }

  /**
   * Set the space controller repository to use.
   *
   * @param spaceControllerRepository
   *          the space controller repository
   */
  public void setSpaceControllerRepository(SpaceControllerRepository spaceControllerRepository) {
    this.spaceControllerRepository = spaceControllerRepository;
  }

  /**
   * Form bean to keep all info about the installed activity.
   *
   * @author Keith M. Hughes
   */
  public static class LiveActivityForm implements Serializable {

    /**
     * The serialization ID.
     */
    private static final long serialVersionUID = -8354616250551919707L;

    /**
     * Info about the installed activity.
     */
    private SimpleLiveActivity liveActivity = new SimpleLiveActivity();

    /**
     * The ID of the controller.
     */
    private String controllerId;

    /**
     * The ID of the activity.
     */
    private String activityId;

    /**
     * @return the liveActivity
     */
    public SimpleLiveActivity getLiveActivity() {
      return liveActivity;
    }

    /**
     * @param liveActivity
     *          the liveActivity to set
     */
    public void setLiveActivity(SimpleLiveActivity liveActivity) {
      this.liveActivity = liveActivity;
    }

    /**
     * @return the controllerId
     */
    public String getControllerId() {
      return controllerId;
    }

    /**
     * @param controllerId
     *          the controllerId to set
     */
    public void setControllerId(String controllerId) {
      this.controllerId = controllerId;
    }

    /**
     * @return the activityId
     */
    public String getActivityId() {
      return activityId;
    }

    /**
     * @param activityId
     *          the activityId to set
     */
    public void setActivityId(String activityId) {
      this.activityId = activityId;
    }
  }
}
