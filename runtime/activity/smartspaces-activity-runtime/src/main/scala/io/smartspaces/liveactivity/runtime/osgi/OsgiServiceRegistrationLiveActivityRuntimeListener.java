/*
 * Copyright (C) 2016 Keith M. Hughes
 * Copyright (C) 2015 Google Inc.
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

package io.smartspaces.liveactivity.runtime.osgi;

import io.smartspaces.activity.binary.NativeActivityRunnerFactory;
import io.smartspaces.liveactivity.runtime.BaseLiveActivityRuntimeListener;
import io.smartspaces.liveactivity.runtime.LiveActivityRunnerFactory;
import io.smartspaces.liveactivity.runtime.LiveActivityRuntime;
import io.smartspaces.system.osgi.SmartSpacesOsgiBundleActivator;

/**
 * A listener for live activity runtimes that registers live activity runtime
 * services with OSGi.
 *
 * @author Keith M. Hughes
 */
public class OsgiServiceRegistrationLiveActivityRuntimeListener extends
    BaseLiveActivityRuntimeListener {

  /**
   * The bundle activator to be used for registering services.
   */
  private SmartSpacesOsgiBundleActivator bundleActivator;

  /**
   * Construct a new listener.
   *
   * @param bundleActivator
   *          the bundle activator to be used for registering services
   */
  public OsgiServiceRegistrationLiveActivityRuntimeListener(
      SmartSpacesOsgiBundleActivator bundleActivator) {
    this.bundleActivator = bundleActivator;
  }

  @Override
  public void onLiveActivityRuntimeStartup(LiveActivityRuntime runtime) {

    LiveActivityRunnerFactory liveActivityRunnerFactory = runtime.getLiveActivityRunnerFactory();
    bundleActivator.registerOsgiFrameworkService(LiveActivityRunnerFactory.class.getName(),
        liveActivityRunnerFactory);

    NativeActivityRunnerFactory nativeActivityRunnerFactory =
        runtime.getNativeActivityRunnerFactory();
    bundleActivator.registerOsgiFrameworkService(NativeActivityRunnerFactory.class.getName(),
        nativeActivityRunnerFactory);
  }
}
