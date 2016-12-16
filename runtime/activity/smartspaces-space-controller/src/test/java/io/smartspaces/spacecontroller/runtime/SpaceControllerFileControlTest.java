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

package io.smartspaces.spacecontroller.runtime;

import io.smartspaces.system.SmartSpacesEnvironment;
import io.smartspaces.system.core.container.SmartSpacesSystemControl;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Tests for the {@link SpaceControllerFileControl} class.
 *
 * @author Keith M. Hughes
 */
public class SpaceControllerFileControlTest {

  private SmartSpacesEnvironment spaceEnvironment;

  private SmartSpacesSystemControl spaceSystemControl;

  private SpaceControllerControl spaceControllerControl;

  private SpaceControllerFileControl fileControl;

  @Before
  public void setup() {
    spaceEnvironment = Mockito.mock(SmartSpacesEnvironment.class);
    spaceSystemControl = Mockito.mock(SmartSpacesSystemControl.class);
    spaceControllerControl = Mockito.mock(SpaceControllerControl.class);

    fileControl =
        new SpaceControllerFileControl(spaceControllerControl, spaceSystemControl, spaceEnvironment);
  }

  /**
   * Make sure shutdown is called on control if a shutdown command is received.
   */
  @Test
  public void testShutdownCall() {
    fileControl.handleCommand(SpaceControllerFileControl.COMMAND_SHUTDOWN);

    Mockito.verify(spaceSystemControl, Mockito.times(1)).shutdown();
  }

  /**
   * Make sure soft restart is called on control if a soft restart command is received.
   */
  @Test
  public void testSoftRestartCall() {
    fileControl.handleCommand(SpaceControllerFileControl.COMMAND_RESTART_SOFT);

    Mockito.verify(spaceSystemControl, Mockito.times(1)).softRestart();
  }

  /**
   * Make sure hard restart is called on control if a hard restart command is received.
   */
  @Test
  public void testHardRestartCall() {
    fileControl.handleCommand(SpaceControllerFileControl.COMMAND_RESTART_HARD);

    Mockito.verify(spaceSystemControl, Mockito.times(1)).hardRestart();
  }
}
