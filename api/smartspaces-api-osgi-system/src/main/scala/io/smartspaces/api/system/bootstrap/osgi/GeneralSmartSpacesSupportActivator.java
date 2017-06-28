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

package io.smartspaces.api.system.bootstrap.osgi;

import io.smartspaces.api.osgi.service.OsgiServiceTrackerCollection;
import io.smartspaces.api.osgi.service.OsgiServiceTrackerCollection.MyServiceTracker;
import io.smartspaces.api.osgi.service.OsgiServiceTrackerCollection.OsgiServiceTrackerCollectionListener;
import io.smartspaces.api.system.internal.osgi.OsgiSmartSpacesEnvironment;
import io.smartspaces.configuration.Configuration;
import io.smartspaces.configuration.FileSystemConfigurationStorageManager;
import io.smartspaces.configuration.SimpleConfiguration;
import io.smartspaces.evaluation.SimpleExpressionEvaluatorFactory;
import io.smartspaces.logging.ExtendedLog;
import io.smartspaces.logging.StandardExtendedLog;
import io.smartspaces.resource.managed.ManagedResources;
import io.smartspaces.resource.managed.StandardManagedResources;
import io.smartspaces.scope.ManagedScope;
import io.smartspaces.scope.StandardManagedScope;
import io.smartspaces.system.BasicSmartSpacesFilesystem;
import io.smartspaces.system.SmartSpacesEnvironment;
import io.smartspaces.system.core.configuration.ConfigurationProvider;
import io.smartspaces.system.core.logging.LoggingProvider;
import io.smartspaces.tasks.ManagedTasks;
import io.smartspaces.tasks.StandardManagedTasks;
import io.smartspaces.time.provider.LocalTimeProvider;
import io.smartspaces.time.provider.NtpTimeProvider;
import io.smartspaces.time.provider.TimeProvider;
import io.smartspaces.util.concurrency.DefaultScheduledExecutorService;
import io.smartspaces.util.net.InetAddressFactory;

import org.apache.commons.logging.Log;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;

import java.io.File;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Activate general services needed by a container wanting to use SmartSpaces
 * infrastructure.
 *
 * @author Keith M. Hughes
 */
public class GeneralSmartSpacesSupportActivator
    implements BundleActivator, OsgiServiceTrackerCollectionListener {

  /**
   * The bundle context for this activator.
   */
  private BundleContext bundleContext;

  /**
   * The service tracker collection.
   */
  private OsgiServiceTrackerCollection serviceTrackerCollection;
  
  /**
   * The container log.
   */
  private ExtendedLog containerLog;

  /**
   * Thread pool for everyone to use.
   */
  private ScheduledExecutorService executorService;

  /**
   * Smart Spaces environment for the container.
   */
  private OsgiSmartSpacesEnvironment spaceEnvironment;

  /**
   * The Smart Spaces-wide file system.
   */
  private BasicSmartSpacesFilesystem filesystem;

  /**
   * The storage manager for system configurations.
   */
  private FileSystemConfigurationStorageManager systemConfigurationStorageManager;

  /**
   * Factory for expression evaluators.
   */
  private SimpleExpressionEvaluatorFactory expressionEvaluatorFactory;

  /**
   * The platform logging provider.
   */
  private LoggingProvider loggingProvider;

  /**
   * The platform configuration provider.
   */
  private ConfigurationProvider configurationProvider;

  /**
   * The platform time provider.
   */
  private TimeProvider timeProvider;

  /**
   * Managed resources for the bundle. This simplifies automatic startup and
   * shutdown of resources the bundle may need to provide.
   */
  private ManagedResources managedResources;

  /**
   * All service registrations put into place.
   */
  private final List<ServiceRegistration<?>> serviceRegistrations = new ArrayList<>();

  /**
   * Host address to use if address lookup fails.
   */
  private static final String UNKNOWN_HOST_ADDRESS = "unknown";

  /**
   * Update period for NTP.
   */
  private static final long NTP_UPDATE_PERIOD_SECONDS = 10L;

  /**
   * The managed scope for the entire container.
   */
  private ManagedScope containerManagedScope;

  @Override
  public void start(BundleContext bundleContext) throws Exception {
    this.bundleContext = bundleContext;

    serviceTrackerCollection = new OsgiServiceTrackerCollection(bundleContext, this);

    onStart();

    serviceTrackerCollection.startTracking();
  }

  @Override
  public void stop(BundleContext context) throws Exception {
    serviceTrackerCollection.stopTracking();

    containerManagedScope.shutdown();

    // Remove all OSGi service registrations.
    for (ServiceRegistration<?> registration : serviceRegistrations) {
      registration.unregister();
    }
  }

  /**
   * The bundle is starting up. Do any preprocessing before things really get
   * going.
   */
  public void onStart() {
    // Default is to do nothing
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
   * All required services are now available.
   */
  public void allRequiredServicesAvailable() {
    // Default is to do nothing
  }

  @Override
  public final void handleAllRequiredServicesAvailable() {

    allRequiredServicesAvailable();

    String baseInstallDirProperty = "."; // bundleContext
    // .getProperty(CoreConfiguration.CONFIGURATION_NAME_SMARTSPACES_BASE_INSTALL_DIR);
    File baseInstallDir = new File(baseInstallDirProperty);

    try {
      getCoreServices();
      containerLog = new StandardExtendedLog("container", loggingProvider.getLog());

      managedResources = new StandardManagedResources(containerLog);

      ManagedTasks managedTasks =
          new StandardManagedTasks(executorService, containerLog);

      containerManagedScope = new StandardManagedScope(managedResources, managedTasks);
      containerManagedScope.startup();

      setupSpaceEnvironment(baseInstallDir);

      createAdditionalResources();

      registerOsgiServices();
    } catch (Exception e) {
      containerLog.error("Could not start up smartspaces system", e);
    }
  }

  /**
   * Create any additional resources needed by the container.
   * 
   * <p>
   * This method is meant to be overridden
   */
  protected void createAdditionalResources() {
    // Default is to add nothing.
  }

  /**
   * Get all core services needed.
   *
   * <p>
   * These services should have been provided by the OSGi container bootstrap
   * and so will be immediately available. They will never go away since they
   * are only destroyed when bundle 0 goes, which means the entire container is
   * being shut down.
   *
   * @throws Exception
   *           something bad happened
   */
  private void getCoreServices() throws Exception {
    ServiceReference<LoggingProvider> loggingProviderServiceReference =
        bundleContext.getServiceReference(LoggingProvider.class);
    loggingProvider = bundleContext.getService(loggingProviderServiceReference);

    ServiceReference<ConfigurationProvider> configurationProviderServiceReference =
        bundleContext.getServiceReference(ConfigurationProvider.class);
    configurationProvider = bundleContext.getService(configurationProviderServiceReference);
  }

  /**
   * Register all services which need to be made available to others.
   */
  private void registerOsgiServices() {
    registerOsgiService(SmartSpacesEnvironment.class.getName(), spaceEnvironment);
  }

  /**
   * Register an OSGi service.
   *
   * @param name
   *          name of the service
   * @param service
   *          the service object
   */
  protected void registerOsgiService(String name, Object service) {
    serviceRegistrations.add(bundleContext.registerService(name, service, null));
  }

  /**
   * Set up the {@link SmartSpacesEnvironment} everyone should use.
   *
   * @param baseInstallDir
   *          the base directory where Smart Spaces is installed
   */
  private void setupSpaceEnvironment(File baseInstallDir) {
    Map<String, String> containerProperties = configurationProvider.getInitialConfiguration();

    executorService = new DefaultScheduledExecutorService();

    // filesystem = new BasicSmartSpacesFilesystem(baseInstallDir);
    // filesystem.startup();
    // managedResources.addResource(filesystem);

    spaceEnvironment = new OsgiSmartSpacesEnvironment();
    spaceEnvironment.setExecutorService(executorService);
    spaceEnvironment.setLoggingProvider(loggingProvider, containerLog);
    spaceEnvironment.setFilesystem(filesystem);
    spaceEnvironment.setContainerManagedScope(containerManagedScope);

    setupSystemConfiguration(bundleContext, containerProperties);

    timeProvider = getTimeProvider(containerProperties);
    spaceEnvironment.setTimeProvider(timeProvider);
    timeProvider.startup();
    managedResources.addResource(timeProvider);
  }

  /**
   * Get the time provider to use.
   *
   * @param containerProperties
   *          properties to use for configuration
   *
   * @return the time provider to use
   */
  public TimeProvider getTimeProvider(Map<String, String> containerProperties) {
    String provider =
        containerProperties.get(SmartSpacesEnvironment.CONFIGURATION_NAME_PROVIDER_TIME);
    if (provider == null) {
      provider = SmartSpacesEnvironment.CONFIGURATION_VALUE_PROVIDER_TIME_DEFAULT;
    }

    if (SmartSpacesEnvironment.CONFIGURATION_VALUE_PROVIDER_TIME_NTP.equals(provider)) {
      String host =
          containerProperties.get(SmartSpacesEnvironment.CONFIGURATION_NAME_PROVIDER_TIME_NTP_URL);
      if (host != null) {
        InetAddress ntpAddress = InetAddressFactory.newFromHostString(host);
        return new NtpTimeProvider(ntpAddress, NTP_UPDATE_PERIOD_SECONDS, TimeUnit.SECONDS,
            executorService, containerLog);
      } else {
        containerLog.warn(String.format(
            "Could not find host for NTP time provider. No value for configuration %s",
            SmartSpacesEnvironment.CONFIGURATION_NAME_PROVIDER_TIME_NTP_URL));

        return new LocalTimeProvider();
      }
    } else {
      return new LocalTimeProvider();
    }
  }

  /**
   * Set up the system configuration.
   *
   * @param context
   *          bundle context to use
   * @param containerProperties
   *          properties for the container
   */
  private void setupSystemConfiguration(BundleContext context,
      Map<String, String> containerProperties) {
    expressionEvaluatorFactory = new SimpleExpressionEvaluatorFactory();

    // FileSystemConfigurationStorageManager
    // fileSystemConfigurationStorageManager =
    // new FileSystemConfigurationStorageManager();
    // fileSystemConfigurationStorageManager.setLog(spaceEnvironment.getLog());
    // fileSystemConfigurationStorageManager.setExpressionEvaluatorFactory(expressionEvaluatorFactory);
    // fileSystemConfigurationStorageManager.setsmartspacesFilesystem(filesystem);
    // fileSystemConfigurationStorageManager.setConfigFolder(configurationProvider.getConfigFolder());
    //
    // systemConfigurationStorageManager =
    // fileSystemConfigurationStorageManager;
    // systemConfigurationStorageManager.startup();
    // managedResources.addResource(systemConfigurationStorageManager);
    //
    // Configuration systemConfiguration =
    // systemConfigurationStorageManager.getSystemConfiguration();
    //
    Configuration systemConfiguration = new SimpleConfiguration(expressionEvaluatorFactory.newEvaluator());
    systemConfiguration.setProperties(containerProperties);
    //
    // systemConfiguration.setProperty(SmartSpacesEnvironment.CONFIGURATION_NAME_SMARTSPACES_VERSION,
    // bundleContext.getProperty(CoreConfiguration.CONFIGURATION_NAME_SMARTSPACES_VERSION));
    //
    // String hostAddress = getHostAddress(systemConfiguration);
    // if (hostAddress != null) {
    // log.info(String.format("Using container host address %s", hostAddress));
    // systemConfiguration.setProperty(SmartSpacesEnvironment.CONFIGURATION_NAME_HOST_ADDRESS,
    // hostAddress);
    // } else {
    // log.warn("Could not determine container host address.");
    // }
    //
    // systemConfiguration.setProperty(
    // SmartSpacesEnvironment.CONFIGURATION_NAME_SYSTEM_FILESYSTEM_DIR_INSTALL,
    // filesystem.getInstallDirectory().getAbsolutePath());
    // systemConfiguration.setProperty(
    // SmartSpacesEnvironment.CONFIGURATION_NAME_SYSTEM_FILESYSTEM_DIR_DATA,
    // filesystem.getDataDirectory().getAbsolutePath());
    // systemConfiguration.setProperty(
    // SmartSpacesEnvironment.CONFIGURATION_NAME_SYSTEM_FILESYSTEM_DIR_TMP,
    // filesystem.getTempDirectory().getAbsolutePath());

    spaceEnvironment.setSystemConfiguration(systemConfiguration);
  }

  /**
   * Get the IP address for the system.
   *
   * @param systemConfiguration
   *          The system configuration
   *
   * @return host IP address
   */
  private String getHostAddress(Configuration systemConfiguration) {
    try {
      String hostname = systemConfiguration
          .getPropertyString(SmartSpacesEnvironment.CONFIGURATION_NAME_HOST_NAME);
      if (hostname != null) {
        InetAddress address = InetAddress.getByName(hostname);
        return address.getHostAddress();
      }

      String hostInterface = systemConfiguration
          .getPropertyString(SmartSpacesEnvironment.CONFIGURATION_NAME_HOST_INTERFACE);
      if (hostInterface != null) {
        spaceEnvironment.getLog().formatInfo("Using network interface with name %s", hostInterface);
        NetworkInterface networkInterface = NetworkInterface.getByName(hostInterface);
        if (networkInterface != null) {
          for (InetAddress inetAddress : Collections.list(networkInterface.getInetAddresses())) {
            if (inetAddress instanceof Inet4Address) {
              return inetAddress.getHostAddress();
            }
          }
        } else {
          spaceEnvironment.getLog().formatWarn(
              "No network interface with name %s from configuration %s", hostInterface,
              SmartSpacesEnvironment.CONFIGURATION_NAME_HOST_INTERFACE);

        }
      }

      // See if a single network interface. If so, we will use it.
      List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
      if (interfaces.size() == 1) {
        for (InetAddress inetAddress : Collections.list(interfaces.get(0).getInetAddresses())) {
          if (inetAddress instanceof Inet4Address) {
            return inetAddress.getHostAddress();
          }
        }
      }

      return null;
    } catch (Exception e) {
      spaceEnvironment.getLog().error("Could not obtain IP address", e);
      return UNKNOWN_HOST_ADDRESS;
    }
  }
}
