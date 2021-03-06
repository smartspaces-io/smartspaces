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

package io.smartspaces.liveactivity.runtime;

import io.smartspaces.activity.ActivityFilesystem;

import java.io.File;

/**
 * An {@link ActivityFilesystem} with some extras needed by the controller.
 *
 * @author Keith M. Hughes
 */
public interface InternalLiveActivityFilesystem extends ActivityFilesystem {

  /**
   * Get the directory which contains internal Smart Spaces data for the
   * activity.
   *
   * @return the directory which contains Smart Spaces data for the
   *         activity
   */
  File getInternalDirectory();

  /**
   * Get an Smart Spaces data file for the activity in the internal
   * Smart Spaces folder.
   *
   * @param relative
   *          relative path for the file
   *
   * @return the requested file
   */
  File getInternalFile(String relative);
}
