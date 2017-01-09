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

package io.smartspaces.system.bootstrap.osgi;

import io.smartspaces.configuration.Configuration;
import io.smartspaces.configuration.FileSystemConfigurationStorageManager;
import io.smartspaces.configuration.SystemConfigurationStorageManager;
import io.smartspaces.evaluation.ExpressionEvaluatorFactory;
import io.smartspaces.evaluation.SimpleExpressionEvaluatorFactory;
import io.smartspaces.resource.managed.ManagedResource;
import io.smartspaces.resource.managed.ManagedResources;
import io.smartspaces.resource.managed.StandardManagedResources;
import io.smartspaces.scope.ManagedScope;
import io.smartspaces.scope.StandardManagedScope;
import io.smartspaces.service.Service;
import io.smartspaces.service.ServiceRegistry;
import io.smartspaces.service.event.observable.StandardEventObservableService;
import io.smartspaces.system.BasicSmartSpacesFilesystem;
import io.smartspaces.system.SmartSpacesEnvironment;
import io.smartspaces.system.core.configuration.ConfigurationProvider;
import io.smartspaces.system.core.configuration.CoreConfiguration;
import io.smartspaces.system.core.container.ContainerCustomizerProvider;
import io.smartspaces.system.core.container.SmartSpacesSystemControl;
import io.smartspaces.system.core.logging.LoggingProvider;
import io.smartspaces.system.internal.osgi.OsgiContainerResourceManager;
import io.smartspaces.system.internal.osgi.OsgiSmartSpacesEnvironment;
import io.smartspaces.system.resources.ContainerResourceManager;
import io.smartspaces.tasks.ManagedTasks;
import io.smartspaces.tasks.StandardManagedTasks;
import io.smartspaces.time.provider.LocalTimeProvider;
import io.smartspaces.time.provider.NtpTimeProvider;
import io.smartspaces.time.provider.TimeProvider;

import org.apache.commons.logging.Log;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.framework.wiring.FrameworkWiring;
import org.ros.address.InetAddressFactory;
import org.ros.concurrent.DefaultScheduledExecutorService;
import org.ros.log.RosLogFactory;
import org.ros.master.uri.MasterUriProvider;
import org.ros.master.uri.StaticMasterUriProvider;
import org.ros.master.uri.SwitchableMasterUriProvider;
import org.ros.osgi.common.RosEnvironment;
import org.ros.osgi.common.SimpleRosEnvironment;

import java.io.File;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Activate general services needed by a Smart Spaces container.
 *
 * @author Keith M. Hughes
 */
public class GeneralSmartSpacesSupportActivator implements BundleActivator {

  /**
   * The bundle context for this activator.
   */
  private BundleContext bundleContext;

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
   * ROS environment for the container.
   */
  private SimpleRosEnvironment rosEnvironment;

  /**
   * The storage manager for system configurations.
   */
  private FileSystemConfigurationStorageManager systemConfigurationStorageManager;

  /**
   * Factory for expression evaluators.
   */
  private SimpleExpressionEvaluatorFactory expressionEvaluatorFactory;

  /**
   * The system control for Smart Spaces.
   */
  private SmartSpacesSystemControl systemControl;

  /**
   * The ROS Master URI provider in use.
   */
  private SwitchableMasterUriProvider masterUriProvider;

  /**
   * The platform logging provider.
   */
  private LoggingProvider loggingProvider;

  /**
   * The platform configuration provider.
   */
  private ConfigurationProvider configurationProvider;

  /**
   * The platform container customizer provider.
   *
   * <p>
   * Can be {@code null} if none is provided.
   */
  private ContainerCustomizerProvider containerCustomizerProvider;

  /**
   * The platform time provider.
   */
  private TimeProvider timeProvider;

  /**
   * The container resource manager.
   */
  private OsgiContainerResourceManager containerResourceManager;

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

  private ManagedScope containerManagedScope;

  @Override
  public void start(BundleContext context) throws Exception {
    bundleContext = context;

    String baseInstallDirProperty = bundleContext
        .getProperty(CoreConfiguration.CONFIGURATION_NAME_SMARTSPACES_BASE_INSTALL_DIR);
    File baseInstallDir = new File(baseInstallDirProperty);

    try {
      getCoreServices();

      managedResources = new StandardManagedResources(loggingProvider.getLog());

      ManagedTasks managedTasks =
          new StandardManagedTasks(executorService, loggingProvider.getLog());

      containerManagedScope = new StandardManagedScope(managedResources, managedTasks);
      containerManagedScope.startup();

      setupSpaceEnvironment(baseInstallDir);

      createAdditionalResources();

      registerOsgiServices();

      spaceEnvironment.getLog()
          .info(String.format("Base system startup. Smart Spaces Version %s",
              spaceEnvironment.getSystemConfiguration().getPropertyString(
                  SmartSpacesEnvironment.CONFIGURATION_NAME_SMARTSPACES_VERSION)));
    } catch (Exception e) {
      spaceEnvironment.getLog().error("Could not start up smartspaces system", e);
    }
  }

  /**
   * Create any additional resources needed by the container.
   */
  private void createAdditionalResources() {
    containerResourceManager = new OsgiContainerResourceManager(bundleContext,
        bundleContext.getBundle(0).adapt(FrameworkWiring.class), filesystem,
        configurationProvider.getConfigFolder(), spaceEnvironment.getLog());
    containerResourceManager.startup();
    managedResources.addResource(containerResourceManager);
  }

  @Override
  public void stop(BundleContext context) throws Exception {
    containerManagedScope.shutdown();

    // Remove all OSGi service registrations.
    for (ServiceRegistration<?> registration : serviceRegistrations) {
      registration.unregister();
    }
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

    ServiceReference<ContainerCustomizerProvider> containerCustomizerProviderServiceReference =
        bundleContext.getServiceReference(ContainerCustomizerProvider.class);
    containerCustomizerProvider =
        bundleContext.getService(containerCustomizerProviderServiceReference);

    ServiceReference<SmartSpacesSystemControl> systemControlReference =
        bundleContext.getServiceReference(SmartSpacesSystemControl.class);
    systemControl = bundleContext.getService(systemControlReference);
  }

  /**
   * Register all services which need to be made available to others.
   */
  private void registerOsgiServices() {
    registerOsgiService(ContainerResourceManager.class.getName(), containerResourceManager);
    registerOsgiService(ExpressionEvaluatorFactory.class.getName(), expressionEvaluatorFactory);
    registerOsgiService(SystemConfigurationStorageManager.class.getName(),
        systemConfigurationStorageManager);
    registerOsgiService(SmartSpacesEnvironment.class.getName(), spaceEnvironment);
    registerOsgiService(RosEnvironment.class.getName(), rosEnvironment);
    registerOsgiService(SwitchableMasterUriProvider.class.getName(), masterUriProvider);
  }

  /**
   * Register an OSGi service.
   *
   * @param name
   *          name of the service
   * @param service
   *          the service object
   */
  private void registerOsgiService(String name, Object service) {
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

    filesystem = new BasicSmartSpacesFilesystem(baseInstallDir);
    filesystem.startup();
    managedResources.addResource(filesystem);

    spaceEnvironment = new OsgiSmartSpacesEnvironment();
    spaceEnvironment.setExecutorService(executorService);
    spaceEnvironment.setLoggingProvider(loggingProvider);
    spaceEnvironment.setFilesystem(filesystem);
    spaceEnvironment.setNetworkType(
        containerProperties.get(SmartSpacesEnvironment.CONFIGURATION_NAME_NETWORK_TYPE));
    spaceEnvironment.setContainerManagedScope(containerManagedScope);

    setupSystemConfiguration(bundleContext, containerProperties);

    timeProvider = getTimeProvider(containerProperties, loggingProvider.getLog());
    spaceEnvironment.setTimeProvider(timeProvider);
    timeProvider.startup();
    managedResources.addResource(timeProvider);

    StandardEventObservableService eventService = new StandardEventObservableService();
    managedResources.addResource(eventService);
    spaceEnvironment.getServiceRegistry().registerService(eventService);

    setupRosEnvironment(
        systemConfigurationStorageManager.getSystemConfiguration().getCollapsedMap(),
        loggingProvider.getLog());

    // TODO(keith): Get the value property in a central place.
    spaceEnvironment.setValue("environment.ros", rosEnvironment);

    // Potentially request the container to permit file control.
    spaceEnvironment.getSystemConfiguration().setProperty(
        SmartSpacesEnvironment.CONFIGURATION_NAME_CONTAINER_FILE_CONTROLLABLE,
        Boolean.toString(containerCustomizerProvider.isFileControllable()));

    customizeContainer();
  }

  /**
   * Get the time provider to use.
   *
   * @param containerProperties
   *          properties to use for configuration
   * @param log
   *          logger for messages
   *
   * @return the time provider to use
   */
  public TimeProvider getTimeProvider(Map<String, String> containerProperties, Log log) {
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
        // TODO(keith): Make sure got valid address. Also, move copy of
        // factory class into IS.
        return new NtpTimeProvider(ntpAddress, NTP_UPDATE_PERIOD_SECONDS, TimeUnit.SECONDS,
            executorService, log);
      } else {
        log.warn(String.format(
            "Could not find host for NTP time provider. No value for configuration %s",
            SmartSpacesEnvironment.CONFIGURATION_NAME_PROVIDER_TIME_NTP_URL));

        return new LocalTimeProvider();
      }
    } else {
      return new LocalTimeProvider();
    }
  }

  /**
   * Add any customization to the service from services and other objects
   * provided by the container.
   */
  public void customizeContainer() {
    if (containerCustomizerProvider != null) {
      ServiceRegistry serviceRegistry = spaceEnvironment.getServiceRegistry();
      for (Entry<String, Object> entry : containerCustomizerProvider.getServices().entrySet()) {
        serviceRegistry.registerService((Service) entry.getValue());
      }
    }
  }

  /**
   * Set up the full ROS environment.
   *
   * @param containerProperties
   *          properties for configuration
   * @param log
   *          logger to use
   */
  private void setupRosEnvironment(Map<String, String> containerProperties, Log log) {
    RosLogFactory.setLog(log);
    rosEnvironment = new SimpleRosEnvironment();
    rosEnvironment.setExecutorService(executorService);
    rosEnvironment.setLog(spaceEnvironment.getLog());
    rosEnvironment.setMaster(SmartSpacesEnvironment.CONFIGURATION_VALUE_CONTAINER_TYPE_MASTER
        .equals(containerProperties.get(SmartSpacesEnvironment.CONFIGURATION_NAME_CONTAINER_TYPE)));
    rosEnvironment.setNetworkType(
        containerProperties.get(SmartSpacesEnvironment.CONFIGURATION_NAME_NETWORK_TYPE));

    for (Entry<String, String> entry : containerProperties.entrySet()) {
      rosEnvironment.setProperty(entry.getKey(), entry.getValue());
    }

    configureRosFromSmartspaces(containerProperties);

    // Want to start Smart Spaces with no master URI unless there was
    // one in the config properties.
    rosEnvironment.setMasterUri(null);
    rosEnvironment.startup();
    managedResources.addResource(new ManagedResource() {

      @Override
      public void startup() {
        // Won't be calling startup
      }

      @Override
      public void shutdown() {
        rosEnvironment.shutdown();
      }
    });

    MasterUriProvider baseProvider = null;
    URI masterUri = rosEnvironment.getMasterUri();
    if (masterUri != null) {
      log.info(String.format("Have initial ROS Master URI %s", masterUri));
      baseProvider = new StaticMasterUriProvider(masterUri);
    }

    masterUriProvider = new SwitchableMasterUriProvider(baseProvider);
    rosEnvironment.setMasterUriProvider(masterUriProvider);
  }

  /**
   * Configure the ROS environment from the smart spaces properties.
   *
   * @param containerProperties
   *          the properties from the container configuration
   */
  private void configureRosFromSmartspaces(Map<String, String> containerProperties) {
    rosEnvironment.setProperty(RosEnvironment.CONFIGURATION_NAME_ROS_NODE_NAME,
        RosEnvironment.ROS_NAME_SEPARATOR
            + containerProperties.get(SmartSpacesEnvironment.CONFIGURATION_NAME_HOSTID));
    rosEnvironment.setProperty(RosEnvironment.CONFIGURATION_NAME_ROS_NETWORK_TYPE,
        spaceEnvironment.getNetworkType());
    rosEnvironment.setProperty(RosEnvironment.CONFIGURATION_NAME_ROS_CONTAINER_TYPE,
        spaceEnvironment.getSystemConfiguration()
            .getRequiredPropertyString(SmartSpacesEnvironment.CONFIGURATION_NAME_CONTAINER_TYPE));
    rosEnvironment.setProperty(RosEnvironment.CONFIGURATION_NAME_ROS_HOST,
        spaceEnvironment.getSystemConfiguration()
            .getPropertyString(SmartSpacesEnvironment.CONFIGURATION_NAME_HOST_NAME));

    // This call is so that the ROS URI gets evaluated.
    rosEnvironment.setProperty(RosEnvironment.CONFIGURATION_NAME_ROS_MASTER_URI, spaceEnvironment
        .getSystemConfiguration().getPropertyString(RosEnvironment.CONFIGURATION_NAME_ROS_MASTER_URI));
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

    FileSystemConfigurationStorageManager fileSystemConfigurationStorageManager =
        new FileSystemConfigurationStorageManager();
    fileSystemConfigurationStorageManager.setLog(spaceEnvironment.getLog());
    fileSystemConfigurationStorageManager.setExpressionEvaluatorFactory(expressionEvaluatorFactory);
    fileSystemConfigurationStorageManager.setsmartspacesFilesystem(filesystem);
    fileSystemConfigurationStorageManager.setConfigFolder(configurationProvider.getConfigFolder());

    systemConfigurationStorageManager = fileSystemConfigurationStorageManager;
    systemConfigurationStorageManager.startup();
    managedResources.addResource(systemConfigurationStorageManager);

    Configuration systemConfiguration = systemConfigurationStorageManager.getSystemConfiguration();

    systemConfiguration.setProperties(containerProperties);

    systemConfiguration.setProperty(SmartSpacesEnvironment.CONFIGURATION_NAME_SMARTSPACES_VERSION,
        bundleContext.getProperty(CoreConfiguration.CONFIGURATION_NAME_SMARTSPACES_VERSION));

    String hostAddress = getHostAddress(systemConfiguration);
    if (hostAddress != null) {
      systemConfiguration.setProperty(SmartSpacesEnvironment.CONFIGURATION_NAME_HOST_ADDRESS,
          hostAddress);
    }

    systemConfiguration.setProperty(
        SmartSpacesEnvironment.CONFIGURATION_NAME_SYSTEM_FILESYSTEM_DIR_INSTALL,
        filesystem.getInstallDirectory().getAbsolutePath());
    systemConfiguration.setProperty(
        SmartSpacesEnvironment.CONFIGURATION_NAME_SYSTEM_FILESYSTEM_DIR_DATA,
        filesystem.getDataDirectory().getAbsolutePath());
    systemConfiguration.setProperty(
        SmartSpacesEnvironment.CONFIGURATION_NAME_SYSTEM_FILESYSTEM_DIR_TMP,
        filesystem.getTempDirectory().getAbsolutePath());

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
