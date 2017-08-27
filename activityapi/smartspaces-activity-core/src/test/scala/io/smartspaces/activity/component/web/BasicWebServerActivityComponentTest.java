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

package io.smartspaces.activity.component.web;

import io.smartspaces.activity.SupportedActivity;
import io.smartspaces.activity.component.ActivityComponentContext;
import io.smartspaces.logging.ExtendedLog;
import io.smartspaces.service.web.server.WebServerWebSocketHandler;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Map;

/**
 * Tests for the {@link BasicWebServerActivityComponent}.
 *
 * @author Keith M. Hughes
 */
public class BasicWebServerActivityComponentTest {

  private BasicWebServerActivityComponent activityComponent;
  private WebServerWebSocketHandler<Map<String, Object>> delegate;
  private ActivityComponentContext activityComponentContext;
  private BasicWebServerActivityComponent.MyWebServerWebSocketHandler handler;
  private InOrder activityComponentContextInOrder;
  private SupportedActivity activity;
  private ExtendedLog log;

  @SuppressWarnings("unchecked")
  @Before
  public void setup() {
	delegate = Mockito.mock(WebServerWebSocketHandler.class);
    activityComponentContext = Mockito.mock(ActivityComponentContext.class);

    activityComponent = Mockito.mock(BasicWebServerActivityComponent.class);
    Mockito.when(activityComponent.getComponentContext()).thenReturn(activityComponentContext);

    activity = Mockito.mock(SupportedActivity.class);
    Mockito.when(activityComponentContext.getActivity()).thenReturn(activity);

    log = Mockito.mock(ExtendedLog.class);
    Mockito.when(activity.getLog()).thenReturn(log);

    handler =
        new BasicWebServerActivityComponent.MyWebServerWebSocketHandler(delegate, activityComponent);

    activityComponentContextInOrder = Mockito.inOrder(activityComponentContext);
  }

  /**
   * Test the onConnect of the web socket handler.
   */
  @Test
  public void testMyWebServerWebSocketHandlerOnConnectSuccess() {
    Mockito.when(activityComponentContext.canHandlerRun()).thenReturn(true);

    handler.onConnect();

    Mockito.verify(delegate).onConnect();

    activityComponentContextInOrder.verify(activityComponentContext).enterHandler();
    activityComponentContextInOrder.verify(activityComponentContext).exitHandler();
  }

  /**
   * Test the onConnect failure of the web socket handler.
   */
  @Test
  public void testMyWebServerWebSocketHandlerOnConnectFailure() {
    Mockito.when(activityComponentContext.canHandlerRun()).thenReturn(true);

    Exception e = new RuntimeException();
    Mockito.doThrow(e).when(delegate).onConnect();

    handler.onConnect();

    Mockito.verify(delegate).onConnect();

    activityComponentContextInOrder.verify(activityComponentContext).enterHandler();
    activityComponentContextInOrder.verify(activityComponentContext).exitHandler();

    Mockito.verify(activityComponent, Mockito.times(1)).handleError(Mockito.anyString(),
        Mockito.eq(e));
  }

  /**
   * Test the onConnect of the web socket handler when context won't allow it to
   * run.
   */
  @Test
  public void testMyWebServerWebSocketHandlerOnConnectNoRun() {
    Mockito.when(activityComponentContext.canHandlerRun()).thenReturn(false);

    handler.onConnect();

    Mockito.verify(delegate, Mockito.never()).onConnect();

    Mockito.verify(activityComponentContext, Mockito.never()).enterHandler();
    Mockito.verify(activityComponentContext, Mockito.never()).exitHandler();
  }

  /**
   * Test the onClose of the web socket handler.
   */
  @Test
  public void testMyWebServerWebSocketHandlerOnCloseSuccess() {
    Mockito.when(activityComponentContext.canHandlerRun()).thenReturn(true);

    handler.onClose();

    Mockito.verify(delegate).onClose();

    activityComponentContextInOrder.verify(activityComponentContext).enterHandler();
    activityComponentContextInOrder.verify(activityComponentContext).exitHandler();
  }

  /**
   * Test the onClose failure of the web socket handler.
   */
  @Test
  public void testMyWebServerWebSocketHandlerOnCloseFailure() {
    Mockito.when(activityComponentContext.canHandlerRun()).thenReturn(true);

    Exception e = new RuntimeException();
    Mockito.doThrow(e).when(delegate).onClose();

    handler.onClose();

    Mockito.verify(delegate).onClose();

    activityComponentContextInOrder.verify(activityComponentContext).enterHandler();
    activityComponentContextInOrder.verify(activityComponentContext).exitHandler();

    Mockito.verify(activityComponent, Mockito.times(1)).handleError(Mockito.anyString(),
        Mockito.eq(e));
  }

  /**
   * Test the onClose of the web socket handler when context won't allow it to
   * run.
   */
  @Test
  public void testMyWebServerWebSocketHandlerOnCloseNoRun() {
    Mockito.when(activityComponentContext.canHandlerRun()).thenReturn(false);

    handler.onClose();

    Mockito.verify(delegate, Mockito.never()).onClose();

    Mockito.verify(activityComponentContext, Mockito.never()).enterHandler();
    Mockito.verify(activityComponentContext, Mockito.never()).exitHandler();
  }

  /**
   * Test the onNewMessage of the web socket handler.
   */
  @Test
  public void testMyWebServerWebSocketHandlerOnReceiveSuccess() {
    Mockito.when(activityComponentContext.canHandlerRun()).thenReturn(true);

    Map<String, Object> message = new HashMap<>();

    handler.onNewMessage(message);

    Mockito.verify(delegate).onNewMessage(message);

    activityComponentContextInOrder.verify(activityComponentContext).enterHandler();
    activityComponentContextInOrder.verify(activityComponentContext).exitHandler();
  }

  /**
   * Test the onNewMessage failure of the web socket handler.
   */
  @Test
  public void testMyWebServerWebSocketHandlerOnReceiveFailure() {
    Mockito.when(activityComponentContext.canHandlerRun()).thenReturn(true);

    Map<String, Object> message = new HashMap<>();

    Exception e = new RuntimeException();
    Mockito.doThrow(e).when(delegate).onNewMessage(message);

    handler.onNewMessage(message);

    Mockito.verify(delegate).onNewMessage(message);

    activityComponentContextInOrder.verify(activityComponentContext).enterHandler();
    activityComponentContextInOrder.verify(activityComponentContext).exitHandler();

    Mockito.verify(activityComponent, Mockito.times(1)).handleError(Mockito.anyString(),
        Mockito.eq(e));
  }

  /**
   * Test the onNewMessage of the web socket handler when context won't allow it to
   * run.
   */
  @Test
  public void testMyWebServerWebSocketHandlerOnReceiveNoRun() {
    Mockito.when(activityComponentContext.canHandlerRun()).thenReturn(false);

    Map<String, Object> message = new HashMap<>();

    handler.onNewMessage(message);

    Mockito.verify(delegate, Mockito.never()).onNewMessage(message);

    Mockito.verify(activityComponentContext, Mockito.never()).enterHandler();
    Mockito.verify(activityComponentContext, Mockito.never()).exitHandler();
  }

  /**
   * Test the writeMessage of the web socket handler.
   */
  @Test
  public void testMyWebServerWebSocketHandlerSendSuccess() {
    Mockito.when(activityComponentContext.canHandlerRun()).thenReturn(true);

    Map<String, Object> message = new HashMap<>();

    handler.sendMessage(message);

    Mockito.verify(delegate).sendMessage(message);

    activityComponentContextInOrder.verify(activityComponentContext).enterHandler();
    activityComponentContextInOrder.verify(activityComponentContext).exitHandler();
  }

  /**
   * Test the writeMessage failure of the web socket handler.
   */
  @Test
  public void testMyWebServerWebSocketHandlerSendFailure() {
    Mockito.when(activityComponentContext.canHandlerRun()).thenReturn(true);

    Map<String, Object> message = new HashMap<>();

    Exception e = new RuntimeException();
    Mockito.doThrow(e).when(delegate).sendMessage(message);

    handler.sendMessage(message);

    Mockito.verify(delegate).sendMessage(message);

    activityComponentContextInOrder.verify(activityComponentContext).enterHandler();
    activityComponentContextInOrder.verify(activityComponentContext).exitHandler();

    Mockito.verify(activityComponent, Mockito.times(1)).handleError(Mockito.anyString(),
        Mockito.eq(e));
  }
}
