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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import io.smartspaces.system.SmartSpacesEnvironment;
import io.smartspaces.tasks.AcceptingPriorityTaskQueue;
import io.smartspaces.tasks.SimpleAcceptingPriorityTaskQueue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.logging.Log;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.ros.concurrent.DefaultScheduledExecutorService;

/**
 * A set of tests for the {@link SimpleAcceptingPriorityTaskQueue}.
 *
 * @author Keith M. Hughes
 */
public class AcceptingPriorityTaskQueueTest {

  private Log log;

  private SmartSpacesEnvironment spaceEnvironment;

  private ScheduledExecutorService executorService;

  private AcceptingPriorityTaskQueue queue;

  @Before
  public void setup() {
    executorService = new DefaultScheduledExecutorService();

    log = Mockito.mock(Log.class);
    spaceEnvironment = Mockito.mock(SmartSpacesEnvironment.class);

    Mockito.when(spaceEnvironment.getExecutorService()).thenReturn(executorService);

    queue = new SimpleAcceptingPriorityTaskQueue(spaceEnvironment, log);
  }

  @After
  public void cleanup() {
    queue.shutdown();
    executorService.shutdown();
  }

  /**
   * Test that the queue is accepting tasks and task queue was running when
   * added.
   */
  @Test
  public void testAcceptStartupBefore() throws Exception {
    queue.setAccepting(true);
    queue.startup();

    InOrderTask task = new InOrderTask();
    queue.addTask(task);

    // Assume it gets answered in under a second.
    assertTrue(task.await(1, TimeUnit.SECONDS));
  }

  /**
   * Test that the queue is accepting tasks and task queue wasn't running when
   * added.
   */
  @Test
  public void testAcceptStartupLater() throws Exception {
    queue.setAccepting(true);

    InOrderTask task = new InOrderTask();
    queue.addTask(task);

    queue.startup();

    // Assume it gets answered in under a second.
    assertTrue(task.await(1, TimeUnit.SECONDS));
  }

  /**
   * Test that the queue is accepting tasks but nothing gets called.
   */
  @Test
  public void testAcceptButNoProcess() throws Exception {
    queue.setAccepting(true);

    InOrderTask task = new InOrderTask();
    queue.addTask(task);

    // Assume it gets answered in under a second.
    assertFalse(task.await(1, TimeUnit.SECONDS));
  }

  /**
   * Test that the queue is not accepting tasks and task queue wasn't running
   * when added.
   */
  @Test
  public void testNotAcceptStartupLater() throws Exception {
    queue.setAccepting(false);

    InOrderTask task = new InOrderTask();
    queue.addTask(task);

    queue.startup();

    // Assume it gets answered in under a second.
    assertFalse(task.await(1, TimeUnit.SECONDS));
  }

  /**
   * Test that the queue is not accepting tasks and task queue wasn't running
   * when added.
   */
  @Test
  public void testNotAcceptStartupBefore() throws Exception {
    queue.setAccepting(false);
    queue.startup();

    InOrderTask task = new InOrderTask();
    queue.addTask(task);

    // Assume it gets answered in under a second.
    assertFalse(task.await(1, TimeUnit.SECONDS));
  }

  /**
   * Make sure the higher priority task is handled first.
   */
  @Test
  public void testPriority() throws Exception {
    queue.setAccepting(true);

    InOrderTask task1 = new InOrderTask();
    InOrderTask task2 = new InOrderTask();
    queue.addTask(task2, 10);
    queue.addTask(task1, 1);

    queue.startup();

    // Assume it gets answered in under a second.
    assertTrue(task2.await(1, TimeUnit.SECONDS));
    assertTrue(task1.await(1, TimeUnit.SECONDS));

    assertTrue(task1.getOrder() < task2.getOrder());
  }

  public static class InOrderTask implements Runnable {
    public static final AtomicInteger sequence = new AtomicInteger();

    private int order;

    private CountDownLatch latch = new CountDownLatch(1);

    @Override
    public void run() {
      order = sequence.incrementAndGet();
      latch.countDown();
    }

    public boolean await(long timeout, TimeUnit unit) throws InterruptedException {
      return latch.await(timeout, unit);
    }

    public int getOrder() {
      return order;
    }

  }

}
