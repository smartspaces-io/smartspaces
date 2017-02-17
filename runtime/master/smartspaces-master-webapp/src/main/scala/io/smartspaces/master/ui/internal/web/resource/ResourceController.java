/*
 * Copyright (C) 2017 Keith M. Hughes
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

package io.smartspaces.master.ui.internal.web.resource;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import io.smartspaces.master.ui.internal.web.BaseActiveSpaceMasterController;

/**
 * Spring MVC controller for resource operations.
 *
 * @author Keith M. Hughes
 */
@Controller
public class ResourceController extends BaseActiveSpaceMasterController {

  /**
   * Display a list of all resources.
   *
   * @return Model and view for controller list display.
   */
  @RequestMapping("/resource/all.html")
  public ModelAndView listResources() {
    ModelAndView mav = getModelAndView();

    mav.setViewName("resource/ResourceViewAll");

    return mav;
  }
}
