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
package io.smartspaces.master.server.services.internal.jpa;

import io.smartspaces.SimpleSmartSpacesException;
import io.smartspaces.domain.basic.ConfigurationParameter;
import io.smartspaces.domain.basic.SpaceController;
import io.smartspaces.domain.basic.SpaceControllerConfiguration;
import io.smartspaces.expression.FilterExpression;
import io.smartspaces.master.server.services.ActivityRepository;
import io.smartspaces.master.server.services.BaseSpaceControllerRepository;
import io.smartspaces.master.server.services.SpaceControllerRepository;
import io.smartspaces.master.server.services.internal.jpa.domain.JpaSpaceController;
import io.smartspaces.master.server.services.internal.jpa.domain.JpaSpaceControllerConfiguration;
import io.smartspaces.master.server.services.internal.jpa.domain.JpaSpaceControllerConfigurationParameter;
import io.smartspaces.util.uuid.UuidGenerator;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

/**
 * A JPA implementation of {@link SpaceControllerRepository}.
 *
 * @author Keith M. Hughes
 */
public class JpaSpaceControllerRepository extends BaseSpaceControllerRepository {

  /**
   * The repository for activity entities.
   */
  private ActivityRepository activityRepository;

  /**
   * The UUID generator to use.
   */
  private UuidGenerator uuidGenerator;

  /**
   * The entity manager for JPA entities.
   */
  private EntityManager entityManager;

  @Override
  public SpaceController newSpaceController() {
    SpaceController controller = new JpaSpaceController();

    controller.setUuid(uuidGenerator.newUuid());

    return controller;
  }

  @Override
  public SpaceController newSpaceController(SpaceController template) {
    return copySpaceControllerTemplate(uuidGenerator.newUuid(), template);
  }

  @Override
  public SpaceController newSpaceController(String uuid, SpaceController template) {
    return copySpaceControllerTemplate(uuid, template);
  }

  /**
   * Create a new controller and fill in the controller from the template.
   *
   * @param uuid
   *          the uuid to give the controller
   * @param template
   *          the template with the values for the new controller
   *
   * @return a new controller with the specified values
   */
  private SpaceController copySpaceControllerTemplate(String uuid, SpaceController template) {
    SpaceController controller = new JpaSpaceController();
    controller.setUuid(uuid);
    controller.setName(template.getName());
    controller.setDescription(template.getDescription());
    controller.setHostId(template.getHostId());
    controller.setHostName(template.getHostName());
    controller.setHostControlPort(template.getHostControlPort());

    return controller;
  }

  @Override
  public long getNumberSpaceControllers() {
    TypedQuery<Long> query = entityManager.createNamedQuery("countSpaceControllerAll", Long.class);
    List<Long> results = query.getResultList();
    return results.get(0);
  }

  @Override
  public List<SpaceController> getAllSpaceControllers() {
    TypedQuery<SpaceController> query =
        entityManager.createNamedQuery("spaceControllerAll", SpaceController.class);
    return query.getResultList();
  }

  @Override
  public List<SpaceController> getSpaceControllers(FilterExpression filter) {
    TypedQuery<SpaceController> query =
        entityManager.createNamedQuery("spaceControllerAll", SpaceController.class);
    List<SpaceController> controllers = query.getResultList();

    List<SpaceController> results = new ArrayList<>();

    if (filter != null) {
      for (SpaceController controller : controllers) {
        if (filter.accept(controller)) {
          results.add(controller);
        }
      }
    } else {
      results.addAll(controllers);
    }

    return results;
  }

  @Override
  public SpaceController getSpaceControllerById(String id) {
    return entityManager.find(JpaSpaceController.class, id);
  }

  @Override
  public SpaceController getSpaceControllerByUuid(String uuid) {
    TypedQuery<SpaceController> query =
        entityManager.createNamedQuery("spaceControllerByUuid", SpaceController.class);
    query.setParameter("uuid", uuid);
    List<SpaceController> results = query.getResultList();
    if (!results.isEmpty()) {
      return results.get(0);
    } else {
      return null;
    }
  }

  @Override
  public SpaceController saveSpaceController(SpaceController controller) {
    if (controller.getId() != null) {
      return entityManager.merge(controller);
    } else {
      entityManager.persist(controller);

      return controller;
    }
  }

  @Override
  public void deleteSpaceController(SpaceController controller) {
    long count = activityRepository.getNumberLiveActivitiesByController(controller);
    if (count == 0) {
      entityManager.remove(controller);
    } else {
      throw new SimpleSmartSpacesException(String.format(
          "Cannot delete space controller %s, it is in %d live activities", controller.getId(),
          count));
    }
  }

  @Override
  public SpaceControllerConfiguration newSpaceControllerConfiguration() {
    return new JpaSpaceControllerConfiguration();
  }

  @Override
  public ConfigurationParameter newSpaceControllerConfigurationParameter() {
    return new JpaSpaceControllerConfigurationParameter();
  }

  /**
   * @param activityRepository
   *          the activityRepository to set
   */
  public void setActivityRepository(ActivityRepository activityRepository) {
    this.activityRepository = activityRepository;
  }

  /**
   * @param uuidGenerator
   *          the uuidGenerator to set
   */
  public void setUuidGenerator(UuidGenerator uuidGenerator) {
    this.uuidGenerator = uuidGenerator;
  }

  /**
   * @param entityManager
   *          the entity manager to set
   */
  public void setEntityManager(EntityManager entityManager) {
    this.entityManager = entityManager;
  }
}
