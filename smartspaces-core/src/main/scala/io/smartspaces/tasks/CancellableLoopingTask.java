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

import io.smartspaces.SmartSpacesException;
import io.smartspaces.system.SmartSpacesEnvironment;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicReference;

/**
 * An interruptable loop.
 *
 * This should be run with the {@link ExecutorService} obtained from
 * {@link SmartSpacesEnvironment#getExecutorService()}.
 *
 * @author Keith M. Hughes
 */
public abstract class CancellableLoopingTask implements Runnable {

  /**
   * The {@link Thread} the code will be running in.
   */
  private Thread thread;

  /**
   * Any exception which may have happened during execution.
   */
  private AtomicReference<Exception> exception = new AtomicReference<Exception>();

  /**
   * Runtime state of the loop.
   */
  private AtomicReference<TaskState> runtimeState = new AtomicReference<TaskState>(
      TaskState.READY);

  @Override
  public void run() {
    synchronized (this) {
      if (thread != null) {
        throw new SmartSpacesException("Loop already running");
      }

      thread = Thread.currentThread();
      runtimeState.set(TaskState.READY);
    }
    try {
      setup();
      while (!thread.isInterrupted()) {
        loop();
      }

      runtimeState.set(TaskState.SHUTDOWN);
    } catch (InterruptedException e) {
      runtimeState.set(TaskState.SHUTDOWN);
      handleInterruptedException(e);
    } catch (Exception e) {
      exception.set(e);
      runtimeState.set(TaskState.CRASH);
      handleException(e);
    } finally {
      thread = null;

      cleanup();
    }
  }

  /**
   * The setup block for the loop. This will be called exactly once before the
   * first call to {@link #loop()}.
   */
  protected void setup() {
    // Do nothing by default.
  }

  /**
   * The cleanup block for the loop. This will be called exactly once after the
   * loop has exited for any reason.
   */
  protected void cleanup() {
    // Do nothing by default.
  }

  /**
   * The body of the loop. This will run continuously until the
   * {@link CancellableLoopingTask} has been interrupted externally or by calling
   * {@link #cancel()}.
   */
  protected abstract void loop() throws InterruptedException;

  /**
   * An {@link InterruptedException} was thrown.
   */
  protected void handleInterruptedException(InterruptedException e) {
    // Ignore InterruptedExceptions by default.
  }

  /**
   * An {@link Exception} other than an {@link InterruptedException} was thrown.
   */
  protected void handleException(Exception e) {
    // Ignore Exceptions by default.
  }

  /**
   * Interrupts the loop.
   */
  public void cancel() {
    if (thread != null) {
      thread.interrupt();
    }
  }

  /**
   * @return {@code true} if the loop is running
   */
  public synchronized boolean isRunning() {
    return thread != null && !thread.isInterrupted();
  }

  /**
   * Get the exception that was thrown during the loop running.
   *
   * @return the exception thrown, can be {@code null} if no exception has yet
   *         been thrown
   */
  public Exception getException() {
    return exception.get();
  }
}
