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
import io.smartspaces.logging.ExtendedLog;
import io.smartspaces.logging.StandardExtendedLog;
import io.smartspaces.service.ServiceRegistry;
import io.smartspaces.time.provider.TimeProvider;

import org.apache.commons.logging.Log;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;

/**
 * An {@link SmartSpacesEnvironment} that can be simply put together.
 *
 * <p>
 * Usually used for testing.
 *
 * @author Keith M. Hughes
 */
public class SimpleSmartSpacesEnvironment implements SmartSpacesEnvironment {

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
   * The time provider.
   */
  private TimeProvider timeProvider;

  /**
   * Simple value map.
   */
  private Map<String, Object> values = new HashMap<>();

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
  public Log getLog() {
    return log;
  }

  @Override
  public ExtendedLog getExtendedLog() {
    return log;
  }

  @Override
  public Log getLog(String logName, String level, String filename) {
    // for now just return the system log
    return log;
  }

  @Override
  public boolean modifyLogLevel(Log log, String level) {
    // Not for now
    return false;
  }

  @Override
  public void releaseLog(Log log) {
    // Nothing to do.
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
   * @param systemConfiguration
   *          the systemConfiguration to set
   */
  public void setSystemConfiguration(Configuration systemConfiguration) {
    this.systemConfiguration = systemConfiguration;
  }

  /**
   * @param filesystem
   *          the filesystem to set
   */
  public void setFilesystem(SmartSpacesFilesystem filesystem) {
    this.filesystem = filesystem;
  }

  /**
   * Set the logger.
   *
   * @param log
   *          the log to set
   */
  public void setLog(Log log) {
    this.log = new StandardExtendedLog(log);
  }

  /**
   * Set the executor service.
   *
   * @param executorService
   *          the executorService to set
   */
  public void setExecutorService(ScheduledExecutorService executorService) {
    this.executorService = executorService;
  }

  /**
   * Set the service registry.
   *
   * @param serviceRegistry
   *          the service registry to set
   */
  public void setServiceRegistry(ServiceRegistry serviceRegistry) {
    this.serviceRegistry = serviceRegistry;
  }

  /**
   * Set the time provider.
   *
   * @param timeProvider
   *          the timeProvider to set
   */
  public void setTimeProvider(TimeProvider timeProvider) {
    this.timeProvider = timeProvider;
  }
}
