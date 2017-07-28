/*
 * Copyright (C) 2016 Keith M. Hughes
 * Copyright (C) 2013 Google Inc.
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

import java.util.concurrent.Future;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.logging.Log;

import com.google.common.collect.Queues;

/**
 * A task queue which will run its task handlers in First in, First Out order.
 * Tasks can be given a priority. Lower priority values are handled before
 * higher priority values.
 *
 * <p>
 * The task queue can also be set to accepting new tasks or not.
 *
 * <p>
 * Integrates into Smart Spaces thread pools.
 *
 * @author Keith M. Hughes
 */
public class SimpleAcceptingPriorityTaskQueue implements AcceptingPriorityTaskQueue {

  /**
   * {@code true} if the queue is accepting new tasks.
   */
  private boolean accepting;

  /**
   * The task queue.
   */
  private PriorityBlockingQueue<PriorityTask> tasks = Queues.newPriorityBlockingQueue();

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
   * Lock for adding items to the queue.
   */
  private Object acceptingMutex = new Object();

  /**
   * Lock for starting and stopping the queue.
   */
  private Object runningMutex = new Object();

  public SimpleAcceptingPriorityTaskQueue(SmartSpacesEnvironment spaceEnvironment, Log log) {
    this.spaceEnvironment = spaceEnvironment;
    this.log = log;
  }

  @Override
  public void startup() {
    synchronized (runningMutex) {
      if (queueFuture == null) {
        queueFuture = spaceEnvironment.getExecutorService().submit(() ->
            processTasks());
      }
    }
  }

  @Override
  public void shutdown() {
    synchronized (runningMutex) {
      if (queueFuture != null) {
        queueFuture.cancel(true);
        queueFuture = null;
      }
    }
  }

  @Override
  public void stopAcceptingAndShutdown() {
    synchronized (runningMutex) {
      setAccepting(false);
      shutdown();
    }
  }

  @Override
  public boolean isRunning() {
    synchronized (runningMutex) {
      return queueFuture != null;
    }
  }

  @Override
  public void setAccepting(boolean accepting) {
    synchronized (acceptingMutex) {
      this.accepting = accepting;
    }
  }

  @Override
  public void addTask(Runnable task) {
    addTask(task, DEFAULT_PRIORITY);
  }

  @Override
  public void addTask(Runnable task, int priority) {
    synchronized (acceptingMutex) {
      if (accepting) {
        tasks.put(new PriorityTask(task, priority));
      }
    }
  }

  /**
   * Process tasks until the event processing thread is interrupted.
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
   * Process the next task.
   *
   * @throws InterruptedException
   */
  private void processNextTask() throws InterruptedException {
    try {
      PriorityTask task = tasks.take();

      task.run();
    } catch (InterruptedException e) {
      throw e;
    } catch (Exception e) {
      log.error("Error during event processing", e);
    }
  }

  /**
   * A task in the queue.
   *
   * @author Keith M. Hughes
   */
  private static class PriorityTask implements Comparable<PriorityTask> {

    /**
     * The sequence of tasks to support FIFO ordering.
     */
    private static final AtomicLong sequence = new AtomicLong();

    /**
     * The runnable for the task.
     */
    private Runnable runnable;

    /**
     * The priority of the task.
     */
    private int priority;

    /**
     * The sequence number to force FIFO.
     */
    private long sequenceNumber;

    /**
     * Construct a new task.
     * 
     * @param runnable
     *          the runnable for the task
     * @param priority
     *          the priority for the task
     */
    public PriorityTask(Runnable runnable, int priority) {
      this.runnable = runnable;
      this.priority = priority;
      this.sequenceNumber = sequence.getAndIncrement();
    }

    /**
     * Run the wrapped runnable.
     */
    public void run() {
      runnable.run();
    }

    @Override
    public int compareTo(PriorityTask o) {
      int res = priority - o.priority;
      if (res == 0 && this != o) {
        res = (priority < o.priority) ? -1 : 1;
      }
      
      if (res == 0) {
        if (sequenceNumber < o.sequenceNumber) {
          return -1;
        } else {
          return 1;
        }
      }

      return res;
    }
  }
}
