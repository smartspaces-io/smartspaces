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

package io.smartspaces.service.scheduler.internal.quartz

import io.smartspaces.SmartSpacesException
import io.smartspaces.evaluation.StandardExecutionContext
import io.smartspaces.logging.ExtendedLog
import io.smartspaces.resource.managed.BaseConditionalInitializerMixin
import io.smartspaces.resource.managed.BaseManagedResource
import io.smartspaces.scheduling.quartz.orientdb.OrientDbJobStore
import io.smartspaces.service.BaseSupportedService
import io.smartspaces.service.action.ActionService
import io.smartspaces.service.scheduler.SchedulerService
import io.smartspaces.system.SmartSpacesEnvironment

import org.quartz.CronScheduleBuilder
import org.quartz.Job
import org.quartz.JobBuilder
import org.quartz.JobExecutionContext
import org.quartz.JobKey
import org.quartz.Scheduler
import org.quartz.SchedulerException
import org.quartz.TriggerBuilder
import org.quartz.TriggerKey
import org.quartz.impl.DirectSchedulerFactory
import org.quartz.impl.matchers.GroupMatcher
import org.quartz.simpl.RAMJobStore
import org.quartz.simpl.SimpleThreadPool
import org.quartz.spi.JobFactory
import org.quartz.spi.TriggerFiredBundle

import com.google.common.collect.Sets
import com.orientechnologies.orient.core.Orient

import java.util.Date
import java.util.HashSet
import java.util.Map
import java.util.Set

import scala.collection.JavaConversions.asScalaSet
import io.smartspaces.util.SmartSpacesUtilities
import com.orientechnologies.orient.core.OrientShutdownHook
import com.orientechnologies.orient.core.shutdown.OShutdownHandler
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

object StandardQuartzSchedulerService {
  val ORIENTDB_URI = "plocal:${$system.datadir}/database/quartz"
  val ORIENTDB_USER = "sooperdooper"
  val ORIENTDB_PASSWORD = "sooperdooper"
  val PERSISTED_SCHEDULER_NAME = "SmartSpacesSchedulerPersisted"
  val PERSISTED_SCHEDULER_INSTANCE_ID = "MainSchedulerNonClusteredPersisted"

  val VOLATILE_SCHEDULER_NAME = "SmartSpacesSchedulerVolatile"
  val VOLATILE_SCHEDULER_INSTANCE_ID = "MainSchedulerNonClusteredVolatile"

  /**
   * JobMap property for the ID of the script which will run.
   */
  val JOB_MAP_PROPERTY_SCRIPT_ID = "runnable"

  /**
   * JobMap property for the action name.
   */
  val JOB_MAP_PROPERTY_ACTION_NAME = "actionName"

  /**
   * JobMap property for the action source.
   */
  val JOB_MAP_PROPERTY_ACTION_SOURCE = "actionSource"

}

/**
 * A {@link SchedulerService} which uses quartz.
 *
 * @author Keith M. Hughes
 */
class StandardQuartzSchedulerService extends BaseSupportedService with BaseConditionalInitializerMixin[SchedulerService] with SchedulerService {

  /**
   * The volatile scheduler.duler
   */
  private var volatileScheduler: Scheduler = _

  /**
   * The persisted scheduler.
   */
  private var persistedScheduler: Scheduler = _

  override def getName(): String = {
    SchedulerService.SERVICE_NAME
  }

  override def startup(): Unit = {
    try {
      val jobFactory = new MyJobFactory(getSpaceEnvironment)

      // TODO(keith): Get Smart Spaces thread pool in here.
      // Get URI, username, and password from config.
      val orientDbUri = getSpaceEnvironment().getSystemConfiguration().evaluate(StandardQuartzSchedulerService.ORIENTDB_URI)
      val jobStore =
        new OrientDbJobStore(orientDbUri, StandardQuartzSchedulerService.ORIENTDB_USER, StandardQuartzSchedulerService.ORIENTDB_PASSWORD)
      jobStore.setExecutorService(getSpaceEnvironment.getExecutorService)
      jobStore.setExternalClassLoader(getClass.getClassLoader)

      val threadPool = new SimpleThreadPool(10, Thread.NORM_PRIORITY)

      val instance = DirectSchedulerFactory.getInstance()

      instance.createScheduler(StandardQuartzSchedulerService.PERSISTED_SCHEDULER_NAME, StandardQuartzSchedulerService.PERSISTED_SCHEDULER_INSTANCE_ID,
        threadPool, jobStore)

      persistedScheduler = instance.getScheduler(StandardQuartzSchedulerService.PERSISTED_SCHEDULER_NAME)
      persistedScheduler.setJobFactory(jobFactory)

      persistedScheduler.start()

      instance.createScheduler(StandardQuartzSchedulerService.VOLATILE_SCHEDULER_NAME, StandardQuartzSchedulerService.VOLATILE_SCHEDULER_INSTANCE_ID, threadPool,
        new RAMJobStore())

      volatileScheduler = instance.getScheduler(StandardQuartzSchedulerService.VOLATILE_SCHEDULER_NAME)
      volatileScheduler.setJobFactory(jobFactory)

      volatileScheduler.start()

      // Make sure when SmartSpaces shuts down that orientDB is shut down.
      // Other code may be using OrientDB so we want this at container
      // shutdown.
      getSpaceEnvironment.getContainerManagedScope.managedResources
        .addResource(new BaseManagedResource() {
          override def shutdown(): Unit = {
//       persistedScheduler.shutdown()
//           val latch = new CountDownLatch(1)
//            val shutdownHandler = new OShutdownHandler() {
//              override def getPriority(): Int = {
//                Integer.MAX_VALUE
//              }
//              override def shutdown() = {
//                println("OrientDB shutdown hook called")
//              latch.countDown()
//              }
//            }
            val instance = Orient.instance()
//            instance.closeAllStorages()
//            instance.addShutdownHandler(shutdownHandler)
              instance.shutdown()

            //latch.await(5, TimeUnit.SECONDS)
            
          }
        })

      getSpaceEnvironment.getLog.info("Quartz scheduling service started")
    } catch {
      case e: SchedulerException =>
        throw new SmartSpacesException("Could not start Smart Spaces volatileScheduler", e)
    }
  }

  override def shutdown(): Unit = {
    try {
      volatileScheduler.shutdown()
      persistedScheduler.shutdown()
    } catch {
      case e: SchedulerException =>
        throw new SmartSpacesException("Could not shutdown Smart Spaces volatileScheduler", e)
    }
  }

  override def addSchedulingEntities(entities: Map[String, Object]): Unit = {
    try {
      volatileScheduler.getContext().putAll(entities)
    } catch {
      case e: SchedulerException =>
        throw new SmartSpacesException("Unable to add map of entities to the volatileScheduler", e)
    }
    try {
      persistedScheduler.getContext().putAll(entities)
    } catch {
      case e: SchedulerException =>
        throw new SmartSpacesException("Unable to add map of entities to the persistedScheduler", e)
    }
  }

  override def scheduleScriptWithCron(jobName: String, groupName: String, id: String, schedule: String): Unit = {
    try {
      val detail = JobBuilder.newJob(classOf[SimpleScriptSchedulerJob])
        .withIdentity(jobName, groupName).build()
      val jobDataMap = detail.getJobDataMap()
      jobDataMap.put(StandardQuartzSchedulerService.JOB_MAP_PROPERTY_SCRIPT_ID, id)

      val trigger =
        TriggerBuilder.newTrigger().withIdentity(TriggerKey.triggerKey(jobName, groupName))
          .withSchedule(CronScheduleBuilder.cronSchedule(schedule)).build()

      persistedScheduler.scheduleJob(detail, trigger)
    } catch {
      case e: Throwable =>
        throw new SmartSpacesException(s"Unable to schedule job ${groupName}:${jobName}", e)
    }
  }

  override def scheduleAction(jobName: String, groupName: String, actionSource: String,
    actionName: String, data: Map[String, Object], when: Date): Unit = {
    try {
      val detail =
        JobBuilder.newJob(classOf[ActionSchedulerJob]).withIdentity(jobName, groupName).build()
      val jobDataMap = detail.getJobDataMap()
      jobDataMap.put(StandardQuartzSchedulerService.JOB_MAP_PROPERTY_ACTION_SOURCE, actionSource)
      jobDataMap.put(StandardQuartzSchedulerService.JOB_MAP_PROPERTY_ACTION_NAME, actionName)

      if (data != null) {
        jobDataMap.putAll(data)
      }

      val trigger = TriggerBuilder.newTrigger().withIdentity(TriggerKey.triggerKey(jobName, groupName)).startAt(when).build()

      persistedScheduler.scheduleJob(detail, trigger)

      getSpaceEnvironment.getLog.info("Scheduled job ${groupName}:${jobName} for ${new SimpleDateFormat(\"MM/dd/yyyy@HH:mm:ss\").format(when)}\n")
    } catch {
      case e: Throwable =>
        throw new SmartSpacesException(s"Unable to schedule job ${groupName}:${jobName}", e)
    }
  }

  override def scheduleActionWithCron(jobName: String, groupName: String, actionSource: String,
    actionName: String, data: Map[String, Object], schedule: String): Unit = {
    try {
      val detail =
        JobBuilder.newJob(classOf[ActionSchedulerJob]).withIdentity(jobName, groupName).build()
      val jobDataMap = detail.getJobDataMap()
      jobDataMap.put(StandardQuartzSchedulerService.JOB_MAP_PROPERTY_ACTION_SOURCE, actionSource)
      jobDataMap.put(StandardQuartzSchedulerService.JOB_MAP_PROPERTY_ACTION_NAME, actionName)

      if (data != null) {
        jobDataMap.putAll(data)
      }

      val trigger =
        TriggerBuilder.newTrigger().withIdentity(TriggerKey.triggerKey(jobName, groupName))
          .withSchedule(CronScheduleBuilder.cronSchedule(schedule)).build()

      persistedScheduler.scheduleJob(detail, trigger)

      getSpaceEnvironment().getLog().formatInfo("Scheduled job %s:%s with cron %s", groupName,
        jobName, schedule)
    } catch {
      case e: Throwable =>
        throw new SmartSpacesException(s"Unable to schedule job ${groupName}:${jobName}", e)
    }
  }

  override def removeJobGroup(jobGroupName: String): Unit = {
    try {
      val jobs = persistedScheduler.getJobKeys(GroupMatcher.jobGroupEquals(jobGroupName))
      jobs.foreach { job =>
        persistedScheduler.deleteJob(job)
      }
    } catch {
      case e: SchedulerException =>
        throw new SmartSpacesException(
          String.format("Unable to remove job group %s jobs", jobGroupName), e)
    }
  }

  override def getJobGroupNames(): Set[String] = {
    try {
      return Sets.newHashSet(persistedScheduler.getJobGroupNames())
    } catch {
      case e: SchedulerException =>
        throw new SmartSpacesException("Unable to get all job group names", e)
    }
  }

  override def getGroupJobNames(groupName: String): Set[String] = {
    try {
      val jobNames: Set[String] = new HashSet

      val jobKeys = persistedScheduler.getJobKeys(GroupMatcher.groupEquals(groupName))
      jobKeys foreach { key =>
        jobNames.add(key.getName())
      }

      return jobNames
    } catch {
      case e: SchedulerException =>
        throw new SmartSpacesException("Unable to get all job group names", e)
    }
  }

  override def doesJobExist(groupName: String, jobName: String): Boolean = {
    try {
      return persistedScheduler.checkExists(new JobKey(jobName, groupName));
    } catch {
      case e: SchedulerException =>
        throw SmartSpacesException.newFormattedException(e, "Could not check for job %s:%s",
          groupName, jobName);
    }
  }
}

/**
 * The job factory to use for Quartz job creation.
 *
 * @author Keith M. Hughes
 */
class MyJobFactory(spaceEnvironment: SmartSpacesEnvironment) extends JobFactory {

  override def newJob(bundle: TriggerFiredBundle, scheduler: Scheduler): Job = {
    val jobClass = bundle.getJobDetail().getJobClass()
    try {
      if (classOf[SmartSpacesSchedulerJob].isAssignableFrom(jobClass)) {
        val constructor = jobClass.getConstructor(classOf[SmartSpacesEnvironment], classOf[ExtendedLog])
        if (constructor != null) {
          return constructor.newInstance(spaceEnvironment, spaceEnvironment.getLog)
        } else {
          throw new SchedulerException(String.format(
            "SmartSpaces job class %s does not have a proper constructor", jobClass.getName()))
        }
      } else {
        return jobClass.getConstructor().newInstance();
      }
    } catch {
      case e: Throwable =>
        throw new SchedulerException("Could not instantiate job class " + jobClass, e);
    }
  }
}

/**
 * The base class for all schuler jobs that are SmartSpaces jobs.
 *
 * @author Keith M. Hughes
 */
abstract class SmartSpacesSchedulerJob(spaceEnvironment: SmartSpacesEnvironment, log: ExtendedLog) extends Job {
}

/**
 * The job which the volatileScheduler will run.
 *
 * @author Keith M. Hughes
 */
class SimpleScriptSchedulerJob(spaceEnvironment: SmartSpacesEnvironment, log: ExtendedLog) extends SmartSpacesSchedulerJob(spaceEnvironment, log) {

  override def execute(context: JobExecutionContext): Unit = {
    try {
      val scriptId =
        context.getJobDetail().getJobDataMap().get(StandardQuartzSchedulerService.JOB_MAP_PROPERTY_SCRIPT_ID).asInstanceOf[String]

      log.info(s"Running script ${scriptId}");
    } catch {
      case e: Throwable =>
        log.error("Could not run scheduled job", e);
    }
  }
}

/**
 * The job which the scheduler will run.
 *
 * @author Keith M. Hughes
 */
class ActionSchedulerJob(spaceEnvironment: SmartSpacesEnvironment, log: ExtendedLog) extends SmartSpacesSchedulerJob(spaceEnvironment, log) {

  override def execute(context: JobExecutionContext): Unit = {
    try {
      val jobDataMap = context.getMergedJobDataMap()
      val executionContext = new StandardExecutionContext(
        spaceEnvironment.getContainerManagedScope(), spaceEnvironment, log)
      executionContext.setValues(jobDataMap)

      val actionService: ActionService = spaceEnvironment.getServiceRegistry.getRequiredService(ActionService.SERVICE_NAME)
      actionService.performAction(
        jobDataMap.getString(StandardQuartzSchedulerService.JOB_MAP_PROPERTY_ACTION_SOURCE),
        jobDataMap.getString(StandardQuartzSchedulerService.JOB_MAP_PROPERTY_ACTION_NAME), executionContext)
    } catch {
      case e: Throwable =>

        log.error("Could not run scheduled job", e)
    }
  }
}
