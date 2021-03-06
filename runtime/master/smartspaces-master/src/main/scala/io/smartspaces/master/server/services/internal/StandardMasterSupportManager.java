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

package io.smartspaces.master.server.services.internal;

import io.smartspaces.master.server.services.ActivityRepository;
import io.smartspaces.master.server.services.AutomationRepository;
import io.smartspaces.master.server.services.MasterSupportManager;
import io.smartspaces.master.server.services.ResourceRepository;
import io.smartspaces.master.server.services.SpaceControllerRepository;
import io.smartspaces.master.server.services.internal.support.JdomMasterDomainModelCreator;
import io.smartspaces.master.server.services.internal.support.JdomMasterDomainModelImporter;
import io.smartspaces.system.SmartSpacesEnvironment;
import io.smartspaces.system.core.container.SmartSpacesSystemControl;

/**
 * The standard implementation of the {@link MasterSupportManager}.
 *
 * @author Keith M. Hughes
 */
public class StandardMasterSupportManager implements MasterSupportManager {

  /**
   * Repository for activity entities.
   */
  private ActivityRepository activityRepository;

  /**
   * Repository for space controller entities.
   */
  private SpaceControllerRepository spaceControllerRepository;

  /**
   * Repository for resource entities.
   */
  private ResourceRepository resourceRepository;

  /**
   * Repository for automation entities.
   */
  private AutomationRepository automationRepository;
  
  /**
   * System control for the container.
   */
  private SmartSpacesSystemControl spaceSystemControl;

  /**
   * The space environment being run under.
   */
  private SmartSpacesEnvironment spaceEnvironment;

  @Override
  public void startup() {
    // Nothing right now
  }

  @Override
  public void shutdown() {
    // Nothing right now.
  }

  @Override
  public String getMasterDomainModel() {
    spaceEnvironment.getLog().info("Exporting master domain model");
    JdomMasterDomainModelCreator creator = new JdomMasterDomainModelCreator();

    return creator.newModel(activityRepository, spaceControllerRepository, resourceRepository,
        automationRepository);
  }

  @Override
  public void importMasterDomainModel(String model) {
    spaceEnvironment.getLog().info("Importing master domain model");
    try {
      JdomMasterDomainModelImporter importer = new JdomMasterDomainModelImporter();

      importer.importModel(model, activityRepository, spaceControllerRepository, resourceRepository,
          automationRepository, spaceEnvironment.getTimeProvider());

      spaceEnvironment.getLog().info("Master domain model imported successfully");
    } catch (Exception e) {
      spaceEnvironment.getLog().error("Error while importing master domain model", e);
    }
  }

  @Override
  public void hardRestartMaster() {
    spaceSystemControl.hardRestart();
  }

  @Override
  public void softRestartMaster() {
    spaceSystemControl.softRestart();
  }

  /**
   * @param activityRepository
   *          the activityRepository to set
   */
  public void setActivityRepository(ActivityRepository activityRepository) {
    this.activityRepository = activityRepository;
  }

  /**
   * @param spaceControllerRepository
   *          the controllerRepository to set
   */
  public void setSpaceControllerRepository(SpaceControllerRepository spaceControllerRepository) {
    this.spaceControllerRepository = spaceControllerRepository;
  }

  /**
   * Set the resource repository.
   * 
   * @param resourceRepository
   *          the resource repository
   */
  public void setResourceRepository(ResourceRepository resourceRepository) {
    this.resourceRepository = resourceRepository;
  }

  /**
   * @param automationRepository
   *          the automationRepository to set
   */
  public void setAutomationRepository(AutomationRepository automationRepository) {
    this.automationRepository = automationRepository;
  }
  

  /**
   * @param spaceSystemControl
   *          the spaceSystemControl to set
   */
  public void setSpaceSystemControl(SmartSpacesSystemControl spaceSystemControl) {
    this.spaceSystemControl = spaceSystemControl;
  }


  /**
   * @param spaceEnvironment
   *          the spaceEnvironment to set
   */
  public void setSpaceEnvironment(SmartSpacesEnvironment spaceEnvironment) {
    this.spaceEnvironment = spaceEnvironment;
  }
}
