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

package io.smartspaces.master.spaceoperations.internal;

import groovy.lang.Binding;
import groovy.util.GroovyScriptEngine;
import io.smartspaces.master.spaceoperations.SpaceOperations;

/**
 * Space Operations using Groovy.
 *
 * @author Keith M. Hughes
 */
public class GroovySpaceOperations implements SpaceOperations {

  public static void main(String[] args) {
    GroovySpaceOperations so = new GroovySpaceOperations();
    so.run();
  }

  @Override
  public void run() {
    try {
      String[] roots = new String[] { "/home/keith/robots/ros/smartspaces/smartspaces_scripts" };
      GroovyScriptEngine gse = new GroovyScriptEngine(roots);
      Binding binding = new Binding();
      binding.setVariable("args", new String[] {
          "-s=/home/keith/robots/ros/smartspaces/smartspaces_scripts", "sample.description",
          "deploy" });
      gse.run("SpaceOperationsRunner.groovy", binding);
      System.out.println(binding.getVariable("output"));
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

}
