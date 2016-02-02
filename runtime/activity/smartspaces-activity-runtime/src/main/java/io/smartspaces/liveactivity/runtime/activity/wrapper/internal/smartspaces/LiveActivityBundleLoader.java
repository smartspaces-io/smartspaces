/*
 * Copyright (C) 2016 Keith M. Hughes
 * Copyright (C) 2013 Google Inc.
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

package io.smartspaces.liveactivity.runtime.activity.wrapper.internal.smartspaces;

import io.smartspaces.liveactivity.runtime.domain.InstalledLiveActivity;

import java.io.File;

import org.osgi.framework.Bundle;

/**
 * Loads classes from bundles.
 *
 * <p>
 * This class makes sure that bundles are loaded and unloaded while trying to
 * make sure the proper class is obtained. This means bundles can be shared as
 * long as they are the same bundle.
 *
 * @author Keith M. Hughes
 */
public interface LiveActivityBundleLoader {

  /**
   * Get the OSGi bundle for a live activity.
   *
   * @param liveActivity
   *          the live activity
   * @param bundleFile
   *          the bundle file for the live activity
   *
   * @return the OSGi bundle
   */
  Bundle loadLiveActivityBundle(InstalledLiveActivity liveActivity, File bundleFile);

  /**
   * Dismiss an activity bundle.
   *
   * @param liveActivity
   *          the live activity
   */
  void dismissLiveActivityBundle(InstalledLiveActivity liveActivity);
}
