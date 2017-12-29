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

import io.smartspaces.SmartSpacesException;
import io.smartspaces.domain.basic.Activity;
import io.smartspaces.domain.basic.ActivityConfiguration;
import io.smartspaces.domain.basic.ActivityDependency;
import io.smartspaces.domain.basic.ConfigurationParameter;
import io.smartspaces.domain.basic.LiveActivity;
import io.smartspaces.domain.basic.LiveActivityGroup;
import io.smartspaces.domain.basic.Space;
import io.smartspaces.domain.basic.SpaceController;
import io.smartspaces.domain.basic.pojo.SimpleLiveActivity;
import io.smartspaces.expression.FilterExpression;
import io.smartspaces.master.server.services.ActivityRepository;
import io.smartspaces.master.server.services.BaseActivityRepository;
import io.smartspaces.master.server.services.internal.jpa.domain.JpaActivity;
import io.smartspaces.master.server.services.internal.jpa.domain.JpaActivityConfiguration;
import io.smartspaces.master.server.services.internal.jpa.domain.JpaActivityConfigurationParameter;
import io.smartspaces.master.server.services.internal.jpa.domain.JpaActivityDependency;
import io.smartspaces.master.server.services.internal.jpa.domain.JpaLiveActivity;
import io.smartspaces.master.server.services.internal.jpa.domain.JpaLiveActivityGroup;
import io.smartspaces.master.server.services.internal.jpa.domain.JpaSpace;
import io.smartspaces.util.uuid.UuidGenerator;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

/**
 * A JPA implementation of {@link ActivityRepository}.
 *
 * @author Keith M. Hughes
 */
public class JpaActivityRepository extends BaseActivityRepository {

  /**
   * The UUID generator to use.
   */
  private UuidGenerator uuidGenerator;

  /**
   * The entity manager for JPA entities.
   */
  private EntityManager entityManager;

  @Override
  public Activity newActivity() {
    return new JpaActivity();
  }

  @Override
  public ActivityDependency newActivityDependency() {
    return new JpaActivityDependency();
  }

  @Override
  public ActivityConfiguration newActivityConfiguration() {
    return new JpaActivityConfiguration();
  }

  @Override
  public ConfigurationParameter newActivityConfigurationParameter() {
    return new JpaActivityConfigurationParameter();
  }

  @Override
  public List<Activity> getAllActivities() {
    TypedQuery<Activity> query = entityManager.createNamedQuery("activityAll", Activity.class);
    return query.getResultList();
  }

  @Override
  public List<Activity> getActivities(FilterExpression filter) {
    TypedQuery<Activity> query = entityManager.createNamedQuery("activityAll", Activity.class);
    List<Activity> activities = query.getResultList();

    List<Activity> results = new ArrayList<>();

    for (Activity activity : activities) {
      if (filter.accept(activity)) {
        results.add(activity);
      }
    }

    return results;
  }

  @Override
  public Activity getActivityById(String id) {
    return entityManager.find(JpaActivity.class, id);
  }

  @Override
  public Activity getActivityByNameAndVersion(String identifyingName, String version) {
    TypedQuery<Activity> query =
        entityManager.createNamedQuery("activityByNameAndVersion", Activity.class);
    query.setParameter("identifyingName", identifyingName);
    query.setParameter("version", version);

    List<Activity> results = query.getResultList();
    if (!results.isEmpty()) {
      return results.get(0);
    } else {
      return null;
    }
  }

  @Override
  public Activity saveActivity(Activity activity) {
    if (activity.getId() != null) {
      return entityManager.merge(activity);
    } else {
      entityManager.persist(activity);
      return activity;
    }
  }

  @Override
  public void deleteActivity(Activity activity) {
    long count = getNumberLiveActivitiesByActivity(activity);
    if (count == 0) {
      entityManager.remove(activity);
    } else {
      throw new SmartSpacesException(String.format(
          "Cannot delete activity %s, it is in %d live activities", activity.getId(), count));
    }
  }

  @Override
  public LiveActivity newLiveActivity() {
    LiveActivity lactivity = new JpaLiveActivity();
    lactivity.setUuid(uuidGenerator.newUuid());

    return lactivity;
  }

  @Override
  public LiveActivity newAndSaveLiveActivity(SimpleLiveActivity liveActivityTemplate) {
    LiveActivity newLiveActivity = newLiveActivity();
    newLiveActivity.setActivity(liveActivityTemplate.getActivity());
    newLiveActivity.setController(liveActivityTemplate.getController());
    newLiveActivity.setName(liveActivityTemplate.getName());
    newLiveActivity.setDescription(liveActivityTemplate.getDescription());
    newLiveActivity.setMetadata(liveActivityTemplate.getMetadata());
    newLiveActivity.setConfiguration(liveActivityTemplate.getConfiguration());
    
    return saveLiveActivity(newLiveActivity);
  }

  @Override
  public long getNumberActivities() {
    TypedQuery<Long> query = entityManager.createNamedQuery("countActivityAll", Long.class);
    List<Long> results = query.getResultList();
    return results.get(0);
  }

  @Override
  public List<LiveActivity> getAllLiveActivities() {
    TypedQuery<LiveActivity> query =
        entityManager.createNamedQuery("liveActivityAll", LiveActivity.class);
    return query.getResultList();
  }

  @Override
  public List<LiveActivity> getLiveActivities(FilterExpression filter) {
    TypedQuery<LiveActivity> query =
        entityManager.createNamedQuery("liveActivityAll", LiveActivity.class);
    List<LiveActivity> activities = query.getResultList();

    List<LiveActivity> results = new ArrayList<>();

    for (LiveActivity activity : activities) {
      if (filter.accept(activity)) {
        results.add(activity);
      }
    }

    return results;
  }

  @Override
  public LiveActivity getLiveActivityById(String id) {
    return entityManager.find(JpaLiveActivity.class, id);
  }

  @Override
  public LiveActivity getLiveActivityByUuid(String uuid) {
    TypedQuery<LiveActivity> query =
        entityManager.createNamedQuery("liveActivityByUuid", LiveActivity.class);
    query.setParameter("uuid", uuid);
    List<LiveActivity> results = query.getResultList();
    if (!results.isEmpty()) {
      return results.get(0);
    } else {
      return null;
    }
  }

  @Override
  public List<LiveActivity> getLiveActivitiesByController(SpaceController controller) {
    TypedQuery<LiveActivity> query =
        entityManager.createNamedQuery("liveActivityByController", LiveActivity.class);
    query.setParameter("controller_id", controller.getId());
    List<LiveActivity> results = query.getResultList();
    return results;
  }

  @Override
  public long getNumberLiveActivitiesByController(SpaceController controller) {
    TypedQuery<Long> query =
        entityManager.createNamedQuery("countLiveActivityByController", Long.class);
    query.setParameter("controller_id", controller.getId());
    List<Long> results = query.getResultList();
    return results.get(0);
  }

  @Override
  public List<LiveActivity> getLiveActivitiesByActivity(Activity activity) {
    TypedQuery<LiveActivity> query =
        entityManager.createNamedQuery("liveActivityByActivity", LiveActivity.class);
    query.setParameter("activity_id", activity.getId());
    List<LiveActivity> results = query.getResultList();
    return results;
  }

  @Override
  public long getNumberLiveActivitiesByActivity(Activity activity) {
    TypedQuery<Long> query =
        entityManager.createNamedQuery("countLiveActivityByActivity", Long.class);
    query.setParameter("activity_id", activity.getId());
    List<Long> results = query.getResultList();
    return results.get(0);
  }

  @Override
  public LiveActivity saveLiveActivity(LiveActivity liveActivity) {
    if (liveActivity.getId() != null) {
      return entityManager.merge(liveActivity);
    } else {
      entityManager.persist(liveActivity);
      return liveActivity;
    }
  }

  @Override
  public void deleteLiveActivity(LiveActivity activity) {
    long count = getNumberLiveActivityGroupsByLiveActivity(activity);
    if (count == 0) {
      entityManager.remove(activity);
    } else {
      throw new SmartSpacesException(String.format(
          "Cannot delete live activity %s, it is in %d live activity groups", activity.getId(),
          count));
    }
  }

  @Override
  public LiveActivityGroup newLiveActivityGroup() {
    return new JpaLiveActivityGroup();
  }

  @Override
  public List<LiveActivityGroup> getAllLiveActivityGroups() {
    TypedQuery<LiveActivityGroup> query =
        entityManager.createNamedQuery("liveActivityGroupAll", LiveActivityGroup.class);
    return query.getResultList();
  }

  @Override
  public List<LiveActivityGroup> getLiveActivityGroups(FilterExpression filter) {
    TypedQuery<LiveActivityGroup> query =
        entityManager.createNamedQuery("liveActivityGroupAll", LiveActivityGroup.class);
    List<LiveActivityGroup> groups = query.getResultList();

    List<LiveActivityGroup> results = new ArrayList<>();

    for (LiveActivityGroup group : groups) {
      if (filter.accept(group)) {
        results.add(group);
      }
    }

    return results;
  }

  @Override
  public LiveActivityGroup getLiveActivityGroupById(String id) {
    return entityManager.find(JpaLiveActivityGroup.class, id);
  }

  @Override
  public List<LiveActivityGroup> getLiveActivityGroupsByLiveActivity(LiveActivity liveActivity) {
    TypedQuery<LiveActivityGroup> query =
        entityManager.createNamedQuery("liveActivityGroupByLiveActivity", LiveActivityGroup.class);
    query.setParameter("live_activity_id", liveActivity.getId());
    List<LiveActivityGroup> results = query.getResultList();
    return results;
  }

  @Override
  public long getNumberLiveActivityGroupsByLiveActivity(LiveActivity liveActivity) {
    TypedQuery<Long> query =
        entityManager.createNamedQuery("countLiveActivityGroupByLiveActivity", Long.class);
    query.setParameter("live_activity_id", liveActivity.getId());
    List<Long> results = query.getResultList();
    return results.get(0);
  }

  @Override
  public LiveActivityGroup saveLiveActivityGroup(LiveActivityGroup liveActivityGroup) {
    if (liveActivityGroup.getId() != null) {
      return entityManager.merge(liveActivityGroup);
    } else {
      entityManager.persist(liveActivityGroup);
      return liveActivityGroup;
    }
  }

  @Override
  public void deleteLiveActivityGroup(LiveActivityGroup liveActivityGroup) {
    long count = getNumberSpacesByLiveActivityGroup(liveActivityGroup);
    if (count == 0) {
      entityManager.remove(liveActivityGroup);
    } else {
      throw new SmartSpacesException(String.format(
          "Cannot delete live activity group %s, it is in %d spaces", liveActivityGroup.getId(),
          count));
    }
  }

  @Override
  public Space newSpace() {
    return new JpaSpace();
  }

  @Override
  public List<Space> getAllSpaces() {
    TypedQuery<Space> query = entityManager.createNamedQuery("spaceAll", Space.class);
    return query.getResultList();
  }

  @Override
  public List<Space> getSpaces(FilterExpression filter) {
    TypedQuery<Space> query = entityManager.createNamedQuery("spaceAll", Space.class);
    List<Space> spaces = query.getResultList();

    List<Space> results = new ArrayList<>();

    for (Space space : spaces) {
      if (filter.accept(space)) {
        results.add(space);
      }
    }
    return results;
  }

  @Override
  public Space getSpaceById(String id) {
    return entityManager.find(JpaSpace.class, id);
  }

  @Override
  public List<Space> getSpacesByLiveActivityGroup(LiveActivityGroup liveActivityGroup) {
    TypedQuery<Space> query =
        entityManager.createNamedQuery("spaceByLiveActivityGroup", Space.class);
    query.setParameter("live_activity_group_id", liveActivityGroup.getId());
    List<Space> results = query.getResultList();

    return results;
  }

  @Override
  public long getNumberSpacesByLiveActivityGroup(LiveActivityGroup liveActivityGroup) {
    TypedQuery<Long> query =
        entityManager.createNamedQuery("countSpaceByLiveActivityGroup", Long.class);
    query.setParameter("live_activity_group_id", liveActivityGroup.getId());
    List<Long> results = query.getResultList();

    return results.get(0);
  }

  @Override
  public List<Space> getSpacesBySubspace(Space subspace) {
    TypedQuery<Space> query = entityManager.createNamedQuery("spaceBySubspace", Space.class);
    query.setParameter("subspace_id", subspace.getId());
    List<Space> results = query.getResultList();

    return results;
  }

  @Override
  public long getNumberSpacesBySubspace(Space subspace) {
    TypedQuery<Long> query = entityManager.createNamedQuery("countSpaceBySubspace", Long.class);
    query.setParameter("subspace_id", subspace.getId());
    List<Long> results = query.getResultList();

    return results.get(0);
  }

  @Override
  public Space saveSpace(Space space) {
    if (space.getId() != null) {
      return entityManager.merge(space);
    } else {
      entityManager.persist(space);
      return space;
    }
  }

  @Override
  public void deleteSpace(Space space) {
    long count = getNumberSpacesBySubspace(space);
    if (count == 0) {
      entityManager.remove(space);
    } else {
      throw new SmartSpacesException(String.format("Cannot delete space %s, it is in %d subspaces",
          space.getId(), count));
    }
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
