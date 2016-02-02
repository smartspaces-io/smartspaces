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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import io.smartspaces.activity.Activity;
import io.smartspaces.activity.ActivityFilesystem;
import io.smartspaces.activity.configuration.ActivityConfiguration;
import io.smartspaces.activity.impl.BaseActivity;
import io.smartspaces.configuration.Configuration;
import io.smartspaces.liveactivity.runtime.activity.wrapper.internal.smartspaces.LiveActivityBundleLoader;
import io.smartspaces.liveactivity.runtime.activity.wrapper.internal.smartspaces.SmartSpacesNativeActivityWrapper;
import io.smartspaces.liveactivity.runtime.domain.InstalledLiveActivity;
import io.smartspaces.liveactivity.runtime.domain.pojo.SimpleInstalledLiveActivity;
import io.smartspaces.resource.Version;

import java.io.File;

import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Bundle;

/**
 * Unit tests for the {@link SmartSpacesNativeActivityWrapper}.
 *
 * @author Keith M. Hughes
 */
public class SmartSpacesNativeActivityWrapperTest {

  private InstalledLiveActivity liveActivity;
  private ActivityFilesystem activityFilesystem;
  private Configuration configuration;
  private LiveActivityBundleLoader bundleLoader;

  private SmartSpacesNativeActivityWrapper wrapper;

  private File activityInstallFolder;

  @Before
  public void setup() {
    liveActivity = new SimpleInstalledLiveActivity();

    activityFilesystem = mock(ActivityFilesystem.class);

    activityInstallFolder = new File("activityinstall");
    when(activityFilesystem.getInstallDirectory()).thenReturn(activityInstallFolder);

    configuration = mock(Configuration.class);
    bundleLoader = mock(LiveActivityBundleLoader.class);

    wrapper =
        new SmartSpacesNativeActivityWrapper(liveActivity, activityFilesystem, configuration,
            bundleLoader);
  }

  /**
   * Test if a single new activity works.
   */
  @SuppressWarnings("unchecked")
  @Test
  public void testSingleNew() throws Exception {
    String executable1 = "foo.jar";
    File executableFile1 = new File(activityInstallFolder, executable1);

    String bundleName = "foop";
    Version bundleVersion = new Version(1, 0, 0);

    liveActivity.setIdentifyingName(bundleName);
    liveActivity.setVersion(bundleVersion);

    String className = "Activity1";

    when(
        configuration
            .getRequiredPropertyString(ActivityConfiguration.CONFIGURATION_ACTIVITY_EXECUTABLE))
        .thenReturn(executable1);
    when(
        configuration
            .getRequiredPropertyString(SmartSpacesNativeActivityWrapper.CONFIGURATION_APPLICATION_JAVA_CLASS))
        .thenReturn(className);

    Class expectedActivityClass = Activity1.class;
    Bundle activityBundle = mock(Bundle.class);
    when(bundleLoader.loadLiveActivityBundle(liveActivity, executableFile1)).thenReturn(
        activityBundle);
    when(activityBundle.loadClass(className)).thenReturn(expectedActivityClass);

    Activity activity = wrapper.newInstance();

    assertEquals(expectedActivityClass, activity.getClass());

    wrapper.done();

    verify(bundleLoader).dismissLiveActivityBundle(liveActivity);
  }

  public static class Activity1 extends BaseActivity {
  }
}
