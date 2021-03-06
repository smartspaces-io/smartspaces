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

package io.smartspaces.system.osgi;

import io.smartspaces.resource.Version;
import io.smartspaces.resource.managed.ManagedResource;
import io.smartspaces.resource.managed.ManagedResources;
import io.smartspaces.resource.managed.StandardManagedResources;
import io.smartspaces.scope.ManagedScope;
import io.smartspaces.scope.StandardManagedScope;
import io.smartspaces.service.Service;
import io.smartspaces.service.ServiceRegistry;
import io.smartspaces.service.SupportedService;
import io.smartspaces.system.SmartSpacesEnvironment;
import io.smartspaces.system.osgi.OsgiServiceTrackerCollection.MyServiceTracker;
import io.smartspaces.system.osgi.OsgiServiceTrackerCollection.OsgiServiceTrackerCollectionListener;
import io.smartspaces.tasks.ManagedTasks;
import io.smartspaces.tasks.StandardManagedTasks;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import java.util.ArrayList;
import java.util.List;

/**
 * A base class for creating OSGi BundleActivator subclasses for Smart Spaces
 * services and other OSGi bundles from Smart Spaces.
 *
 * @author Keith M. Hughes
 */
public abstract class SmartSpacesOsgiBundleActivator
    implements BundleActivator, OsgiServiceTrackerCollectionListener {

  /**
   * All OSGi service registrations from this bundle.
   */
  private final List<ServiceRegistration<?>> osgiServiceRegistrations = new ArrayList<>();

  /**
   * All Smart Spaces services registered by this bundle.
   */
  private final List<Service> registeredSmartSpacesServices = new ArrayList<>();

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
   * A collection of managed tasks.
   */
  private ManagedTasks managedTasks;

  /**
   * A managed scope for the bundle.
   */
  private ManagedScope managedScope;

  /**
   * All service trackers we have.
   */
  private OsgiServiceTrackerCollection serviceTrackerCollection;

  @Override
  public void start(BundleContext context) throws Exception {
    this.bundleContext = context;

    serviceTrackerCollection = new OsgiServiceTrackerCollection(context, this);

    smartspacesEnvironmentTracker =
        serviceTrackerCollection.newMyServiceTracker(SmartSpacesEnvironment.class.getName());

    // Get the registrations from the subclass.
    onStart();

    serviceTrackerCollection.startTracking();
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

    serviceTrackerCollection.stopTracking();
  }

  /**
   * Bundle is shutting down. Do any extra cleanup.
   */
  protected void onStop() {
    // Default is do nothing.
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
    return serviceTrackerCollection.newMyServiceTracker(serviceName);
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
    // May shut the activator down before anything registered with it due to a bundle refresh. So tracker
    // may return null.
    SmartSpacesEnvironment myService = smartspacesEnvironmentTracker.getMyService();
    if (myService != null) {
      ServiceRegistry serviceRegistry = myService.getServiceRegistry();
      for (Service service : registeredSmartSpacesServices) {
        try {
          serviceRegistry.shutdownAndUnregisterService(service);
        } catch (Throwable e) {
          getSmartSpacesEnvironment().getLog().error("Could not shut service down", e);
        }
      }
      registeredSmartSpacesServices.clear();
    }
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
  @Override
  public void handleAllRequiredServicesAvailable() {
    SmartSpacesEnvironment spaceEnvironment = smartspacesEnvironmentTracker.getMyService();
    managedResources = new StandardManagedResources(spaceEnvironment.getLog());

    managedTasks =
        new StandardManagedTasks(spaceEnvironment.getExecutorService(), spaceEnvironment.getLog());

    managedScope = new StandardManagedScope(managedResources, managedTasks, spaceEnvironment.getExecutorService());

    allRequiredServicesAvailable();

    managedScope.startup();
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

      registeredSmartSpacesServices.add(service);
    } catch (Exception e) {
      spaceEnvironment.getLog()
          .formatError(e, "Error while starting up service %s", service.getName());
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
   * Get the service tracker for the Smart Spaces environment.
   *
   * @return the service tracker
   */
  public MyServiceTracker<SmartSpacesEnvironment> getSmartSpacesEnvironmentTracker() {
    return smartspacesEnvironmentTracker;
  }
}
