/*
 * Copyright (C) 2016 Keith M. Hughes
 * Copyright (C) 2014 Google Inc.
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
import io.smartspaces.event.observable.EventObservableRegistry;
import io.smartspaces.logging.ExtendedLog;
import io.smartspaces.scope.ManagedScope;
import io.smartspaces.system.core.logging.LoggingProvider;
import io.smartspaces.time.provider.TimeProvider;

import java.util.concurrent.ScheduledExecutorService;

/**
 * A Smart spaces environment giving access to portions of the environment for
 * modification.
 *
 * @author Keith M. Hughes
 */
public interface InternalSmartSpacesEnvironment {

  /**
   * Set the file system for the environment.
   *
   * @param filesystem
   *          the filesystem
   */
  void setFilesystem(SmartSpacesFilesystem filesystem);

  /**
   * Set the network type for Smart Spaces.
   *
   * <p>
   * This allows distinguishing between Smart Spaces networks, e.g. localdev,
   * prod, fredbot.
   *
   * @param networkType
   *          the network type
   */
  void setNetworkType(String networkType);

  /**
   * Set the executor service for the environment.
   *
   * @param executorService
   *          the executorService
   */
  void setExecutorService(ScheduledExecutorService executorService);

  /**
   * Set the system configuration.
   *
   * @param systemConfiguration
   *          the system configuration
   */
  void setSystemConfiguration(Configuration systemConfiguration);

  /**
   * Change the system configuration by taking the configuration already
   * available and making it the parent of the configuration supplied here.
   *
   * @param newTopSystemConfiguration
   *          the new top of the system configuration
   */
  void changeSystemConfigurationTop(Configuration newTopSystemConfiguration);

  /**
   * set the time provider.
   *
   * @param timeProvider
   *          the time provider
   */
  void setTimeProvider(TimeProvider timeProvider);

  /**
   * Set the logging provider.
   *
   * @param loggingProvider
   *          the logging provider
   * @param containerLog
   *          the container log
   */
  void setLoggingProvider(LoggingProvider loggingProvider, ExtendedLog containerLog);

  /**
   * Set the container managed scope.
   *
   * @param containerManagedScope
   *          the container managed scope
   */
  void setContainerManagedScope(ManagedScope containerManagedScope);

  /**
   * Set the event observable registry.
   * 
   * @param eventObservableRegistry
   *          the registry
   */
  void setEventObservableRegistry(EventObservableRegistry eventObservableRegistry);
}
