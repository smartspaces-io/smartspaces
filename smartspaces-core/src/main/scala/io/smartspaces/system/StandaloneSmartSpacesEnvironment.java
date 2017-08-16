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

package io.smartspaces.system;

import io.smartspaces.configuration.Configuration;
import io.smartspaces.configuration.SimpleConfiguration;
import io.smartspaces.event.observable.EventObservableRegistry;
import io.smartspaces.event.observable.StandardEventObservableRegistry;
import io.smartspaces.logging.ExtendedLog;
import io.smartspaces.logging.StandardExtendedLog;
import io.smartspaces.resource.managed.ManagedResource;
import io.smartspaces.resource.managed.ManagedResources;
import io.smartspaces.resource.managed.StandardManagedResources;
import io.smartspaces.scope.ManagedScope;
import io.smartspaces.scope.StandardManagedScope;
import io.smartspaces.service.Service;
import io.smartspaces.service.ServiceRegistry;
import io.smartspaces.service.StandardServiceRegistry;
import io.smartspaces.tasks.ManagedTasks;
import io.smartspaces.tasks.StandardManagedTasks;
import io.smartspaces.time.provider.SettableTimeProvider;
import io.smartspaces.time.provider.TimeProvider;

import org.apache.commons.logging.impl.Jdk14Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * An {@link SmartSpacesEnvironment} that can be used for standalone running of
 * components.
 *
 * @author Keith M. Hughes
 */
public final class StandaloneSmartSpacesEnvironment implements SmartSpacesEnvironment {

  /**
   * The number of threads to initialize in the scheduled thread pool.
   */
  private static final int NUM_THREADS_IN_POOL = 100;

  /**
   * Create a new {@link StandaloneSmartSpacesEnvironment}.
   *
   * @return the space environment
   */
  public static StandaloneSmartSpacesEnvironment newStandaloneSmartSpacesEnvironment() {
    StandaloneSmartSpacesEnvironment environment = new StandaloneSmartSpacesEnvironment();

    environment.systemConfiguration = SimpleConfiguration.newConfiguration();
    environment.executorService = Executors.newScheduledThreadPool(NUM_THREADS_IN_POOL);
    environment.log = new StandardExtendedLog("container", new Jdk14Logger("test.smartspaces"));
    environment.serviceRegistry = new StandardServiceRegistry(environment);
    environment.timeProvider = new SettableTimeProvider();
    environment.eventObservableRegistry = new StandardEventObservableRegistry(environment.log);
    environment.managedResources = new StandardManagedResources(environment.log);
    environment.managedTasks =
        new StandardManagedTasks(environment.executorService, environment.log);
    environment.managedScope =
        new StandardManagedScope(environment.managedResources, environment.managedTasks);
    environment.managedResources.startupResources();

    return environment;
  }

  /**
   * The system configuration.
   */
  private Configuration systemConfiguration;

  /**
   * The container file system.
   */
  private SmartSpacesFilesystem filesystem;

  /**
   * The log for the system.
   */
  private ExtendedLog log;

  /**
   * The executor service.
   */
  private ScheduledExecutorService executorService;

  /**
   * The service registry.
   */
  private ServiceRegistry serviceRegistry;
  
  /**
   * The event observable registry.
   */
  private EventObservableRegistry eventObservableRegistry;

  /**
   * The time provider.
   */
  private TimeProvider timeProvider;

  /**
   * Simple value map.
   */
  private final Map<String, Object> values = new HashMap<>();

  /**
   * The managed resources for this environment.
   */
  private ManagedResources managedResources;

  /**
   * The managed tasks for this environment.
   */
  private ManagedTasks managedTasks;

  /**
   * The managed scope for this environment.
   */
  private ManagedScope managedScope;

  /**
   * Construct a new environment.
   */
  private StandaloneSmartSpacesEnvironment() {
  }

  @Override
  public Configuration getSystemConfiguration() {
    return systemConfiguration;
  }

  @Override
  public SmartSpacesFilesystem getFilesystem() {
    return filesystem;
  }

  @Override
  public ScheduledExecutorService getExecutorService() {
    return executorService;
  }

  @Override
  public ManagedScope getContainerManagedScope() {
    return managedScope;
  }

  @Override
  public ExtendedLog getLog() {
    return log;
  }

  @Override
  public ExtendedLog getLog(String logName, String level, String filename) {
    // for now just return the system log
    return log;
  }

  @Override
  public boolean modifyLogLevel(ExtendedLog log, String level) {
    // Not for now
    return false;
  }

  @Override
  public void releaseLog(ExtendedLog log) {
    // Nothing to do
  }

  @Override
  public String getNetworkType() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public TimeProvider getTimeProvider() {
    return timeProvider;
  }

  @Override
  public ServiceRegistry getServiceRegistry() {
    return serviceRegistry;
  }
  
  @Override
  public EventObservableRegistry getEventObservableRegistry() {
    return eventObservableRegistry;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> T getValue(String valueName) {
    return (T) values.get(valueName);
  }

  @Override
  public void setValue(String valueName, Object value) {
    values.put(valueName, value);
  }

  @Override
  public void removeValue(String valueName) {
    values.remove(valueName);
  }

  /**
   * Shutdown the environment.
   */
  public void shutdown() {
    managedScope.shutdown();
    executorService.shutdown();
  }

  /**
   * Set the file system to use.
   *
   * @param filesystem
   *          the file system to use
   */
  public void setFilesystem(SmartSpacesFilesystem filesystem) {
    this.filesystem = filesystem;
  }

  /**
   * Set the time provider to use if the default isn't appropriate.
   *
   * @param timeProvider
   *          the new time provider
   */
  public void setTimeProvider(TimeProvider timeProvider) {
    this.timeProvider = timeProvider;
  }

  /**
   * Add a managed resource.
   *
   * @param resource
   *          the resource to add
   */
  public void addManagedResource(ManagedResource resource) {
    managedResources.addResource(resource);
  }

  /**
   * Register and start a service with the registry.
   * 
   * {@code SupportedService}s will be add to the managed resources for the
   * environment.
   *
   * @param service
   *          the service instance
   */
  public void registerAndStartService(Service service) {
    serviceRegistry.startupAndRegisterService(service);
  }
}
