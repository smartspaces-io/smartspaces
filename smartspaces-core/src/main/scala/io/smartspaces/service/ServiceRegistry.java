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

package io.smartspaces.service;

import io.smartspaces.SmartSpacesException;

import java.util.Set;

/**
 * A registry of services which can be used.
 *
 * @author Keith M. Hughes
 */
public interface ServiceRegistry {

  /**
   * Register a service with the registry.
   *
   * @param service
   *          the service instance
   */
  void registerService(Service service);

  /**
   * Start up a service and register it with the registry.
   * 
   * <p>
   * All dependencies will be supplied, e.g. the space environment.
   *
   * @param service
   *          the service instance
   */
  void startupAndRegisterService(Service service);

  /**
   * Unregister a service with the registry.
   *
   * <p>
   * Does nothing if the service wasn't registered.
   *
   * @param service
   *          the service to unregister
   */
  void unregisterService(Service service);

  /**
   * Shutdown and unregister a service with the registry.
   *
   * <p>
   * Does nothing if the service wasn't registered.
   *
   * @param service
   *          the service to unregister
   */
  void shutdownAndUnregisterService(Service service);

  /**
   * Get all the service descriptions for all registered services.
   *
   * @return all the service descriptions for all registered services
   */
  Set<ServiceDescription> getAllServiceDescriptions();

  /**
   * Get a given service from the registry.
   *
   * @param <T>
   *          type of the service the name of the desired service
   * @param name
   *          the name of the desired service
   *
   * @return the requested service, or {@code null} if there is no such service
   *         registered with the specified name
   */
  <T extends Service> T getService(String name);

  /**
   * Get a required service from the registry.
   *
   * @param <T>
   *          type of the service
   * @param name
   *          the name of the desired service
   *
   * @return the requested service
   *
   * @throws SmartSpacesException
   *           no service with the given name was found
   */
  <T extends Service> T getRequiredService(String name) throws SmartSpacesException;

  /**
   * Add in a listener for service notifications.
   * 
   * <p>
   * If the service is already registered, this will be called immediately.
   * 
   * @param notificationListener
   *          the listener to add
   */
  void addServiceNotificationListener(String serviceName,
      ServiceNotification<?> notificationListener);
}
