/*
 * Copyright (C) 2015 Keith M. Hughes
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

package io.smartspaces.service.action.internal;

import io.smartspaces.evaluation.StandardExecutionContext;
import io.smartspaces.service.action.Action;
import io.smartspaces.service.action.ActionSource;
import io.smartspaces.service.action.BasicActionReference;
import io.smartspaces.service.action.internal.StandardActionService;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Map;

/**
 * Test the {@link StandardActionService}.
 * 
 * @author Keith M. Hughes
 */
public class StandardActionServiceTest {
  private StandardActionService actionService;

  @Before
  public void setup() {
    actionService = new StandardActionService();
  }

  @Test
  public void testSuccessfulFind() {
    ActionSource source = Mockito.mock(ActionSource.class);

    String actionName = "bar";
    Action action = Mockito.mock(Action.class);
    Mockito.when(source.getAction(actionName)).thenReturn(action);

    String sourceName = "foo";
    actionService.registerActionSource(sourceName, source);

    StandardExecutionContext context = new StandardExecutionContext(null, null);
    actionService.performAction(sourceName, actionName, context);

    Mockito.verify(action, Mockito.times(1)).perform(context);

  }

  @Test
  public void testSuccessfulFindReference() {
    ActionSource source = Mockito.mock(ActionSource.class);

    String actionName = "bar";
    Action action = Mockito.mock(Action.class);
    Mockito.when(source.getAction(actionName)).thenReturn(action);

    String sourceName = "foo";
    actionService.registerActionSource(sourceName, source);

    Map<String, Object> data = new HashMap<>();
    data.put("foo1",  "bar1");
    StandardExecutionContext context = new StandardExecutionContext(null, null);

    BasicActionReference actionReference =
        new BasicActionReference("ref1", "test1", sourceName, null, actionName, data);
    actionService.performActionReference(actionReference, context);

    Mockito.verify(action, Mockito.times(1)).perform(context);
    
    String value = context.getValue("foo1");
    Assert.assertEquals("bar1", value);
  }
}
