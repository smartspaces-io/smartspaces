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

package io.smartspaces.osgi.service;

import io.smartspaces.resource.Version;
import io.smartspaces.resource.managed.ManagedResource;
import io.smartspaces.resource.managed.ManagedResources;
import io.smartspaces.resource.managed.StandardManagedResources;
import io.smartspaces.scope.ManagedScope;
import io.smartspaces.scope.StandardManagedScope;
import io.smartspaces.service.Service;
import io.smartspaces.service.ServiceRegistry;
import io.smartspaces.system.SmartSpacesEnvironment;
import io.smartspaces.tasks.ManagedTasks;
import io.smartspaces.tasks.StandardManagedTasks;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A base class for creating OSGi BundleActivator subclasses for Smart Spaces
 * services and other OSGi bundles from Smart Spaces.
 *
 * @author Keith M. Hughes
 */
public abstract class SmartSpacesServiceOsgiBundleActivator implements BundleActivator {

  /**
   * All OSGi service registrations from this bundle.
   */
  private final List<ServiceRegistration<?>> osgiServiceRegistrations = new ArrayList<>();

  /**
   * All services registered by this bundle.
   */
  private final List<Service> registeredServices = new ArrayList<>();

  /**
   * OSGi service tracker for the smart spaces environment.
   */
  private MyServiceTracker<SmartSpacesEnvironment> smartspacesEnvironmentTracker;

  /**
   * OSGi bundle context for this bundle.
   */
  private BundleContext bundleContext;

  /**
   * A collection of managed resources.
   */
  private ManagedResources managedResources;

  /**
   * A managed scope for the bundle.
   */
  private ManagedScope managedScope;

  /**
   * All service trackers we have.
   */
  private final Map<String, MyServiceTracker<?>> serviceTrackers =
      new HashMap<String, MyServiceTracker<?>>();

  /**
   * Object to give lock for putting this bundle's services together.
   */
  private final Object serviceLock = new Object();

  @Override
  public void start(BundleContext context) throws Exception {
    this.bundleContext = context;

    smartspacesEnvironmentTracker = newMyServiceTracker(SmartSpacesEnvironment.class.getName());

    // Get the registrations from the subclass.
    onStart();

    // Open all the trackers.
    for (MyServiceTracker<?> tracker : serviceTrackers.values()) {
      tracker.open();
    }
  }

  /**
   * The bundle is starting. Add any requests for services.
   */
  protected void onStart() {
    // Default is to do nothing.
  }

  @Override
  public void stop(BundleContext context) throws Exception {
    onStop();

    unregisterOsgiServices();
    unregisterSmartSpacesServices();
    if (managedScope != null) {
      managedScope.shutdown();
    }

    // Close all the trackers.
    for (MyServiceTracker<?> tracker : serviceTrackers.values()) {
      tracker.close();
    }
    serviceTrackers.clear();
  }

  /**
   * Bundle is shutting down. Do any extra cleanup.
   */
  protected void onStop() {
    // Default is do nothing.
  }

  /**
   * Unregister all OSGi-registered services.
   */
  private void unregisterOsgiServices() {
    for (ServiceRegistration<?> service : osgiServiceRegistrations) {
      service.unregister();
    }
    osgiServiceRegistrations.clear();
  }

  /**
   * Unregister and shutdown all services registered with smart spaces.
   */
  private void unregisterSmartSpacesServices() {
    ServiceRegistry serviceRegistry =
        smartspacesEnvironmentTracker.getMyService().getServiceRegistry();
    for (Service service : registeredServices) {
      try {
        serviceRegistry.shutdownAndUnregisterService(service);
      } catch (Throwable e) {
        getSmartSpacesEnvironment().getLog().error("Could not shut service down", e);
      }
    }
    registeredServices.clear();
  }

  /**
   * Register an smartspaces service as a new OSGi service.
   *
   * @param name
   *          name for the OSGi service
   * @param service
   *          the Smart Spaces service
   */
  public void registerOsgiService(String name, Service service) {
    osgiServiceRegistrations.add(bundleContext.registerService(name, service, null));
  }

  /**
   * Get the managed scope for the bundle.
   * 
   * @return the managed scope
   */
  public ManagedScope getBundleManagedScope() {
    return managedScope;
  }

  /**
   * Register a generic OSGi framework service.
   *
   * @param name
   *          name for the OSGi service
   * @param service
   *          the service to be registered
   */
  public void registerOsgiFrameworkService(String name, Object service) {
    osgiServiceRegistrations.add(bundleContext.registerService(name, service, null));
  }

  /**
   * Got another reference from a dependency.
   */
  protected void gotAnotherReference() {
    synchronized (serviceLock) {
      // If missing any of our needed services, punt.
      for (MyServiceTracker<?> tracker : serviceTrackers.values()) {
        if (tracker.getMyService() == null) {
          return;
        }
      }

      SmartSpacesEnvironment spaceEnvironment = smartspacesEnvironmentTracker.getMyService();
      managedResources = new StandardManagedResources(spaceEnvironment.getLog());

      ManagedTasks managedTasks = new StandardManagedTasks(spaceEnvironment.getExecutorService(),
          spaceEnvironment.getLog());

      managedScope = new StandardManagedScope(managedResources, managedTasks);

      allRequiredServicesAvailable();

      managedScope.startup();
    }
  }

  /**
   * All required services are available.
   */
  protected abstract void allRequiredServicesAvailable();

  /**
   * Register a new service with Smart Spaces.
   * 
   * <p>
   * The service will be injected with the space environment and then will be
   * started if a {@link SupportedService}.
   *
   * @param service
   *          the service to be registered
   */
  public void registerNewSmartSpacesService(Service service) {
    SmartSpacesEnvironment spaceEnvironment = smartspacesEnvironmentTracker.getMyService();
    try {
      spaceEnvironment.getServiceRegistry().startupAndRegisterService(service);

      registeredServices.add(service);
    } catch (Exception e) {
      spaceEnvironment.getLog().formatError(e, "Error while starting up service %s",
          service.getName());
    }
  }

  /**
   * Add in a managed resource.
   *
   * @param resource
   *          the managed resource to add
   */
  public void addManagedResource(ManagedResource resource) {
    managedResources.addResource(resource);
  }

  /**
   * Get the version of the bundle.
   *
   * @return the bundle version
   */
  public String getBundleVersion() {
    return bundleContext.getBundle().getVersion().toString();
  }

  /**
   * Get the version of the bundle.
   *
   * @return the version of the bundle
   */
  public Version getBundleVersionAsVersion() {
    return Version.parseVersion(getBundleVersion());
  }

  /**
   * Create a new service tracker.
   *
   * @param serviceName
   *          name of the service class
   * @param <T>
   *          class being tracked by the service tracker
   *
   * @return the service tracker
   */
  protected <T> MyServiceTracker<T> newMyServiceTracker(String serviceName) {
    MyServiceTracker<T> tracker = new MyServiceTracker<T>(bundleContext, serviceName);

    serviceTrackers.put(serviceName, tracker);

    return tracker;
  }

  /**
   * Get the bundle context for the bundle.
   *
   * @return the bundle context
   */
  public BundleContext getBundleContext() {
    return bundleContext;
  }

  /**
   * Get the Smart Spaces environment.
   *
   * @return the environment
   */
  public SmartSpacesEnvironment getSmartSpacesEnvironment() {
    return smartspacesEnvironmentTracker.getMyService();
  }

  /**
   * An OSGi service tracking class.
   *
   * @param <T>
   *          the class of the service being tracked
   *
   * @author Keith M. Hughes
   */
  public final class MyServiceTracker<T> extends ServiceTracker {

    /**
     * The reference for the service object being waited for.
     */
    private final AtomicReference<T> serviceReference = new AtomicReference<T>();

    /**
     * Construct a service tracker.
     *
     * @param context
     *          bundle context the tracker is running under
     * @param serviceName
     *          the name of the service
     */
    public MyServiceTracker(BundleContext context, String serviceName) {
      super(context, serviceName, null);
    }

    @Override
    public Object addingService(ServiceReference reference) {
      @SuppressWarnings("unchecked")
      T service = (T) super.addingService(reference);

      if (serviceReference.compareAndSet(null, service)) {
        gotAnotherReference();
      }

      return service;
    }

    /**
     * Get the service needed.
     *
     * @return the service, or {@code null} if it hasn't been obtained yet.
     */
    public T getMyService() {
      return serviceReference.get();
    }
  }
}
