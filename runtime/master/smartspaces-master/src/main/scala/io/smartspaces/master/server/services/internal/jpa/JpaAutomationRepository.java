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

import io.smartspaces.domain.system.NamedScript;
import io.smartspaces.expression.FilterExpression;
import io.smartspaces.master.server.services.AutomationRepository;
import io.smartspaces.master.server.services.internal.jpa.domain.JpaNamedScript;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

/**
 * A JPA implementation of {@link AutomationRepository}.
 *
 * @author Keith M. Hughes
 */
public class JpaAutomationRepository implements AutomationRepository {

  /**
   * The entity manager for JPA entities.
   */
  private EntityManager entityManager;

  @Override
  public NamedScript newNamedScript() {
    return new JpaNamedScript();
  }

  @Override
  public NamedScript newNamedScript(NamedScript template) {
    return copyNamedScriptTemplate(template);
  }

  /**
   * Create a new script and fill in the script from the template.
   *
   * @param template
   *          the template with the values for the new script
   *
   * @return a new script with the specified values
   */
  private NamedScript copyNamedScriptTemplate(NamedScript template) {
    NamedScript script = new JpaNamedScript();
    script.setName(template.getName());
    script.setDescription(template.getDescription());
    script.setContent(template.getContent());
    script.setLanguage(template.getLanguage());
    script.setSchedule(template.getSchedule());
    script.setScheduled(template.getScheduled());

    return script;
  }

  @Override
  public List<NamedScript> getAllNamedScripts() {
    TypedQuery<NamedScript> query =
        entityManager.createNamedQuery("namedScriptAll", NamedScript.class);
    return query.getResultList();
  }

  @Override
  public List<NamedScript> getNamedScripts(FilterExpression filter) {
    TypedQuery<NamedScript> query =
        entityManager.createNamedQuery("namedScriptAll", NamedScript.class);
    List<NamedScript> scripts = query.getResultList();

    List<NamedScript> results = new ArrayList<>();

    if (filter != null) {
      for (NamedScript script : scripts) {
        if (filter.accept(script)) {
          results.add(script);
        }
      }
    } else {
      results.addAll(scripts);
    }

    return results;
  }

  @Override
  public NamedScript getNamedScriptById(String id) {
    return entityManager.find(JpaNamedScript.class, id);
  }

  @Override
  public NamedScript saveNamedScript(NamedScript script) {
    if (script.getId() != null) {
      return entityManager.merge(script);
    } else {
      entityManager.persist(script);

      return script;
    }
  }

  @Override
  public void deleteNamedScript(NamedScript script) {
    entityManager.remove(script);
  }

  /**
   * @param entityManager
   *          the entity manager to set
   */
  public void setEntityManager(EntityManager entityManager) {
    this.entityManager = entityManager;
  }
}
