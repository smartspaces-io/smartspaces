/*
 * Copyright (C) 2016 Keith M. Hughes
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

package io.smartspaces.example.activity.routable.output

import java.util.Map

import com.google.common.collect.Maps

import io.smartspaces.activity.impl.route.BaseRoutableActivity

/**
 * A simple Smart Spaces Scala-based activity for writing to a route.
 */
class SimpleScalaRoutableOutputActivity extends BaseRoutableActivity {

  override def onActivityActivate(): Unit = {
    val message: Map[String, Object] = Maps.newHashMap()
    message.put("message", "yipee! activated!")
    sendOutputMessage("output1", message)
  }

  override def onActivityDeactivate(): Unit = {
    val message: Map[String, Object] = Maps.newHashMap()
    message.put("message", "bummer! deactivated!")
    sendOutputMessage("output1", message)
  }
}
