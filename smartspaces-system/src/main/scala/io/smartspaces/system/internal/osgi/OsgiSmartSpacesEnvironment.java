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

package io.smartspaces.system.internal.osgi;

import io.smartspaces.configuration.Configuration;
import io.smartspaces.event.observable.EventObservableRegistry;
import io.smartspaces.event.observable.StandardEventObservableRegistry;
import io.smartspaces.logging.ExtendedLog;
import io.smartspaces.logging.StandardExtendedLog;
import io.smartspaces.scope.ManagedScope;
import io.smartspaces.service.ServiceRegistry;
import io.smartspaces.service.StandardServiceRegistry;
import io.smartspaces.system.InternalSmartSpacesEnvironment;
import io.smartspaces.system.SmartSpacesEnvironment;
import io.smartspaces.system.SmartSpacesFilesystem;
import io.smartspaces.system.core.logging.LoggingProvider;
import io.smartspaces.time.provider.TimeProvider;

import com.google.common.collect.Maps;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ScheduledExecutorService;

/**
 * A {@link SmartSpacesEnvironment} which lives in an OSGi container.
 *
 * @author Keith M. Hughes
 */
public class OsgiSmartSpacesEnvironment
    implements SmartSpacesEnvironment, InternalSmartSpacesEnvironment {

  /**
   * The system configuration.
   */
  private Configuration systemConfiguration;

  /**
   * The executor service to use for thread pools.
   */
  private ScheduledExecutorService executorService;

  /**
   * The file system for Smart Spaces.
   */
  private SmartSpacesFilesystem filesystem;

  /**
   * Network type for the container.
   *
   * <p>
   * This allows distinguishing between different Smart Spaces networks, e.g.
   * localdev, prod, fredbot.
   */
  private String networkType;

  /**
   * The time provider for everyone to use.
   */
  private TimeProvider timeProvider;

  /**
   * Values stored in the environment.
   */
  private ConcurrentMap<String, Object> values = Maps.newConcurrentMap();

  /**
   * The service registry.
   */
  private ServiceRegistry serviceRegistry = new StandardServiceRegistry(this);
  
  /**
   * The event observable registry.
   */
  private EventObservableRegistry eventObservableRegistry = new StandardEventObservableRegistry();

  /**
   * The platform logging provider.
   */
  private LoggingProvider loggingProvider;

  /**
   * The extended logger.
   */
  private ExtendedLog log;

  /**
   * The container level managed scope.
   */
  private ManagedScope containerManagedScope;

  /**
   * The loggers keyed by log name.
   */
  private Map<String, StandardExtendedLog> logs = new HashMap<>();

  @Override
  public Configuration getSystemConfiguration() {
    return systemConfiguration;
  }

  @Override
  public String getNetworkType() {
    return networkType;
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
  public ExtendedLog getLog() {
    return log;
  }

  @Override
  public ExtendedLog getLog(String logName, String level, String filename) {
    // TODO(keith): make this generate extended logs, though they will need to
    // be in a map.
    StandardExtendedLog log = logs.get(logName);
    if (log == null) {
      log = new StandardExtendedLog(logName, loggingProvider.getLog(logName, level, filename));
      logs.put(logName, log);
    }
    return log;
  }

  @Override
  public boolean modifyLogLevel(ExtendedLog log, String level) {
    StandardExtendedLog slog = (StandardExtendedLog) log;
    return loggingProvider.modifyLogLevel(slog.getDelegate(), level);
  }

  @Override
  public void releaseLog(ExtendedLog log) {
    StandardExtendedLog slog = (StandardExtendedLog) log;
    logs.remove(slog.getLogName());
    loggingProvider.releaseLog(slog.getDelegate());
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

  @Override
  public ManagedScope getContainerManagedScope() {
    return containerManagedScope;
  }

  @Override
  public <T> T getValue(String valueName) {
    @SuppressWarnings("unchecked")
    T value = (T) values.get(valueName);

    return value;
  }

  @Override
  public void setValue(String valueName, Object value) {
    values.put(valueName, value);
  }

  @Override
  public void removeValue(String valueName) {
    values.remove(valueName);
  }

  @Override
  public void setFilesystem(SmartSpacesFilesystem filesystem) {
    this.filesystem = filesystem;
  }

  @Override
  public void setNetworkType(String networkType) {
    this.networkType = networkType;
  }

  @Override
  public void setExecutorService(ScheduledExecutorService executorService) {
    this.executorService = executorService;
  }

  @Override
  public void setSystemConfiguration(Configuration systemConfiguration) {
    this.systemConfiguration = systemConfiguration;
  }

  @Override
  public void changeSystemConfigurationTop(Configuration newTopSystemConfiguration) {
    newTopSystemConfiguration.setParent(systemConfiguration);
    systemConfiguration = newTopSystemConfiguration;
  }

  @Override
  public void setTimeProvider(TimeProvider timeProvider) {
    this.timeProvider = timeProvider;
  }

  @Override
  public void setLoggingProvider(LoggingProvider loggingProvider, ExtendedLog containerLog) {
    this.loggingProvider = loggingProvider;

    log = containerLog;
  }

  @Override
  public void setContainerManagedScope(ManagedScope containerManagedScope) {
    this.containerManagedScope = containerManagedScope;
  }
}
