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

import java.util.Map;

import io.smartspaces.master.api.master.MasterApiResourceManager;
import io.smartspaces.master.api.messages.MasterApiMessages;
import io.smartspaces.master.ui.internal.web.BaseActiveSpaceMasterController;

/**
 * Spring MVC controller for resource operations.
 *
 * @author Keith M. Hughes
 */
@Controller
public class ResourceController extends BaseActiveSpaceMasterController {

  /**
   * The master API manager for resources.
   */
  private MasterApiResourceManager masterApiResourceManager;

  /**
   * Display a list of all resources.
   *
   * @return model and view for resource list display
   */
  @RequestMapping("/resource/all.html")
  public ModelAndView listResources() {
    Map<String, Object> response = masterApiResourceManager.getResourcesByFilter(null);
    ModelAndView mav = getModelAndView();

    mav.setViewName("resource/ResourceViewAll");
    mav.addObject("resources", response.get(MasterApiMessages.MASTER_API_MESSAGE_ENVELOPE_DATA));

    return mav;
  }

  /**
   * Set the master API manager for resources.
   * 
   * @param masterApiResourceManager
   *          the master API manager for resources
   */
  public void setMasterApiResourceManager(MasterApiResourceManager masterApiResourceManager) {
    this.masterApiResourceManager = masterApiResourceManager;
  }
}
