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

import java.io.File;
import java.net.InetAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

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

import io.smartspaces.configuration.Configuration;
import io.smartspaces.configuration.FileSystemConfigurationStorageManager;
import io.smartspaces.configuration.SystemConfigurationStorageManager;
import io.smartspaces.evaluation.ExpressionEvaluatorFactory;
import io.smartspaces.evaluation.SimpleExpressionEvaluatorFactory;
import io.smartspaces.resource.managed.ManagedResource;
import io.smartspaces.resource.managed.ManagedResources;
import io.smartspaces.resource.managed.StandardManagedResources;
import io.smartspaces.service.Service;
import io.smartspaces.service.ServiceRegistry;
import io.smartspaces.system.BasicSmartSpacesFilesystem;
import io.smartspaces.system.SmartSpacesEnvironment;
import io.smartspaces.system.SmartSpacesSystemControl;
import io.smartspaces.system.core.configuration.ConfigurationProvider;
import io.smartspaces.system.core.configuration.CoreConfiguration;
import io.smartspaces.system.core.container.ContainerCustomizerProvider;
import io.smartspaces.system.core.logging.LoggingProvider;
import io.smartspaces.system.internal.osgi.OsgiContainerResourceManager;
import io.smartspaces.system.internal.osgi.OsgiSmartSpacesSystemControl;
import io.smartspaces.system.internal.osgi.RosOsgiSmartSpacesEnvironment;
import io.smartspaces.system.resources.ContainerResourceManager;
import io.smartspaces.time.LocalTimeProvider;
import io.smartspaces.time.NtpTimeProvider;
import io.smartspaces.time.TimeProvider;

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
  private RosOsgiSmartSpacesEnvironment spaceEnvironment;

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
  private OsgiSmartSpacesSystemControl systemControl;

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

  @Override
  public void start(BundleContext context) throws Exception {
    bundleContext = context;

    String baseInstallDirProperty =
        bundleContext.getProperty(CoreConfiguration.CONFIGURATION_SMARTSPACES_BASE_INSTALL_DIR);
    File baseInstallDir = new File(baseInstallDirProperty);

    try {
      getCoreServices();

      // Do not call startupResources on this. Various items needed to be
      // started up asap to be used.
      managedResources = new StandardManagedResources(loggingProvider.getLog());

      setupSpaceEnvironment(baseInstallDir);

      createAdditionalResources();

      registerOsgiServices();

      spaceEnvironment.getLog()
          .info(String.format("Base system startup. Smart Spaces Version %s",
              spaceEnvironment.getSystemConfiguration()
                  .getPropertyString(SmartSpacesEnvironment.CONFIGURATION_SMARTSPACES_VERSION)));
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  /**
   * Create any additional resources needed by the container.
   */
  private void createAdditionalResources() {
    containerResourceManager = new OsgiContainerResourceManager(bundleContext,
        bundleContext.getBundle(0).adapt(FrameworkWiring.class), filesystem,
        configurationProvider.getConfigFolder(), spaceEnvironment.getExtendedLog());
    containerResourceManager.startup();
    managedResources.addResource(containerResourceManager);
  }

  @Override
  public void stop(BundleContext context) throws Exception {
    managedResources.shutdownResourcesAndClear();

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
    registerOsgiService(SmartSpacesSystemControl.class.getName(), systemControl);
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

    systemControl = new OsgiSmartSpacesSystemControl(bundleContext);

    executorService = new DefaultScheduledExecutorService();

    filesystem = new BasicSmartSpacesFilesystem(baseInstallDir);
    filesystem.startup();
    managedResources.addResource(filesystem);

    spaceEnvironment = new RosOsgiSmartSpacesEnvironment();
    spaceEnvironment.setExecutorService(executorService);
    spaceEnvironment.setLoggingProvider(loggingProvider);
    spaceEnvironment.setFilesystem(filesystem);
    spaceEnvironment
        .setNetworkType(containerProperties.get(SmartSpacesEnvironment.CONFIGURATION_NETWORK_TYPE));

    setupSystemConfiguration(bundleContext, containerProperties);

    timeProvider = getTimeProvider(containerProperties, loggingProvider.getLog());
    spaceEnvironment.setTimeProvider(timeProvider);
    timeProvider.startup();
    managedResources.addResource(timeProvider);

    setupRosEnvironment(
        systemConfigurationStorageManager.getSystemConfiguration().getCollapsedMap(),
        loggingProvider.getLog());

    // TODO(keith): Get the value property in a central place.
    spaceEnvironment.setValue("environment.ros", rosEnvironment);

    // Potentially request the container to permit file control.
    spaceEnvironment.getSystemConfiguration().setValue(
        SmartSpacesEnvironment.CONFIGURATION_CONTAINER_FILE_CONTROLLABLE,
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
    String provider = containerProperties.get(SmartSpacesEnvironment.CONFIGURATION_PROVIDER_TIME);
    if (provider == null) {
      provider = SmartSpacesEnvironment.CONFIGURATION_VALUE_PROVIDER_TIME_DEFAULT;
    }

    if (SmartSpacesEnvironment.CONFIGURATION_VALUE_PROVIDER_TIME_NTP.equals(provider)) {
      String host =
          containerProperties.get(SmartSpacesEnvironment.CONFIGURATION_PROVIDER_TIME_NTP_URL);
      if (host != null) {
        InetAddress ntpAddress = InetAddressFactory.newFromHostString(host);
        // TODO(keith): Make sure got valid address. Also, move copy of
        // factory class into IS.
        return new NtpTimeProvider(ntpAddress, NTP_UPDATE_PERIOD_SECONDS, TimeUnit.SECONDS,
            executorService, log);
      } else {
        log.warn(String.format(
            "Could not find host for NTP time provider. No value for configuration %s",
            SmartSpacesEnvironment.CONFIGURATION_PROVIDER_TIME_NTP_URL));

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
    rosEnvironment.setMaster(SmartSpacesEnvironment.CONFIGURATION_CONTAINER_TYPE_MASTER
        .equals(containerProperties.get(SmartSpacesEnvironment.CONFIGURATION_CONTAINER_TYPE)));
    rosEnvironment
        .setNetworkType(containerProperties.get(SmartSpacesEnvironment.CONFIGURATION_NETWORK_TYPE));

    for (Entry<String, String> entry : containerProperties.entrySet()) {
      rosEnvironment.setProperty(entry.getKey(), entry.getValue());
    }

    configureRosFromsmartspaces(containerProperties);

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
  private void configureRosFromsmartspaces(Map<String, String> containerProperties) {
    rosEnvironment.setProperty(RosEnvironment.CONFIGURATION_ROS_NODE_NAME,
        RosEnvironment.ROS_NAME_SEPARATOR
            + containerProperties.get(SmartSpacesEnvironment.CONFIGURATION_HOSTID));
    rosEnvironment.setProperty(RosEnvironment.CONFIGURATION_ROS_NETWORK_TYPE,
        spaceEnvironment.getNetworkType());
    rosEnvironment.setProperty(RosEnvironment.CONFIGURATION_ROS_CONTAINER_TYPE,
        spaceEnvironment.getSystemConfiguration()
            .getRequiredPropertyString(SmartSpacesEnvironment.CONFIGURATION_CONTAINER_TYPE));
    rosEnvironment.setProperty(RosEnvironment.CONFIGURATION_ROS_HOST,
        spaceEnvironment.getSystemConfiguration()
            .getRequiredPropertyString(SmartSpacesEnvironment.CONFIGURATION_HOSTNAME));

    // This call is so that the ROS URI gets evaluated.
    rosEnvironment.setProperty(RosEnvironment.CONFIGURATION_ROS_MASTER_URI, spaceEnvironment
        .getSystemConfiguration().getPropertyString(RosEnvironment.CONFIGURATION_ROS_MASTER_URI));
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

    systemConfiguration.setValues(containerProperties);

    systemConfiguration.setValue(SmartSpacesEnvironment.CONFIGURATION_SMARTSPACES_VERSION,
        bundleContext.getProperty(CoreConfiguration.CONFIGURATION_SMARTSPACES_VERSION));

    String hostAddress = convertHostnameToAddress(
        containerProperties.get(SmartSpacesEnvironment.CONFIGURATION_HOSTNAME));
    systemConfiguration.setValue(SmartSpacesEnvironment.CONFIGURATION_HOST_ADDRESS, hostAddress);

    spaceEnvironment.setSystemConfiguration(systemConfiguration);
  }

  /**
   * Convert the given hostname to an IP address.
   *
   * @param hostname
   *          hostname to convert
   *
   * @return host IP address
   */
  private String convertHostnameToAddress(String hostname) {
    try {
      InetAddress address = InetAddress.getByName(hostname);
      return address.getHostAddress();
    } catch (Exception e) {
      spaceEnvironment.getLog().error("Could not convert hostname to IP address", e);
      return UNKNOWN_HOST_ADDRESS;
    }
  }
}
