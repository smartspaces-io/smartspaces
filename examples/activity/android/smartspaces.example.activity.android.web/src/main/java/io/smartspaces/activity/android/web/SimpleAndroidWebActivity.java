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

package io.smartspaces.activity.android.web;


/**
 * A simple Smart Spaces Android-based activity.
 * 
 * @author Keith M. Hughes
 */
public class SimpleAndroidWebActivity extends BaseActivity {

  @Override
  public void onActivityStartup() {
    AndroidOsService androidService =
        getSpaceEnvironment().getServiceRegistry().getService(AndroidOsService.SERVICE_NAME);

    try {
      Intent browserIntent = new Intent(Intent.ACTION_VIEW);
      browserIntent.setData(Uri.parse("https://www.smartspaces.io/"));
      browserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      androidService.getAndroidContext().getApplicationContext().startActivity(browserIntent);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
