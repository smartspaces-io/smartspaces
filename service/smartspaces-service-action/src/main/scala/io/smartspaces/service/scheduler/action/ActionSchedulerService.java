/*
 * Copyright (C) 2015 Keith M. Hughes
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

package io.smartspaces.service.scheduler.action;

import io.smartspaces.service.SupportedService;

import java.util.Date;
import java.util.Map;

/**
 * A scheduler for actions.
 *
 * @author Keith M. Hughes
 */
public interface ActionSchedulerService extends SupportedService {

  /**
   * The name of the service.
   */
  public static final String SERVICE_NAME = "scheduler.action";

  /**
   * Schedule a runnable for the future.
   *
   * @param jobName
   *          the name of the job
   * @param groupName
   *          the name of the group the job will run in, can be {@code null} to
   *          be in the default group
   * @param actionSource
   *          the source of the action
   * @param actionName
   *          the name of the action
   * @param when
   *          the date when the job should fire
   */
  void
      schedule(String jobName, String groupName, String actionSource, String actionName, Date when);

  /**
   * Schedule a runnable for the future.
   *
   * @param jobName
   *          the name of the job
   * @param groupName
   *          the name of the group the job will run in, can be {@code null} to
   *          be in the default group
   * @param actionSource
   *          the source of the action
   * @param actionName
   *          the name of the action
   * @param data
   *          data for the action call, can be {@code null}
   * @param when
   *          the date when the job should fire
   */
  void schedule(String jobName, String groupName, String actionSource, String actionName,
      Map<String, Object> data, Date when);

  /**
   * Schedule an action for the future.
   * 
   * <p>
   * The data will be {@code null}.
   *
   * @param jobName
   *          the name of the job
   * @param groupName
   *          the name of the group the job will run in, can be {@code null} to
   *          be in the default group
   * @param actionSource
   *          the source of the action
   * @param actionName
   *          the name of the action
   * @param schedule
   *          the cron schedule when the job should fire
   */
  void scheduleWithCron(String jobName, String groupName, String actionSource, String actionName,
      String schedule);

  /**
   * Schedule a runnable for the future.
   *
   * @param jobName
   *          the name of the job
   * @param groupName
   *          the name of the group the job will run in, can be {@code null} to
   *          be in the default group
   * @param actionSource
   *          the source of the action
   * @param actionName
   *          the name of the action
   * @param data
   *          the data for the call, can be {@code null}
   * @param schedule
   *          the cron schedule when the job should fire
   */
  void scheduleWithCron(String jobName, String groupName, String actionSource, String actionName,
      Map<String, Object> data, String schedule);
}
