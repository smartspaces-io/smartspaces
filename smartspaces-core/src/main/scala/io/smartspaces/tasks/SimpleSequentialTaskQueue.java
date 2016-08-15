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

package io.smartspaces.tasks;

import io.smartspaces.system.SmartSpacesEnvironment;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.logging.Log;

/**
 * A task queue which will run its event handlers in First in, First Out
 * order.
 *
 * @author Keith M. Hughes
 */
public class SimpleSequentialTaskQueue implements SequentialTaskQueue {

  /**
   * The list of events to process.
   */
  private BlockingQueue<Runnable> tasks = new LinkedBlockingQueue<Runnable>();

  /**
   * The queue future, used for shutting the queue down.
   */
  private Future<?> queueFuture;

  /**
   * The space environment to run under.
   */
  private SmartSpacesEnvironment spaceEnvironment;

  /**
   * The logger for errors.
   */
  private Log log;

  /**
   * Construct a new task queue.
   *
   * @param spaceEnvironment
   *          the space environment the queue will use
   * @param log
   *          the logger to use
   */
  public SimpleSequentialTaskQueue(SmartSpacesEnvironment spaceEnvironment, Log log) {
    this.spaceEnvironment = spaceEnvironment;
    this.log = log;
  }

  @Override
  public void startup() {
    queueFuture = spaceEnvironment.getExecutorService().submit(new Runnable() {
      @Override
      public void run() {
        processTasks();
      }
    });
  }

  @Override
  public void shutdown() {
    if (queueFuture != null) {
      queueFuture.cancel(true);
      queueFuture = null;

      tasks.clear();
    }
  }

  @Override
  public void addTask(Runnable task) {
    try {
      tasks.put(task);
    } catch (InterruptedException e) {
      // Don't care
    }
  }

  /**
   * Process tasks until the task processing thread is interrupted.
   */
  private void processTasks() {
    try {
      while (!Thread.interrupted()) {
        processNextTask();
      }
    } catch (InterruptedException e) {
      // Don't care
    }
  }

  /**
   * Process the next event.
   *
   * @throws InterruptedException
   *           the thread got interrupted
   */
  private void processNextTask() throws InterruptedException {
    try {
      Runnable task = tasks.take();

      task.run();
    } catch (InterruptedException e) {
      throw e;
    } catch (Exception e) {
      log.error("Error during task processing", e);
    }
  }
}
