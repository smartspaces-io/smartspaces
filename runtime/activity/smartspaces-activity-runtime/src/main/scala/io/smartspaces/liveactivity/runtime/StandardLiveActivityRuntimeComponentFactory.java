/*
 * Copyright (C) 2016 Keith M. Hughes
 * Copyright (C) 2015 Google Inc.
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

package io.smartspaces.liveactivity.runtime;

import io.smartspaces.activity.binary.NativeActivityRunnerFactory;
import io.smartspaces.activity.binary.SimpleNativeActivityRunnerFactory;
import io.smartspaces.activity.component.ActivityComponentFactory;
import io.smartspaces.activity.component.SimpleActivityComponentFactory;
import io.smartspaces.activity.component.binary.BasicNativeActivityComponent;
import io.smartspaces.activity.component.binary.NativeActivityComponent;
import io.smartspaces.activity.component.ros.BasicRosActivityComponent;
import io.smartspaces.activity.component.ros.RosActivityComponent;
import io.smartspaces.activity.component.route.BasicMessageRouterActivityComponent;
import io.smartspaces.activity.component.route.MessageRouterActivityComponent;
import io.smartspaces.activity.component.web.BasicWebBrowserActivityComponent;
import io.smartspaces.activity.component.web.BasicWebServerActivityComponent;
import io.smartspaces.activity.component.web.WebBrowserActivityComponent;
import io.smartspaces.activity.component.web.WebServerActivityComponent;
import io.smartspaces.liveactivity.runtime.activity.wrapper.internal.bridge.topic.TopicBridgeActivityWrapperFactory;
import io.smartspaces.liveactivity.runtime.activity.wrapper.internal.osnative.NativeActivityWrapperFactory;
import io.smartspaces.liveactivity.runtime.activity.wrapper.internal.smartspaces.SmartSpacesNativeActivityWrapperFactory;
import io.smartspaces.liveactivity.runtime.activity.wrapper.internal.smartspaces.StandardLiveActivityBundleLoader;
import io.smartspaces.liveactivity.runtime.activity.wrapper.internal.web.WebActivityWrapperFactory;
import io.smartspaces.service.ServiceRegistry;
import io.smartspaces.service.event.observable.EventObservableService;
import io.smartspaces.service.event.observable.StandardEventObservableService;
import io.smartspaces.service.web.client.WebSocketClientService;
import io.smartspaces.service.web.client.internal.netty.NettyWebSocketClientService;
import io.smartspaces.service.web.server.WebServerService;
import io.smartspaces.service.web.server.internal.netty.NettyWebServerService;
import io.smartspaces.system.SmartSpacesEnvironment;
import io.smartspaces.system.resources.ContainerResourceManager;

/**
 * A factory for creating various components for a live activity runtime.
 *
 * <p>
 * This is to ensure consistency between the various types of runtimes and
 * controllers.
 *
 * @author Keith M. Hughes
 */
public class StandardLiveActivityRuntimeComponentFactory
    implements LiveActivityRuntimeComponentFactory {

  /**
   * Run under a space environment.
   */
  private SmartSpacesEnvironment spaceEnvironment;

  /**
   * The container resource manager.
   */
  private ContainerResourceManager containerResourceManager;

  /**
   * The SS service for web servers.
   */
  private WebServerService webServerService;

  /**
   * The SS service for web socket clients.
   */
  private WebSocketClientService webSocketClientService;

  /**
   * Construct a new factory.
   *
   * @param spaceEnvironment
   *          the space environment to use
   * @param containerResourceManager
   *          the container resource manager to use
   */
  public StandardLiveActivityRuntimeComponentFactory(SmartSpacesEnvironment spaceEnvironment,
      ContainerResourceManager containerResourceManager) {
    this.spaceEnvironment = spaceEnvironment;
    this.containerResourceManager = containerResourceManager;
  }

  /**
   * Create a new live activity runner factory.
   *
   * @return a new live activity runner factory
   */
  @Override
  public LiveActivityRunnerFactory newLiveActivityRunnerFactory() {
    LiveActivityRunnerFactory liveActivityRunnerFactory =
        new StandardLiveActivityRunnerFactory(spaceEnvironment);
    liveActivityRunnerFactory.registerActivityWrapperFactory(new NativeActivityWrapperFactory());
    liveActivityRunnerFactory.registerActivityWrapperFactory(new WebActivityWrapperFactory());
    liveActivityRunnerFactory
        .registerActivityWrapperFactory(new TopicBridgeActivityWrapperFactory());
    liveActivityRunnerFactory
        .registerActivityWrapperFactory(new SmartSpacesNativeActivityWrapperFactory(
            new StandardLiveActivityBundleLoader(containerResourceManager)));

    return liveActivityRunnerFactory;
  }

  /**
   * Create a new native activity runner factory.
   *
   * @return a new native activity runner factory
   */
  @Override
  public NativeActivityRunnerFactory newNativeActivityRunnerFactory() {
    return new SimpleNativeActivityRunnerFactory(spaceEnvironment);
  }

  /**
   * Create a new activity component factory.
   *
   * @return a new activity component factory
   */
  @Override
  public ActivityComponentFactory newActivityComponentFactory() {
    SimpleActivityComponentFactory factory = new SimpleActivityComponentFactory();

    factory.register(NativeActivityComponent.COMPONENT_NAME, BasicNativeActivityComponent.class);
    factory.register(RosActivityComponent.COMPONENT_NAME, BasicRosActivityComponent.class);
    factory.register(MessageRouterActivityComponent.COMPONENT_NAME,
        BasicMessageRouterActivityComponent.class);
    factory.register(WebBrowserActivityComponent.COMPONENT_NAME,
        BasicWebBrowserActivityComponent.class);
    factory.register(WebServerActivityComponent.COMPONENT_NAME,
        BasicWebServerActivityComponent.class);

    return factory;
  }

  @Override
  public void registerCoreServices(ServiceRegistry serviceRegistry) {
    webServerService = new NettyWebServerService();
    webServerService.setSpaceEnvironment(spaceEnvironment);
    serviceRegistry.registerService(webServerService);
    webServerService.startup();

    webSocketClientService = new NettyWebSocketClientService();
    webSocketClientService.setSpaceEnvironment(spaceEnvironment);
    serviceRegistry.registerService(webSocketClientService);
    webSocketClientService.startup();
  }

  @Override
  public void unregisterCoreServices(ServiceRegistry serviceRegistry) {
    serviceRegistry.unregisterService(webServerService);
    webServerService.shutdown();

    serviceRegistry.unregisterService(webSocketClientService);
    webSocketClientService.shutdown();
  }
}
