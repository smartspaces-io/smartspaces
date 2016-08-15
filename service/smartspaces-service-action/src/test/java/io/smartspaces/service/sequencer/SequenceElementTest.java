/*
 * Copyright (C) 2016 Keith M. Hughes
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

package io.smartspaces.service.sequencer;

import com.google.common.collect.Lists;

import io.smartspaces.service.ServiceRegistry;
import io.smartspaces.service.action.ActionReference;
import io.smartspaces.service.action.ActionService;
import io.smartspaces.service.sequencer.ActionSequenceElement;
import io.smartspaces.service.sequencer.GroupSequenceElement;
import io.smartspaces.service.sequencer.RepeatingSequenceElement;
import io.smartspaces.service.sequencer.RunnableSequenceElement;
import io.smartspaces.service.sequencer.SequenceElement;
import io.smartspaces.service.sequencer.SequenceExecutionContext;
import io.smartspaces.service.sequencer.Sequencer;
import io.smartspaces.system.SmartSpacesEnvironment;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A series of tests for various {@link SequenceElement} subclasses.
 * 
 * @author Keith M. Hughes
 */
public class SequenceElementTest {

  @Mock
  Sequencer sequencer;

  @Mock
  SmartSpacesEnvironment spaceEnvironment;

  @Before
  public void setup() {
    MockitoAnnotations.initMocks(this);
  }
  
  @Test
  public void testGroupSequenceElement() {
    final List<String> actual = new ArrayList<>();

    SequenceElement element1 = new SequenceElement() {
      @Override
      public void run(SequenceExecutionContext sequenceExecutionContext) {
        actual.add("a");
      }
    };
    SequenceElement element2 = new SequenceElement() {
      @Override
      public void run(SequenceExecutionContext sequenceExecutionContext) {
        actual.add("b");
      }
    };

    GroupSequenceElement sequence = new GroupSequenceElement(element1, element2);
    SequenceExecutionContext sequenceExecutionContext =
        new SequenceExecutionContext(sequencer, null, spaceEnvironment);
    sequence.run(sequenceExecutionContext);

    Assert.assertEquals(Lists.newArrayList("a", "b"), actual);
  }

  @Test
  public void testRepeatingSequenceElement() {
    final AtomicInteger actual = new AtomicInteger();

    SequenceElement element1 = new SequenceElement() {
      @Override
      public void run(SequenceExecutionContext sequenceExecutionContext) {
        actual.incrementAndGet();
      }
    };

    RepeatingSequenceElement sequence = new RepeatingSequenceElement(10, element1);
    SequenceExecutionContext sequenceExecutionContext =
        new SequenceExecutionContext(sequencer, null, spaceEnvironment);
    sequence.run(sequenceExecutionContext);

    Assert.assertEquals(10, actual.get());
  }

  @Test
  public void testRunnableSequenceElement() {
    Runnable runnable = Mockito.mock(Runnable.class);

    RunnableSequenceElement sequence = new RunnableSequenceElement(runnable);
    SequenceExecutionContext sequenceExecutionContext =
        new SequenceExecutionContext(sequencer, null, spaceEnvironment);
    sequence.run(sequenceExecutionContext);

    Mockito.verify(runnable).run();
  }

  @Test
  public void testActionSequenceElement() {
    ActionService actionService = Mockito.mock(ActionService.class);

    ServiceRegistry serviceRegistry = Mockito.mock(ServiceRegistry.class);
    Mockito.when(serviceRegistry.getRequiredService(ActionService.SERVICE_NAME))
        .thenReturn(actionService);
    
    Mockito.when(spaceEnvironment.getServiceRegistry()).thenReturn(serviceRegistry);

    ActionReference ref = Mockito.mock(ActionReference.class);
    ActionSequenceElement sequence = new ActionSequenceElement(ref);
    SequenceExecutionContext sequenceExecutionContext =
        new SequenceExecutionContext(sequencer, null, spaceEnvironment);
    sequence.run(sequenceExecutionContext);

    Mockito.verify(actionService).performActionReference(ref, sequenceExecutionContext);
  }
}
