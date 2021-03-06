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

package io.smartspaces.master.ui.internal.web.activity;

import io.smartspaces.master.server.services.ActivityRepository;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.webflow.core.FlowException;
import org.springframework.webflow.execution.FlowExecutionOutcome;
import org.springframework.webflow.execution.repository.NoSuchFlowExecutionException;
import org.springframework.webflow.mvc.servlet.AbstractFlowHandler;

/**
 * Spring Webflow handler for new activities.
 *
 * @author Keith M. Hughes
 */
public class ActivityUploadFlowHandler extends AbstractFlowHandler {

	/**
	 * The default URL for the flow.
	 */
  private static final String DEFAULT_URL = "/activity/all.html";

  /**
   * The repository for activities.
   */
  private ActivityRepository activityRepository;

  @Override
  public String handleExecutionOutcome(FlowExecutionOutcome outcome, HttpServletRequest request,
      HttpServletResponse response) {
    return DEFAULT_URL;
  }

  @Override
  public String handleException(FlowException e, HttpServletRequest request,
      HttpServletResponse response) {
    if (e instanceof NoSuchFlowExecutionException) {
      return DEFAULT_URL;
    } else {
      throw e;
    }
  }

  /**
   * @return the activityRepository
   */
  public ActivityRepository getActivityRepository() {
    return activityRepository;
  }

  /**
   * @param activityRepository
   *          the activityRepository to set
   */
  public void setActivityRepository(ActivityRepository activityRepository) {
    this.activityRepository = activityRepository;
  }

}
