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

package io.smartspaces.activity.component.route;

import io.smartspaces.activity.behavior.comm.ros.RosActivityBehavior;
import io.smartspaces.activity.component.ActivityComponentContext;
import io.smartspaces.activity.component.comm.route.BasicMessageRouterActivityComponent;
import io.smartspaces.activity.execution.ActivityExecutionContext;
import io.smartspaces.configuration.Configuration;
import io.smartspaces.configuration.SimpleConfiguration;
import io.smartspaces.evaluation.SimpleExpressionEvaluator;
import io.smartspaces.logging.ExtendedLog;
import io.smartspaces.messaging.route.RouteMessageHandler;
import io.smartspaces.system.SmartSpacesEnvironment;
import io.smartspaces.time.provider.SettableTimeProvider;
import io.smartspaces.time.provider.TimeProvider;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

/**
 * Tests for {@link BasicMessageRouterActivityComponent}.
 *
 * @author Keith M. Hughes
 */
public class BasicMessageRouterActivityComponentTest {

  private BasicMessageRouterActivityComponent component;

  private RouteMessageHandler messageHandler;
  private ActivityComponentContext activityComponentContext;
  private Configuration configuration;
  private RosActivityBehavior activity;
  private SmartSpacesEnvironment spaceEnvironment;
  private TimeProvider timeProvider;

  private InOrder activityComponentContextInOrder;

  private ExtendedLog log;

  private ActivityExecutionContext executionContext;

  @Before
  public void setup() {
    messageHandler = Mockito.mock(RouteMessageHandler.class);

    activityComponentContext = Mockito.mock(ActivityComponentContext.class);
    activityComponentContextInOrder = Mockito.inOrder(activityComponentContext);

    activity = Mockito.mock(RosActivityBehavior.class);
    Mockito.when(activityComponentContext.getActivity()).thenReturn(activity);

    spaceEnvironment = Mockito.mock(SmartSpacesEnvironment.class);
    Mockito.when(activity.getSpaceEnvironment()).thenReturn(spaceEnvironment);

    timeProvider = new SettableTimeProvider();
    Mockito.when(spaceEnvironment.getTimeProvider()).thenReturn(timeProvider);

    executionContext = Mockito.mock(ActivityExecutionContext.class);
    Mockito.when(activity.getExecutionContext()).thenReturn(executionContext);

    log = Mockito.mock(ExtendedLog.class);
    Mockito.when(activity.getLog()).thenReturn(log);

    configuration = SimpleConfiguration.newConfiguration();

    component = new BasicMessageRouterActivityComponent();
    component.setDefaultRoutableInputMessageHandler(messageHandler);
  }

  /**
   * Test that the message handler is appropriately called.
   */
  @Test
  public void testHandlerSuccess() {
    configuration.setProperty("space.activity.routes.inputs", "foo");
    configuration.setProperty("space.activity.route.input.foo", "bar");

    component.setComponentContext(activityComponentContext);
    component.configureComponent(configuration);

    Mockito.when(activityComponentContext.canHandlerRun()).thenReturn(true);

    // String channelId = "foo";
    // GenericMessage message = Mockito.mock
    // component.handleNewIncomingMessage(channelId, message);
    //
    // Mockito.verify(messageListener).onNewRoutableInputMessage(channelId,
    // message);
    //
    // activityComponentContextInOrder.verify(activityComponentContext).enterHandler();
    // activityComponentContextInOrder.verify(activityComponentContext).exitHandler();
  }

  /**
   * The message handler fails.
   */
  @Test
  public void testHandlerFailure() {
    configuration.setProperty("space.activity.routes.inputs", "foo");
    configuration.setProperty("space.activity.route.input.foo", "bar");

    component.setComponentContext(activityComponentContext);
    component.configureComponent(configuration);

    Mockito.when(activityComponentContext.canHandlerRun()).thenReturn(true);

    String channelId = "foo";
    String message = "bar";

    // Exception e = new RuntimeException();
    // Mockito.doThrow(e).when(messageListener).onNewRoutableInputMessage(channelId,
    // message);
    //
    // component.handleNewIncomingMessage(channelId, message);
    //
    // Mockito.verify(messageListener).onNewRoutableInputMessage(channelId,
    // message);
    //
    // activityComponentContextInOrder.verify(activityComponentContext).enterHandler();
    // activityComponentContextInOrder.verify(activityComponentContext).exitHandler();
    //
    // Mockito.verify(log, Mockito.times(1)).error(Mockito.anyString(),
    // Mockito.eq(e));
  }

  @Test
  public void testHandlerNoRun() {
    configuration.setProperty("space.activity.routes.inputs", "foo");
    configuration.setProperty("space.activity.route.input.foo", "bar");

    component.setComponentContext(activityComponentContext);
    component.configureComponent(configuration);

    Mockito.when(activityComponentContext.canHandlerRun()).thenReturn(false);

    // String channelId = "foo";
    // String message = "bar";
    // component.handleNewIncomingMessage(channelId, message);
    //
    // Mockito.verify(messageListener, Mockito.never())
    // .onNewRoutableInputMessage(channelId, message);
    //
    // Mockito.verify(activityComponentContext, Mockito.never()).enterHandler();
    // Mockito.verify(activityComponentContext, Mockito.never()).exitHandler();
  }

}
