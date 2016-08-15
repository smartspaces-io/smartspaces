package io.smartspaces.tasks;

import io.smartspaces.resource.managed.ManagedResource;

public interface AcceptingPriorityTaskQueue extends ManagedResource {

  /**
   * The base priority for the task.
   */
  int DEFAULT_PRIORITY = 10;

  /**
   * Start the task queue processing tasks which come in.
   *
   * <p>
   * This is independent from accepting new tasks.
   */
  void startup();

  /**
   * Stop the task queue from processing tasks which come in.
   *
   * <p>
   * This is independent from accepting new tasks.
   */
  void shutdown();

  /**
   * Stop accepting new tasks and shut down the queue.
   */
  void stopAcceptingAndShutdown();

  /**
   * Is the task queue running?
   *
   * @return {@code true} if it is running.
   */
  boolean isRunning();

  /**
   * Set whether the queue is accepting new tasks or not.
   *
   * @param accepting
   *          {@code true} if accepting new tasks
   */
  void setAccepting(boolean accepting);

  /**
   * Add a new task to the queue with the default priority
   * {@link #DEFAULT_PRIORITY}.
   *
   * @param task
   *          the new task
   */
  void addTask(Runnable task);

  /**
   * Add a new task to the queue.
   *
   * @param task
   *          the new task
   * @param priority
   *          priority of the task, lower values run first
   */
  void addTask(Runnable task, int priority);
}