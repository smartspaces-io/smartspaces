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

package io.smartspaces.service.scheduler

import io.smartspaces.service.SupportedService

import java.util.Date
import java.util.Map
import java.util.Set
import io.smartspaces.resource.managed.ConditionalInitializerMixin

object SchedulerService {

  /**
   * The name of the service.
   */
  val SERVICE_NAME = "scheduler"

}

/**
 * A service for scheduling jobs. These jobs can persist through system restart.
 *
 * @author Keith M. Hughes
 */
trait SchedulerService extends SupportedService with ConditionalInitializerMixin[SchedulerService] {

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
  def scheduleScriptWithCron(jobName: String, groupName: String, id: String, schedule: String): Unit

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
  def scheduleAction(jobName: String, jobGroupName: String, actionSource: String, actionName: String,
    data: Map[String, Object], when: Date): Unit

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
  def scheduleActionWithCron(jobName: String, groupName: String, actionSource: String,
    actionName: String, data: Map[String, Object], schedule: String): Unit

  /**
   * Remove all jobs in the given job group.
   *
   * @param jobGroupName
   * 		the name for the job group
   */
  def removeJobGroup(jobGroupName: String): Unit

  /**
   * Get the job group names.
   *
   * @return the job group names
   */
  def getJobGroupNames(): Set[String]

  /**
   * Get the job names for a particular group.
   *
   * @param groupName
   *      the name of the group
   *
   * @return the group's job names
   */
  def getGroupJobNames(groupName: String): Set[String]

  /**
   * Does the given job exist?
   *
   * @param groupName
   *      group name of the job
   * @param jobName
   *      name of the job in the group
   *
   * @return {@code true} if the job exists
   */
  def doesJobExist(groupName: String, jobName: String): Boolean

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
  def addSchedulingEntities(entities: Map[String, Object]): Unit
}
