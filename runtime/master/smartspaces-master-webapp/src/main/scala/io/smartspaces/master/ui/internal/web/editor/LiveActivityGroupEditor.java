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

package io.smartspaces.master.ui.internal.web.editor;

import io.smartspaces.domain.basic.LiveActivityGroup;
import io.smartspaces.master.server.services.ActivityRepository;

import java.beans.PropertyEditorSupport;

/**
 * A property editor for {@link LiveActivityGroup} instances.
 *
 * @author Keith M. Hughes
 */
public class LiveActivityGroupEditor extends PropertyEditorSupport {

  /**
   * Repository for activities.
   */
  private ActivityRepository activityRepository;

  public LiveActivityGroupEditor() {
  }

  public LiveActivityGroupEditor(ActivityRepository activityRepository) {
    this.activityRepository = activityRepository;
  }

  @Override
  public String getAsText() {
    Object o = getValue();

    if (o != null) {
      return ((LiveActivityGroup) o).getId();
    } else
      return null;
  }

  @Override
  public void setAsText(String text) throws IllegalArgumentException {
    if (text != null && text.trim().length() > 0) {
      LiveActivityGroup group = activityRepository.getLiveActivityGroupById(text);
      if (group != null)
        setValue(group);
      else
        throw new IllegalArgumentException("No live activity group with ID " + text);
    } else
      setValue(null);
  }

  /**
   * @param activityRepository
   *          the activityRepository to set
   */
  public void setActivityRepository(ActivityRepository activityRepository) {
    this.activityRepository = activityRepository;
  }
}