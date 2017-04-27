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

package io.smartspaces.master.ui.internal.web.admin;

import io.smartspaces.master.api.master.MasterApiMasterSupportManager;
import io.smartspaces.master.ui.internal.web.BaseSpaceMasterController;
import io.smartspaces.messaging.dynamic.SmartSpacesMessagesSupport;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.util.Map;

/**
 * A Spring MVC controller for support activities.
 *
 * @author Keith M. Hughes
 */
@Controller
public class MasterSupportController extends BaseSpaceMasterController {

  /**
   * Value when the IS Version is unknown.
   */
  private static final String SMARTSPACES_VERSION_UNKNOWN = "Unknown";

  /**
   * The UI manager for master support activities.
   */
  private MasterApiMasterSupportManager masterApiMasterSupportManager;

  /**
   * Display a list of all named scripts.
   *
   * @return Model and view for named script list display.
   */
  @RequestMapping("/admin/support/index.html")
  public ModelAndView supportIndexPage() {
    ModelAndView mav = getModelAndView();

    Map<String, Object> response = masterApiMasterSupportManager.getSmartSpacesVersion();
    mav.addAllObjects(SmartSpacesMessagesSupport.getResponseDataMap(response));

    mav.setViewName("admin/SupportAll");

    return mav;
  }

  @RequestMapping(value = "/admin/support/exportMasterDomainModel.json", method = RequestMethod.GET)
  public @ResponseBody
      Map<String, ? extends Object> exportMasterDomainModel() {
    return masterApiMasterSupportManager.exportToFileSystemMasterDomainModel();
  }

  @RequestMapping(value = "/admin/support/importMasterDomainModel.json", method = RequestMethod.GET)
  public @ResponseBody
      Map<String, ? extends Object> importMasterDomainModel() {
    return masterApiMasterSupportManager.importFromFileSystemMasterDomainModel();
  }

  @RequestMapping(value = "/admin/support/hardRestartMaster.json", method = RequestMethod.GET)
  public @ResponseBody
      Map<String, ? extends Object> hardRestartMasterl() {
    return masterApiMasterSupportManager.hardRestartMaster();
  }

  @RequestMapping(value = "/admin/support/softRestartMaster.json", method = RequestMethod.GET)
  public @ResponseBody
      Map<String, ? extends Object> softRestartMasterl() {
    return masterApiMasterSupportManager.softRestartMaster();
  }

  /**
   * @param masterApiMasterSupportManager
   *          the masterApiMasterSupportManager to set
   */
  public void setMasterApiMasterSupportManager(
      MasterApiMasterSupportManager masterApiMasterSupportManager) {
    this.masterApiMasterSupportManager = masterApiMasterSupportManager;
  }
}
