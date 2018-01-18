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

import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * A standard implementation of the {@link ServiceRegistry}.
 *
 * @author Keith M. Hughes
 */
public class StandardServiceRegistry implements ServiceRegistry {

  /**
   * A fair read/write lock.
   */
  private ReadWriteLock lock = new ReentrantReadWriteLock(true);

  /**
   * All services in the registry.
   */
  private Map<String, ServiceEntry> services = new HashMap<>();

  /**
   * The mapping of names to service notifications.
   */
  private Map<String, List<ServiceNotification<?>>> serviceNotifications = new HashMap<>();

  /**
   * The list of all services awaiting dependencies.
   */
  private List<Service> servicesAwaitingDependencies = new ArrayList<>();

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
  public StandardServiceRegistry(SmartSpacesEnvironment spaceEnvironment) {
    this.spaceEnvironment = spaceEnvironment;
  }

  @Override
  public void registerService(Service service) {

    registerService(service, false);
  }

  @Override
  public void startupAndRegisterService(Service service) {
    if (SupportedService.class.isAssignableFrom(service.getClass())) {
      ((SupportedService) service).setSpaceEnvironment(spaceEnvironment);
    }

    registerService(service, true);
  }

  @Override
  public void addServiceNotificationListener(String serviceName,
      ServiceNotification<?> notificationListener) {
    ServiceEntry entry = null;

    lock.writeLock().lock();
    try {
      entry = services.get(serviceName);
      if (entry == null) {
        List<ServiceNotification<?>> notifications = serviceNotifications.get(serviceName);
        if (notifications == null) {
          notifications = new ArrayList<>();
          serviceNotifications.put(serviceName, notifications);
        }

        notifications.add(notificationListener);
      }
    } finally {
      lock.writeLock().unlock();
    }

    if (entry != null) {
      notifyAboutService(entry.getService(), notificationListener);
    }
  }

  @Override
  public synchronized void unregisterService(Service service) {
    spaceEnvironment.getLog()
        .info(String.format("Service unregistering with name %s", service.getName()));
    services.remove(service.getName());
  }

  @Override
  public void shutdownAndUnregisterService(Service service) {
    lock.writeLock().lock();
    try {
      unregisterService(service);
    } finally {
      lock.writeLock().unlock();
    }

    service.shutdown();
  }

  private void registerService(Service service, boolean startup) {
    spaceEnvironment.getLog().formatInfo("Registering service with name %s and dependencies %s", 
        service.getName(), service.getDependencies());
    List<ServiceNotifications> notifications = new ArrayList<>();

    lock.writeLock().lock();
    try {
      if (startup) {
        if (hasAllDependenciesMet(service)) {
          service.startup();

          addService(service, notifications);

          checkOldForDependencies(notifications);
        } else {
          servicesAwaitingDependencies.add(service);
        }
      }
    } finally {
      lock.writeLock().unlock();
    }

    for (ServiceNotifications notification : notifications) {
      notification.sendNotifications();
    }
  }

  private void addService(Service service, List<ServiceNotifications> notifications) {
    // TODO(keith): Support multiple services with the same name of the
    // service.
    String serviceName = service.getName();
    if (!services.containsKey(serviceName)) {
      services.put(service.getName(), new ServiceEntry(service, service.getMetadata()));
      spaceEnvironment.getLog().formatInfo("Service registered with name %s", serviceName);

      List<ServiceNotification<?>> notify = serviceNotifications.remove(serviceName);
      if (notify != null) {
        notifications.add(new ServiceNotifications(service, notify));
      }
    } else {
      spaceEnvironment.getLog().formatWarn(
          "Service already registered with name %s. New registration dropped", serviceName);
    }
  }


  private void checkOldForDependencies(List<ServiceNotifications> notifications) {
    // Dumb algorithm but will try for now.
    
    List<Service> servicesToStart = new ArrayList<>();
    List<Service> servicesToNotStart = new ArrayList<>();
    
    for (Service service : servicesAwaitingDependencies) {
      if (hasAllDependenciesMet(service)) {
        servicesToStart.add(service);
      } else {
        servicesToNotStart.add(service);
      }
    }
    
    while (!servicesToStart.isEmpty()) {
      
      for (Service service : servicesToStart) {
        try {
          service.startup();
          
          addService(service, notifications);
        } catch (Throwable t) {
          spaceEnvironment.getLog().formatError(t,  "Could not start up service %s", service.getName());
        }
      }
      
      servicesAwaitingDependencies = servicesToNotStart;
      servicesToStart.clear();
      servicesToNotStart = new ArrayList<>();
      
      for (Service service : servicesAwaitingDependencies) {
        if (hasAllDependenciesMet(service)) {
          servicesToStart.add(service);
        } else {
          servicesToNotStart.add(service);
        }
      }
    }
  }

  private boolean hasAllDependenciesMet(Service service) {
    for (ServiceDescription dependency : service.getDependencies()) {
      if (!services.containsKey(dependency.name())) {
        return false;
      }
    }

    return true;
  }

  /**
   * Notify that a service is available to a notification listener.
   * 
   * @param service
   *          the service
   * @param notification
   *          the listener
   */
  private void notifyAboutService(Service service, ServiceNotification<?> notification) {
    try {
      notification.notifyServiceAvailable(service);
    } catch (Throwable e) {
      spaceEnvironment.getLog().formatWarn(e, "Service with name %s notification failed",
          service.getName());
    }
  }

  @Override
  public synchronized Set<ServiceDescription> getAllServiceDescriptions() {
    Set<ServiceDescription> allDescriptions = new HashSet<>();

    lock.readLock().lock();
    try {
      // TODO(keith): Cache these as services are registered and unregistered.
      for (ServiceEntry entry : services.values()) {
        allDescriptions.add(entry.getService().getServiceDescription());
      }
    } finally {
      lock.readLock().unlock();
    }

    return allDescriptions;
  }

  @Override
  public <T extends Service> T getService(String name) {
    lock.readLock().lock();
    try {
      ServiceEntry entry = services.get(name);
      if (entry != null) {
        @SuppressWarnings("unchecked")
        T service = (T) entry.getService();

        return service;
      } else {
        return null;
      }
    } finally {
      lock.readLock().unlock();
    }
  }

  @Override
  public <T extends Service> T getRequiredService(String name) throws SmartSpacesException {
    T service = getService(name);
    if (service != null) {
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

  private class ServiceNotifications {
    final List<ServiceNotification<?>> notifications;

    final Service service;

    ServiceNotifications(Service service, List<ServiceNotification<?>> notifications) {
      this.service = service;
      this.notifications = notifications;
    }

    void sendNotifications() {
      if (notifications != null) {
        for (ServiceNotification<?> notification : notifications) {
          notifyAboutService(service, notification);
        }
      }
    }
  }
}
