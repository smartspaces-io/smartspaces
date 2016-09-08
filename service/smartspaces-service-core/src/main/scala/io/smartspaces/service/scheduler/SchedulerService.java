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

package io.smartspaces.service.scheduler;

import io.smartspaces.service.SupportedService;

import java.util.Date;
import java.util.Map;

/**
 * A scheduler
 *
 * @author Keith M. Hughes
 */
public interface SchedulerService extends SupportedService {

  /**
   * The name of the service.
   */
  public static final String SERVICE_NAME = "scheduler";

  /**
   * Schedule a script using a CRON expression.
   *
   * @param jobName
   *          the name of the job
   * @param groupName
   *          the name of the group the job will run in, can be {@code null} to
   *          be in the default group
   * @param id
   *          id of the script
   * @param schedule
   *          the cron schedule when the job should fire
   */
  void scheduleScriptWithCron(String jobName, String groupName, String id, String schedule);

  /**
   * Schedule an action job for a specific time.
   * 
   * <p>
   * The job will be persisted.
   *
   * @param jobName
   *          the name of the job
   * @param groupName
   *          the name of the job group the job will run in, can be {@code null} to
   *          be in the default group
   * @param actionSource
   *          the name of the action source
   * @param actionName
   *          the name of the action
   * @param data
   *          the data for the action
   * @param when
   *          the date when the job should fire
   */
  void scheduleAction(String jobName, String jobGroupName, String actionSource, String actionName,
      Map<String, Object> data, Date when);

  /**
   * Schedule an action job using a CRON expression.
   * 
   * <p>
   * The job will be persisted.
   *
   * @param jobName
   *          the name of the job
   * @param groupName
   *          the name of the job group the job will run in, can be {@code null} to
   *          be in the default group
   * @param actionSource
   *          the name of the action source
   * @param actionName
   *          the name of the action
   * @param data
   *          the data for the action
   * @param schedule
   *          the cron schedule when the job should fire
   */
  void scheduleActionWithCron(String jobName, String groupName, String actionSource,
      String actionName, Map<String, Object> data, String schedule);

  /**
   * Add entities to the scheduler that can be used for scheduled jobs.
   *
   * <p>
   * Entities already registered with a given name will be replaced by new
   * entities with the same name.
   *
   * @param entities
   *          map of entity names to entities
   */
  void addSchedulingEntities(Map<String, Object> entities);
}
