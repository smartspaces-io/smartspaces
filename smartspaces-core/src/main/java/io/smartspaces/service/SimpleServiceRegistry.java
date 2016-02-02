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

import io.smartspaces.SimpleSmartSpacesException;
import io.smartspaces.SmartSpacesException;
import io.smartspaces.system.SmartSpacesEnvironment;

import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * A simple implementation of the {@link ServiceRegistry}.
 *
 * @author Keith M. Hughes
 */
public class SimpleServiceRegistry implements ServiceRegistry {

  /**
   * All services in the registry.
   */
  private Map<String, ServiceEntry> services = Maps.newHashMap();

  /**
   * The space environment for services.
   */
  private SmartSpacesEnvironment spaceEnvironment;

  /**
   * Construct a new registry.
   *
   * @param spaceEnvironment
   *          the space environment to use
   */
  public SimpleServiceRegistry(SmartSpacesEnvironment spaceEnvironment) {
    this.spaceEnvironment = spaceEnvironment;
  }

  @Override
  public synchronized void registerService(Service service) {

    // TODO(keith): Support multiple services with the same name of the
    // service.
    services.put(service.getName(), new ServiceEntry(service, service.getMetadata()));

    service.setSpaceEnvironment(spaceEnvironment);

    spaceEnvironment.getLog().info(
        String.format("Service registered with name %s", service.getName()));
  }

  @Override
  public synchronized void unregisterService(Service service) {
    spaceEnvironment.getLog().info(
        String.format("Service unregistering with name %s", service.getName()));
    services.remove(service.getName());
  }

  @Override
  public synchronized Set<ServiceDescription> getAllServiceDescriptions() {
    Set<ServiceDescription> allDescriptions = Sets.newHashSet();

    // TODO(keith): Cache these as services are registered and unregistered.
    for (ServiceEntry entry : services.values()) {
      allDescriptions.add(entry.getService().getServiceDescription());
    }

    return allDescriptions;
  }

  @Override
  public synchronized <T extends Service> T getService(String name) {
    ServiceEntry entry = services.get(name);
    if (entry != null) {
      @SuppressWarnings("unchecked")
      T service = (T) entry.getService();

      return service;
    } else {
      return null;
    }
  }

  @Override
  public synchronized <T extends Service> T getRequiredService(String name)
      throws SmartSpacesException {
    ServiceEntry entry = services.get(name);
    if (entry != null) {
      @SuppressWarnings("unchecked")
      T service = (T) entry.getService();

      return service;
    } else {
      throw new SimpleSmartSpacesException(String.format("No service found with name %s", name));
    }
  }

  /**
   * An entry in the service map.
   *
   * @author Keith M. Hughes
   */
  private static class ServiceEntry {

    /**
     * The service instance.
     */
    private Service service;

    /**
     * The metadata for the entry.
     */
    private Map<String, Object> metadata;

    /**
     * Construct a new entry.
     *
     * @param service
     *          the service
     * @param metadata
     *          any specialized metadata for the service
     */
    public ServiceEntry(Service service, Map<String, Object> metadata) {
      this.service = service;
      this.metadata = metadata;
    }

    /**
     * Get the service for this entry.
     *
     * @return the service
     */
    public Service getService() {
      return service;
    }

    /**
     * Get the metadata for this entry.
     *
     * @return the metadata
     */
    public Map<String, Object> getMetadata() {
      return metadata;
    }
  }
}
