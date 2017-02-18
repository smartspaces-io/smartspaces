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

package io.smartspaces.master.server.services.internal.jpa;

import io.smartspaces.SmartSpacesException;
import io.smartspaces.domain.basic.Resource;
import io.smartspaces.expression.FilterExpression;
import io.smartspaces.master.server.services.ResourceRepository;
import io.smartspaces.master.server.services.internal.jpa.domain.JpaResource;
import io.smartspaces.util.uuid.UuidGenerator;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

/**
 * A JPA implementation of {@link ResourceRepository}.
 *
 * @author Keith M. Hughes
 */
public class JpaResourceRepository implements ResourceRepository {

  /**
   * The UUID generator to use.
   */
  private UuidGenerator uuidGenerator;

  /**
   * The entity manager for JPA entities.
   */
  private EntityManager entityManager;

  @Override
  public Resource newResource() {
    return new JpaResource();
  }

  @Override
  public List<Resource> getAllResources() {
    TypedQuery<Resource> query = entityManager.createNamedQuery("resourceAll", Resource.class);
    return query.getResultList();
  }

  @Override
  public List<Resource> getResources(FilterExpression filter) {
    TypedQuery<Resource> query = entityManager.createNamedQuery("resourceAll", Resource.class);
    List<Resource> resources = query.getResultList();

    List<Resource> results = new ArrayList<>();

    for (Resource resource : resources) {
      if (filter.accept(resource)) {
        results.add(resource);
      }
    }

    return results;
  }

  @Override
  public Resource getResourceById(String id) {
    return entityManager.find(JpaResource.class, id);
  }

  @Override
  public Resource getResourceByNameAndVersion(String identifyingName, String version) {
    TypedQuery<Resource> query =
        entityManager.createNamedQuery("resourceByNameAndVersion", Resource.class);
    query.setParameter("identifyingName", identifyingName);
    query.setParameter("version", version);

    List<Resource> results = query.getResultList();
    if (!results.isEmpty()) {
      return results.get(0);
    } else {
      return null;
    }
  }

  @Override
  public Resource saveResource(Resource resource) {
    if (resource.getId() != null) {
      return entityManager.merge(resource);
    } else {
      entityManager.persist(resource);
      return resource;
    }
  }

  @Override
  public void deleteResource(Resource resource) {
    entityManager.remove(resource);
  }

  @Override
  public long getNumberResources() {
    TypedQuery<Long> query = entityManager.createNamedQuery("countResourceAll", Long.class);
    List<Long> results = query.getResultList();
    return results.get(0);
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
