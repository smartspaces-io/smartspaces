/*
 * Copyright (C) 2017 Keith M. Hughes
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

package io.smartspaces.monitor.expectation.time;

import org.junit.Before
import org.junit.Test
import org.scalatest.junit.JUnitSuite
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import io.smartspaces.logging.ExtendedLog
import io.smartspaces.time.provider.TimeProvider
import org.mockito.Matchers

/**
 * Tests for the standard heartbeat expectation monitor.
 *
 * @author Keith M. Hughes
 */
class HeartbeatExpectationMonitorTest extends JUnitSuite {

  @Mock var log: ExtendedLog = _
  @Mock var timeProvider: TimeProvider = _
  @Mock var listener: HeartbeatExpectationMonitorListener[String] = _

  var monitor: StandardHeartbeatExpectationMonitor[String] = _

  @Before def setup(): Unit = {
    MockitoAnnotations.initMocks(this)

    monitor = new StandardHeartbeatExpectationMonitor(1000, listener, timeProvider, log)
  }

  /**
   * Test a scan happening inside the heartbeat window.
   */
  @Test def scanInsideTimeWindow(): Unit = {
    
    var monitored = "foo"
    var monitoredId = "id"
    Mockito.when(timeProvider.getCurrentTime).thenReturn(1000, 1100)
    
    monitor.addMonitoredWatcher(monitoredId, monitored, 200)
    
    monitor.scan
    
    Mockito.verify(listener, Mockito.never).onHeartbeatLost(Matchers.any[HeartbeatMonitorWatcher[String]], Matchers.any[Long], Matchers.any[Long])
  }

  /**
   * Test a scan happening outside the heartbeat window.
   */
  @Test def scanOutsideTimeWindow(): Unit = {
    
    val monitored = "foo"
    val monitoredId = "id"
    val timeBegin = 1000L
    val timeLost = 1201L
    Mockito.when(timeProvider.getCurrentTime).thenReturn(timeBegin, timeLost)
    
    monitor.addMonitoredWatcher(monitoredId, monitored, 200)
    val watcher = monitor.getMonitoredWatcher(monitoredId).get
    
    monitor.scan
    
    Mockito.verify(listener, Mockito.times(1)).onHeartbeatLost(watcher, timeLost, timeLost - timeBegin)
  }

  /**
   * Test a scan happening outside the heartbeat window twice. Should only be signalled once.
   */
  @Test def scanOutsideTimeWindowTwice(): Unit = {
    
    val monitored = "foo"
    val monitoredId = "id"
    val timeBegin = 1000L
    val timeLost = 1201L
    val timeWindow = 200L
    
    Mockito.when(timeProvider.getCurrentTime).thenReturn(timeBegin, timeLost, timeLost * 2)
    
    monitor.addMonitoredWatcher(monitoredId, monitored, timeWindow)
    val watcher = monitor.getMonitoredWatcher(monitoredId).get
    
    monitor.scan
    monitor.scan
    
    Mockito.verify(listener, Mockito.times(1)).onHeartbeatLost(watcher, timeLost, timeLost - timeBegin)
  }

  /**
   * Test a heartbeat and show no regain message.
   */
  @Test def heartbeatNoRegain(): Unit = {
    
    val monitored = "foo"
    val monitoredId = "id"
    val timeBegin = 1000L
    val timeWindow = 200L
    
    Mockito.when(timeProvider.getCurrentTime).thenReturn(timeBegin, timeBegin + timeWindow/4, timeBegin + timeWindow/2)
    
    monitor.addMonitoredWatcher(monitoredId, monitored, timeWindow)
    
    monitor.updateHeartbeat(monitoredId, timeBegin + timeWindow/4)
    
    Mockito.verify(listener, Mockito.never).onHeartbeatRegained(Matchers.any[HeartbeatMonitorWatcher[String]], Matchers.any[Long])
    
    monitor.updateHeartbeat(monitoredId, timeBegin + timeWindow/2)
    
    Mockito.verify(listener, Mockito.never).onHeartbeatRegained(Matchers.any[HeartbeatMonitorWatcher[String]], Matchers.any[Long])
  }

  /**
   * Test a lost heartbeat and then a regain.
   */
  @Test def heartbeatLostThenRegained(): Unit = {
    
    val monitored = "foo"
    val monitoredId = "id"
    val timeBegin = 1000L
    val timeWindow = 200L
    val timeLost = timeBegin + timeWindow + 1
    Mockito.when(timeProvider.getCurrentTime).thenReturn(timeBegin, timeLost)
    
    monitor.addMonitoredWatcher(monitoredId, monitored, 200)
    val watcher = monitor.getMonitoredWatcher(monitoredId).get
    
    monitor.scan
    
    Mockito.verify(listener, Mockito.never).onHeartbeatRegained(Matchers.any[HeartbeatMonitorWatcher[String]], Matchers.any[Long])
    Mockito.verify(listener, Mockito.times(1)).onHeartbeatLost(watcher, timeLost, timeLost - timeBegin)
    
    val nextHeartbeat = timeLost + timeWindow/2
    monitor.updateHeartbeat(monitoredId, nextHeartbeat)
    
    Mockito.verify(listener, Mockito.times(1)).onHeartbeatRegained(watcher, nextHeartbeat)
    Mockito.verify(listener, Mockito.times(1)).onHeartbeatLost(watcher, timeLost, timeLost - timeBegin)
    
  }
}
