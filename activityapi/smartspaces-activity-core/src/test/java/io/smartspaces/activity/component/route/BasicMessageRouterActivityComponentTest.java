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

import org.apache.commons.logging.Log;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import io.smartspaces.activity.component.ActivityComponentContext;
import io.smartspaces.activity.component.route.BasicMessageRouterActivityComponent;
import io.smartspaces.activity.execution.ActivityExecutionContext;
import io.smartspaces.activity.ros.RosActivity;
import io.smartspaces.configuration.Configuration;
import io.smartspaces.configuration.SimpleConfiguration;
import io.smartspaces.evaluation.SimpleExpressionEvaluator;
import io.smartspaces.messaging.route.RoutableInputMessageListener;
import io.smartspaces.system.SmartSpacesEnvironment;
import io.smartspaces.time.SettableTimeProvider;
import io.smartspaces.time.TimeProvider;

/**
 * Tests for {@link BasicMessageRouterActivityComponent}.
 *
 * @author Keith M. Hughes
 */
public class BasicMessageRouterActivityComponentTest {

  private BasicMessageRouterActivityComponent component;

  private RoutableInputMessageListener messageListener;
  private ActivityComponentContext activityComponentContext;
  private Configuration configuration;
  private RosActivity activity;
  private SmartSpacesEnvironment spaceEnvironment;
  private TimeProvider timeProvider;

  private InOrder activityComponentContextInOrder;

  private Log log;

  private ActivityExecutionContext executionContext;

  @Before
  public void setup() {
    messageListener = Mockito.mock(RoutableInputMessageListener.class);

    activityComponentContext = Mockito.mock(ActivityComponentContext.class);
    activityComponentContextInOrder = Mockito.inOrder(activityComponentContext);

    activity = Mockito.mock(RosActivity.class);
    Mockito.when(activityComponentContext.getActivity()).thenReturn(activity);

    spaceEnvironment = Mockito.mock(SmartSpacesEnvironment.class);
    Mockito.when(activity.getSpaceEnvironment()).thenReturn(spaceEnvironment);

    timeProvider = new SettableTimeProvider();
    Mockito.when(spaceEnvironment.getTimeProvider()).thenReturn(timeProvider);

    executionContext = Mockito.mock(ActivityExecutionContext.class);
    Mockito.when(activity.getExecutionContext()).thenReturn(executionContext);

    log = Mockito.mock(Log.class);
    Mockito.when(activity.getLog()).thenReturn(log);

    configuration = new SimpleConfiguration(new SimpleExpressionEvaluator());

    component = new BasicMessageRouterActivityComponent();
    component.setRoutableInputMessageListener(messageListener);
  }

  /**
   * Test that the message handler is appropriately called.
   */
  @Test
  public void testHandlerSuccess() {
    configuration.setValue("space.activity.routes.inputs", "foo");
    configuration.setValue("space.activity.route.input.foo", "bar");

    component.setComponentContext(activityComponentContext);
    component.configureComponent(configuration);

    Mockito.when(activityComponentContext.canHandlerRun()).thenReturn(true);

    // String channelName = "foo";
    // GenericMessage message = Mockito.mock
    // component.handleNewIncomingMessage(channelName, message);
    //
    // Mockito.verify(messageListener).onNewRoutableInputMessage(channelName,
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
    configuration.setValue("space.activity.routes.inputs", "foo");
    configuration.setValue("space.activity.route.input.foo", "bar");

    component.setComponentContext(activityComponentContext);
    component.configureComponent(configuration);

    Mockito.when(activityComponentContext.canHandlerRun()).thenReturn(true);

    String channelName = "foo";
    String message = "bar";

    // Exception e = new RuntimeException();
    // Mockito.doThrow(e).when(messageListener).onNewRoutableInputMessage(channelName,
    // message);
    //
    // component.handleNewIncomingMessage(channelName, message);
    //
    // Mockito.verify(messageListener).onNewRoutableInputMessage(channelName,
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
    configuration.setValue("space.activity.routes.inputs", "foo");
    configuration.setValue("space.activity.route.input.foo", "bar");

    component.setComponentContext(activityComponentContext);
    component.configureComponent(configuration);

    Mockito.when(activityComponentContext.canHandlerRun()).thenReturn(false);

    // String channelName = "foo";
    // String message = "bar";
    // component.handleNewIncomingMessage(channelName, message);
    //
    // Mockito.verify(messageListener, Mockito.never())
    // .onNewRoutableInputMessage(channelName, message);
    //
    // Mockito.verify(activityComponentContext, Mockito.never()).enterHandler();
    // Mockito.verify(activityComponentContext, Mockito.never()).exitHandler();
  }

}
