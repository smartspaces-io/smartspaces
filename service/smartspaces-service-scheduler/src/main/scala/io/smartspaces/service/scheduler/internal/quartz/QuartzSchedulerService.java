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

package io.smartspaces.service.scheduler.internal.quartz;

import io.smartspaces.SmartSpacesException;
import io.smartspaces.evaluation.ExecutionContext;
import io.smartspaces.evaluation.StandardExecutionContext;
import io.smartspaces.scheduling.quartz.orientdb.OrientDbJobStore;
import io.smartspaces.service.BaseSupportedService;
import io.smartspaces.service.action.ActionService;
import io.smartspaces.service.scheduler.SchedulerService;
import io.smartspaces.system.SmartSpacesEnvironment;

import org.apache.commons.logging.Log;
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.quartz.impl.DirectSchedulerFactory;
import org.quartz.simpl.RAMJobStore;
import org.quartz.simpl.SimpleThreadPool;
import org.quartz.spi.JobFactory;
import org.quartz.spi.TriggerFiredBundle;

import java.lang.reflect.Constructor;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * A {@link SchedulerService} which uses quartz.
 *
 * @author Keith M. Hughes
 */
public class QuartzSchedulerService extends BaseSupportedService implements SchedulerService {
  private static final String ORIENTDB_URI = "PLOCAL:/var/tmp/quartz";
  private static final String ORIENTDB_USER = "sooperdooper";
  private static final String ORIENTDB_PASSWORD = "sooperdooper";
  private static final String PERSISTED_SCHEDULER_NAME = "SmartSpacesSchedulerPersisted";
  private static final String PERSISTED_SCHEDULER_INSTANCE_ID =
      "MainSchedulerNonClusteredPersisted";

  private static final String VOLATILE_SCHEDULER_NAME = "SmartSpacesSchedulerVolatile";
  private static final String VOLATILE_SCHEDULER_INSTANCE_ID = "MainSchedulerNonClusteredVolatile";

  /**
   * JobMap property for the runnable which will run.
   */
  public static final String JOB_MAP_PROPERTY_RUNNABLE = "runnable";

  /**
   * JobMap property for the ID of the script which will run.
   */
  public static final String JOB_MAP_PROPERTY_SCRIPT_ID = "runnable";

  /**
   * JobMap property for the logger to use.
   */
  public static final String JOB_MAP_PROPERTY_LOGGER = "logger";

  /**
   * JobMap property for the logger to use.
   */
  public static final String JOB_MAP_PROPERTY_SPACE_ENVIRONMENT = "spaceEnvironment";

  /**
   * JobMap property for the action name.
   */
  public static final String JOB_MAP_PROPERTY_ACTION_NAME = "action.name";

  /**
   * JobMap property for the action source.
   */
  public static final String JOB_MAP_PROPERTY_ACTION_SOURCE = "action.source";

  /**
   * The volatile volatileScheduler.
   */
  private Scheduler volatileScheduler;

  /**
   * The persisted scheduler.
   */
  private Scheduler persistedScheduler;

  @Override
  public String getName() {
    return SchedulerService.SERVICE_NAME;
  }

  @Override
  public void startup() {
    try {
      MyJobFactory jobFactory = new MyJobFactory();
      
      // TODO(keith): Get Smart Spaces thread pool in here.
      OrientDbJobStore jobStore =
          new OrientDbJobStore(ORIENTDB_URI, ORIENTDB_USER, ORIENTDB_PASSWORD);
      SimpleThreadPool threadPool = new SimpleThreadPool(10, Thread.NORM_PRIORITY);

      DirectSchedulerFactory instance = DirectSchedulerFactory.getInstance();

      instance.createScheduler(PERSISTED_SCHEDULER_NAME, PERSISTED_SCHEDULER_INSTANCE_ID,
          threadPool, jobStore);

      persistedScheduler = instance.getScheduler(PERSISTED_SCHEDULER_NAME);
      persistedScheduler.setJobFactory(jobFactory);

      persistedScheduler.start();

      instance.createScheduler(VOLATILE_SCHEDULER_NAME, VOLATILE_SCHEDULER_INSTANCE_ID, threadPool,
          new RAMJobStore());

      volatileScheduler = instance.getScheduler(VOLATILE_SCHEDULER_NAME);
      volatileScheduler.setJobFactory(jobFactory);

      volatileScheduler.start();
    } catch (SchedulerException e) {
      throw new SmartSpacesException("Could not start Smart Spaces volatileScheduler", e);
    }
  }

  @Override
  public void shutdown() {
    try {
      volatileScheduler.shutdown();
    } catch (SchedulerException e) {
      throw new SmartSpacesException("Could not shutdown Smart Spaces volatileScheduler", e);
    }
  }

  @Override
  public void addSchedulingEntities(Map<String, Object> entities) {
    try {
      volatileScheduler.getContext().putAll(entities);
    } catch (SchedulerException e) {
      throw new SmartSpacesException("Unable to add map of entities to the volatileScheduler", e);
    }
  }

  @Override
  public void schedule(String jobName, String groupName, Runnable runnable, Date when) {
    try {
      JobDetail detail =
          JobBuilder.newJob(SimpleSchedulerJob.class).withIdentity(jobName, groupName).build();
      detail.getJobDataMap().put(JOB_MAP_PROPERTY_RUNNABLE, runnable);
      detail.getJobDataMap().put(JOB_MAP_PROPERTY_LOGGER, getSpaceEnvironment().getLog());

      Trigger trigger = TriggerBuilder.newTrigger()
          .withIdentity(TriggerKey.triggerKey(jobName, groupName)).startAt(when).build();
      volatileScheduler.scheduleJob(detail, trigger);

      getSpaceEnvironment().getLog().info(String.format("Scheduled job %s:%s for %s\n", groupName,
          jobName, new SimpleDateFormat("MM/dd/yyyy@HH:mm:ss").format(when)));
    } catch (SchedulerException e) {
      throw new SmartSpacesException(
          String.format("Unable to schedule job %s:%s", groupName, jobName), e);
    }
  }

  @Override
  public void scheduleScriptWithCron(String jobName, String groupName, String id, String schedule) {
    try {
      JobDetail detail = JobBuilder.newJob(SimpleScriptSchedulerJob.class)
          .withIdentity(jobName, groupName).build();
      JobDataMap jobDataMap = detail.getJobDataMap();
      jobDataMap.put(JOB_MAP_PROPERTY_SCRIPT_ID, id);
      jobDataMap.put(JOB_MAP_PROPERTY_LOGGER, getSpaceEnvironment().getLog());

      CronTrigger trigger =
          TriggerBuilder.newTrigger().withIdentity(TriggerKey.triggerKey(jobName, groupName))
              .withSchedule(CronScheduleBuilder.cronSchedule(schedule)).build();

      volatileScheduler.scheduleJob(detail, trigger);
    } catch (Exception e) {
      throw new SmartSpacesException(
          String.format("Unable to schedule job %s:%s", groupName, jobName), e);
    }
  }

  /**
   * The job factory to use for Quartz job creation.
   * 
   * @author Keith M. Hughes
   */
  public class MyJobFactory implements JobFactory {

    /**
     * The parameter types for SmartSpacesSchedulerJob subclass constructors.
     */
    private final Class<?>[] SMARTSPACES_JOB_CONSTRUCTOR_PARAMETER_TYPES =
        new Class<?>[] { SmartSpacesEnvironment.class, Log.class };

    @Override
    public Job newJob(TriggerFiredBundle bundle, Scheduler scheduler) throws SchedulerException {
      Class<? extends Job> jobClass = bundle.getJobDetail().getJobClass();
      try {
        if (SmartSpacesSchedulerJob.class.isAssignableFrom(jobClass)) {
          Constructor<? extends Job> constructor =
              jobClass.getConstructor(SMARTSPACES_JOB_CONSTRUCTOR_PARAMETER_TYPES);
          if (constructor != null) {
            return constructor.newInstance(getSpaceEnvironment(), getSpaceEnvironment().getLog());
          } else {
            throw new SchedulerException(String.format(
                "SmartSpaces job class %s does not have a proper constructor", jobClass.getName()));
          }
        } else {
          return jobClass.getConstructor().newInstance();
        }
      } catch (Exception e) {
        throw new SchedulerException("Could not instantiate job class " + jobClass, e);
      }
    }
  }

  /**
   * The job which the volatileScheduler will run.
   *
   * @author Keith M. Hughes
   */
  public static class SimpleSchedulerJob implements Job {

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
      try {
        Runnable runner =
            (Runnable) context.getJobDetail().getJobDataMap().get(JOB_MAP_PROPERTY_RUNNABLE);
        runner.run();
      } catch (Exception e) {
        ((Log) context.getJobDetail().getJobDataMap().get(JOB_MAP_PROPERTY_LOGGER))
            .error("Could not run scheduled job", e);
      }
    }

  }

  /**
   * The job which the volatileScheduler will run.
   *
   * @author Keith M. Hughes
   */
  public static class SimpleScriptSchedulerJob implements Job {

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
      try {
        String scriptId =
            (String) context.getJobDetail().getJobDataMap().get(JOB_MAP_PROPERTY_SCRIPT_ID);
        ((Log) context.getJobDetail().getJobDataMap().get(JOB_MAP_PROPERTY_LOGGER))
            .info(String.format("Running script %s", scriptId));
      } catch (Exception e) {
        ((Log) context.getJobDetail().getJobDataMap().get(JOB_MAP_PROPERTY_LOGGER))
            .error("Could not run scheduled job", e);
      }
    }
  }

  public static abstract class SmartSpacesSchedulerJob implements Job {

    /**
     * The action service.
     */
    private SmartSpacesEnvironment spaceEnvironment;

    /**
     * Logger for the class
     */
    private Log log;

    public SmartSpacesSchedulerJob(SmartSpacesEnvironment spaceEnvironment, Log log) {
      this.spaceEnvironment = spaceEnvironment;
      this.log = log;
    }

    protected Log getLog() {
      return log;
    }

    protected SmartSpacesEnvironment getSpaceEnvironment() {
      return spaceEnvironment;
    }
  }

  /**
   * The job which the scheduler will run.
   *
   * @author Keith M. Hughes
   */
  public static class ActionSchedulerJob extends SmartSpacesSchedulerJob {

    public ActionSchedulerJob(SmartSpacesEnvironment spaceEnvironment, Log log) {
      super(spaceEnvironment, log);
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
      try {
        JobDataMap jobDataMap = context.getMergedJobDataMap();
        ExecutionContext executionContext =
            new StandardExecutionContext(getSpaceEnvironment(), getLog());
        executionContext.setValues(jobDataMap);

        ActionService actionService = getSpaceEnvironment().getServiceRegistry()
            .getRequiredService(ActionService.SERVICE_NAME);
        actionService.performAction(jobDataMap.getString(JOB_MAP_PROPERTY_ACTION_SOURCE),
            jobDataMap.getString(JOB_MAP_PROPERTY_ACTION_NAME), executionContext);
      } catch (Exception e) {
        getLog().error("Could not run scheduled job", e);
      }
    }
  }

}
