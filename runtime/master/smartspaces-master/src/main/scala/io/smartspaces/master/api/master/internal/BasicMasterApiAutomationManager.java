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

package io.smartspaces.master.api.master.internal;

import io.smartspaces.SimpleSmartSpacesException;
import io.smartspaces.domain.support.AutomationUtils;
import io.smartspaces.domain.system.NamedScript;
import io.smartspaces.domain.system.pojo.SimpleNamedScript;
import io.smartspaces.expression.FilterExpression;
import io.smartspaces.master.api.master.MasterApiAutomationManager;
import io.smartspaces.master.api.master.MasterApiUtilities;
import io.smartspaces.master.api.messages.MasterApiMessages;
import io.smartspaces.master.server.services.AutomationManager;
import io.smartspaces.master.server.services.AutomationRepository;
import io.smartspaces.messaging.dynamic.SmartSpacesMessagesSupport;
import io.smartspaces.messaging.dynamic.SmartSpacesMessages;
import io.smartspaces.service.scheduler.SchedulerService;
import io.smartspaces.service.scheduler.SchedulerService$;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * A basic implementation of the {@link MasterApiAutomationManager}.
 *
 * @author Keith M. Hughes
 */
public class BasicMasterApiAutomationManager extends BaseMasterApiManager implements
    MasterApiAutomationManager {

  /**
   * The automation repository.
   */
  private AutomationRepository automationRepository;

  /**
   * The automation manager.
   */
  private AutomationManager automationManager;

  @Override
  public Set<String> getScriptingLanguages() {
    return automationManager.getScriptingLanguages();
  }

  @Override
  public NamedScript saveNamedScript(SimpleNamedScript script) {
    NamedScript finalScript = automationRepository.newNamedScript(script);

    finalScript = automationRepository.saveNamedScript(finalScript);

    potentiallyScheduleScript(finalScript);

    return finalScript;
  }

  @Override
  public NamedScript updateNamedScript(String id, SimpleNamedScript template) {
    NamedScript script = automationRepository.getNamedScriptById(id);
    if (script != null) {
      AutomationUtils.copy(template, script);
      script = automationRepository.saveNamedScript(script);

      potentiallyScheduleScript(script);

      return script;
    } else {
      throw new SimpleSmartSpacesException(String.format("Named script with id %s does not exist",
          id));
    }
  }

  /**
   * Start a script if it is scheduled.
   *
   * @param script
   *          the script to schedule
   */
  private void potentiallyScheduleScript(NamedScript script) {
    if (script.getScheduled()) {
      SchedulerService schedulerService =
          spaceEnvironment.getServiceRegistry().getService(SchedulerService$.MODULE$.SERVICE_NAME());
      if (schedulerService != null) {
        spaceEnvironment.getLog().info(String.format("Scheduling script %s", script.getName()));

        String schedule = script.getSchedule();
        if (schedule.startsWith(SCHEDULE_TYPE_ONCE)) {
          // TODO(keith): Support once running
        } else if (schedule.startsWith(SCHEDULE_TYPE_REPEAT)) {
          // TODO(keith): Allow specification or autogeneration of job and group
          // names.
          schedulerService.scheduleScriptWithCron("foo", "bar", "goober",
              "0 " + schedule.substring(SCHEDULE_TYPE_REPEAT.length()));
        } else {
          spaceEnvironment.getLog().error(
              String.format("Script %s has an illegal schedule: %s", script.getName(), schedule));
        }
      } else {
        spaceEnvironment.getLog().warn(
            String.format("No scheduling service for scheduling script %s", script.getName()));
      }
    }
  }

  @Override
  public Map<String, Object> deleteNamedScript(String id) {
    NamedScript script = automationRepository.getNamedScriptById(id);
    if (script != null) {
      automationRepository.deleteNamedScript(script);

      return SmartSpacesMessagesSupport.getSimpleSuccessResponse();
    } else {
      return noSuchNamedScriptResult();
    }
  }

  @Override
  public Map<String, Object> runNamedScript(String id) {
    spaceEnvironment.getLog().info(String.format("Running script with id %s", id));
    NamedScript script = automationRepository.getNamedScriptById(id);
    if (script != null) {
      // TODO(keith): Run in another thread?
      automationManager.runScript(script);

      return SmartSpacesMessagesSupport.getSimpleSuccessResponse();
    } else {
      return noSuchNamedScriptResult();
    }
  }

  @Override
  public Map<String, Object> getNamedScriptsByFilter(String filter) {
    List<Map<String, Object>> responseData = new ArrayList<>();

    try {
      FilterExpression filterExpression = expressionFactory.getFilterExpression(filter);

      List<NamedScript> scripts = automationRepository.getNamedScripts(filterExpression);
      Collections.sort(scripts, MasterApiUtilities.NAMED_SCRIPT_BY_NAME_COMPARATOR);
      for (NamedScript script : scripts) {
        responseData.add(extractBasicNamedScriptApiData(script));
      }

      return SmartSpacesMessagesSupport.getSuccessResponse(responseData);
    } catch (Throwable e) {
      spaceEnvironment.getLog().error("Attempt to get named script data failed", e);

      return SmartSpacesMessagesSupport.getFailureResponse(
          MasterApiMessages.MESSAGE_API_CALL_FAILURE, e);
    }
  }

  /**
   * Get basic information about a named script.
   *
   * @param script
   *          the script
   *
   * @return a Master API coded object giving the basic information
   */
  private Map<String, Object> extractBasicNamedScriptApiData(NamedScript script) {
    Map<String, Object> data = new HashMap<>();

    data.put(MasterApiMessages.MASTER_API_PARAMETER_NAME_ENTITY_ID, script.getId());
    data.put(MasterApiMessages.MASTER_API_PARAMETER_NAME_ENTITY_NAME, script.getName());
    data.put(MasterApiMessages.MASTER_API_PARAMETER_NAME_ENTITY_DESCRIPTION,
        script.getDescription());
    data.put(MasterApiMessages.MASTER_API_PARAMETER_NAME_ENTITY_METADATA, script.getMetadata());
    data.put(MasterApiMessages.MASTER_API_PARAMETER_NAME_NAMED_SCRIPT_LANGUAGE,
        script.getLanguage());

    return data;
  }

  @Override
  public Map<String, Object> getNamedScriptView(String id) {
    NamedScript activity = automationRepository.getNamedScriptById(id);
    if (activity != null) {
      return SmartSpacesMessagesSupport.getSuccessResponse(extractBasicNamedScriptApiData(activity));
    } else {
      return noSuchNamedScriptResult();
    }
  }

  @Override
  public Map<String, Object> updateNamedScriptMetadata(String id, Object metadataCommandObj) {
    if (!(metadataCommandObj instanceof Map)) {
      return SmartSpacesMessagesSupport.getFailureResponse(
          MasterApiMessages.MESSAGE_SPACE_CALL_ARGS_NOMAP,
          MasterApiMessages.MESSAGE_SPACE_DETAIL_CALL_ARGS_NOMAP);
    }

    @SuppressWarnings("unchecked")
    Map<String, Object> metadataCommand = (Map<String, Object>) metadataCommandObj;

    try {
      NamedScript script = automationRepository.getNamedScriptById(id);
      if (script == null) {
        return noSuchNamedScriptResult();
      }

      String command = (String) metadataCommand.get(MasterApiMessages.MASTER_API_PARAMETER_COMMAND);

      if (MasterApiMessages.MASTER_API_COMMAND_METADATA_REPLACE.equals(command)) {
        @SuppressWarnings("unchecked")
        Map<String, Object> replacement =
            (Map<String, Object>) metadataCommand
                .get(SmartSpacesMessages.MESSAGE_ENVELOPE_DATA);
        script.setMetadata(replacement);
      } else if (MasterApiMessages.MASTER_API_COMMAND_METADATA_MODIFY.equals(command)) {
        Map<String, Object> metadata = script.getMetadata();

        @SuppressWarnings("unchecked")
        Map<String, Object> modifications =
            (Map<String, Object>) metadataCommand
                .get(SmartSpacesMessages.MESSAGE_ENVELOPE_DATA);
        for (Entry<String, Object> entry : modifications.entrySet()) {
          metadata.put(entry.getKey(), entry.getValue());
        }

        script.setMetadata(metadata);
      } else if (MasterApiMessages.MASTER_API_COMMAND_METADATA_DELETE.equals(command)) {
        Map<String, Object> metadata = script.getMetadata();

        @SuppressWarnings("unchecked")
        List<String> modifications =
            (List<String>) metadataCommand.get(SmartSpacesMessages.MESSAGE_ENVELOPE_DATA);
        for (String entry : modifications) {
          metadata.remove(entry);
        }

        script.setMetadata(metadata);
      } else {
        return SmartSpacesMessagesSupport.getFailureResponse(
            MasterApiMessages.MESSAGE_API_COMMAND_UNKNOWN,
            String.format("Unknown command %s", command));
      }

      automationRepository.saveNamedScript(script);

      return SmartSpacesMessagesSupport.getSimpleSuccessResponse();
    } catch (Throwable e) {
      spaceEnvironment.getLog().error("Could not modify named script metadata", e);

      return SmartSpacesMessagesSupport.getFailureResponse(
          MasterApiMessages.MESSAGE_API_CALL_FAILURE, e);
    }
  }

  /**
   * Get a Master API response for no such named script.
   *
   * @return the Master API response
   */
  private Map<String, Object> noSuchNamedScriptResult() {
    return SmartSpacesMessagesSupport.getFailureResponse(
        MasterApiAutomationManager.MESSAGE_SPACE_DOMAIN_NAMEDSCRIPT_UNKNOWN,
        "The requested named script is unknown");
  }

  /**
   * Set the automation repository to use.
   *
   * @param automationRepository
   *          the automation repository
   */
  public void setAutomationRepository(AutomationRepository automationRepository) {
    this.automationRepository = automationRepository;
  }

  /**
   * Set the automation manager to use.
   *
   * @param automationManager
   *          the automation manager
   */
  public void setAutomationManager(AutomationManager automationManager) {
    this.automationManager = automationManager;
  }
}
