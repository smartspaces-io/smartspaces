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

package io.smartspaces.monitor.expectation.time

import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.scalatest.junit.JUnitSuite

/**
 * A set of unit tests for the standard heartbeat monitorable mixin.
 * 
 * @author Keith M. Hughes
 */
class StandardHeartbeatMonitorableTest extends JUnitSuite {

  val modelCreationTime = 123456l
  
  /**
   * Test to see if offline happens when there has been no update time.
   */
  @Test def testOfflineFromInitialUpdateTimeout(): Unit = {
    val timeoutTime = 1000l

    val model = new TestObject(None, Option(timeoutTime), modelCreationTime)

    //The model starts offline until proven online
    Assert.assertFalse(model.online)
        
    Assert.assertEquals(0, model.offlineEventCount)
    Assert.assertEquals(0, model.onlineEventCount)

    // Model didn't change from short change
    val transition1 = model.checkIfOfflineTransition(modelCreationTime + timeoutTime / 2)
    Assert.assertFalse(transition1)
    Assert.assertFalse(model.online)
    
    Assert.assertEquals(0, model.offlineEventCount)
    Assert.assertEquals(0, model.onlineEventCount)

    // Now trigger it from no signal from model start.
    val offlineTime = modelCreationTime + timeoutTime + 1
    var transition2 = model.checkIfOfflineTransition(offlineTime)
    Assert.assertTrue(transition2)
    Assert.assertFalse(model.online)
    
    Assert.assertEquals(1, model.offlineEventCount)
    Assert.assertEquals(0, model.onlineEventCount)
  }

  /**
   * Test to see if offline happens when there has been no update time.
   */
  @Test def testOfflineFromOnlineUpdate(): Unit = {
    val lastUpdate = 30000
    val timeoutTime = 1000l

    val model = new TestObject(None, Option(timeoutTime), modelCreationTime)

    // Set the model online and last update time
    model.setOnline(true)
    model.setLastUpdateTime(lastUpdate)
    
    Assert.assertEquals(0, model.offlineEventCount)
    Assert.assertEquals(0, model.onlineEventCount)

    // Model didn't change from short change
    val transition1 = model.checkIfOfflineTransition(lastUpdate + timeoutTime / 2)
    Assert.assertFalse(transition1)
    Assert.assertTrue(model.online)
    
    Assert.assertEquals(0, model.offlineEventCount)
    Assert.assertEquals(0, model.onlineEventCount)

    // Now trigger it
    var offlineTime = lastUpdate + timeoutTime + 1
    var transition2 = model.checkIfOfflineTransition(offlineTime)
    Assert.assertTrue(transition2)
    Assert.assertFalse(model.online)
    
    Assert.assertEquals(1, model.offlineEventCount)
    Assert.assertEquals(0, model.onlineEventCount)
  }

  /**
   * Test to see if offline happens when there has been no update time.
   */
  @Test def testOfflineFromInitialHeartbeatTimeout(): Unit = {
    val timeoutTime = 1000l

    val model = new TestObject(Option(timeoutTime), None, modelCreationTime)

    //The model starts offline until proven online
    Assert.assertFalse(model.online)
    
    Assert.assertEquals(0, model.offlineEventCount)
    Assert.assertEquals(0, model.onlineEventCount)

    // Model didn't change from short change
    val transition1 = model.checkIfOfflineTransition(modelCreationTime + timeoutTime / 2)
    Assert.assertFalse(transition1)
    Assert.assertFalse(model.online)
   
    Assert.assertEquals(0, model.offlineEventCount)
    Assert.assertEquals(0, model.onlineEventCount)

    // Now trigger it from no signal from model start.
    val offlineTime = modelCreationTime + timeoutTime + 1
    val transition2 = model.checkIfOfflineTransition(offlineTime)
    Assert.assertTrue(transition2)
    Assert.assertFalse(model.online)
    
    Assert.assertEquals(1, model.offlineEventCount)
    Assert.assertEquals(0, model.onlineEventCount)
  }

  /**
   * Test to see if offline happens when there has been a heartbeat limit but no sensor update limit.
   */
  @Test def testOfflineFromOnlineHeartbeat(): Unit = {
    val lastUpdate = 30000
    val timeoutTime = 1000l

    val model = new TestObject(Option(timeoutTime), None, modelCreationTime)

    // Set the model online and last update time
    model.setOnline(true)
    model.setLastHeartbeatUpdateTime(lastUpdate)
    
    Assert.assertEquals(0, model.offlineEventCount)
    Assert.assertEquals(0, model.onlineEventCount)

    // Model didn't change from short change
    val transition1 = model.checkIfOfflineTransition(lastUpdate + timeoutTime / 2)
    Assert.assertFalse(transition1)
    Assert.assertTrue(model.online)
    
    Assert.assertEquals(0, model.offlineEventCount)
    Assert.assertEquals(0, model.onlineEventCount)

    // Now trigger it
    var offlineTime = lastUpdate + timeoutTime + 1
    val transition2 = model.checkIfOfflineTransition(offlineTime)
    Assert.assertTrue(transition2)
    Assert.assertFalse(model.online)
    
    Assert.assertEquals(1, model.offlineEventCount)
    Assert.assertEquals(0, model.onlineEventCount)
  }

  /**
   * Test to see if offline happens when there has been a heartbeat limit but it comes from a sensor update limit.
   */
  @Test def testOfflineFromOnlineHeartbeatFromSensor(): Unit = {
    val lastUpdate = 30000
    val timeoutTime = 1000l

    val model = new TestObject(Option(timeoutTime), None, modelCreationTime)

    // Set the model online and last update time
    model.setOnline(true)
    model.setLastHeartbeatUpdateTime(lastUpdate/2)
    model.setLastUpdateTime(lastUpdate)
    
    Assert.assertEquals(0, model.offlineEventCount)
    Assert.assertEquals(0, model.onlineEventCount)

    // Model didn't change from short change
    Assert.assertFalse(model.checkIfOfflineTransition(lastUpdate + timeoutTime / 2))
    Assert.assertTrue(model.online)
    
    Assert.assertEquals(0, model.offlineEventCount)
    Assert.assertEquals(0, model.onlineEventCount)

    // Now trigger it
    var offlineTime = lastUpdate + timeoutTime + 1
    Assert.assertTrue(model.checkIfOfflineTransition(offlineTime))
    Assert.assertFalse(model.online)
    
    Assert.assertEquals(1, model.offlineEventCount)
    Assert.assertEquals(0, model.onlineEventCount)
  }

  /**
   * Test a model heartbeat update and confirm that it makes the sensor model online, and properly
   * records when it happened. Also, no online event was triggered because no offline event was triggered.
   *
   */
  @Test def testHeartbeatUpdate(): Unit = {
    val model = new TestObject(None, None, modelCreationTime)
    
    val timestampCurrent = 10000l

    model.setOnline(false)
    
    Assert.assertEquals(0, model.offlineEventCount)
    Assert.assertEquals(0, model.onlineEventCount)

    Assert.assertTrue(model.timestampLastHeartbeat.isEmpty)

    model.updateHeartbeat(timestampCurrent)

    Assert.assertEquals(timestampCurrent, model.timestampLastHeartbeat.get)
    Assert.assertTrue(model.online)
    
    Assert.assertEquals(0, model.offlineEventCount)
    Assert.assertEquals(0, model.onlineEventCount)
  }

  /**
   * Test a model heartbeat update and confirm that it makes the sensor model online, and properly
   * records when it happened. Also, an online event was triggered because an offline event had been signalled.
   *
   */
  @Test def testHeartbeatUpdateEmitOnline(): Unit = {
    val model = new TestObject(None, None, modelCreationTime)
    
    val timestampCurrent = 10000l

    model.setOnline(false)
    model.setOfflineSignaled(true)
    
    Assert.assertEquals(0, model.offlineEventCount)
    Assert.assertEquals(0, model.onlineEventCount)

    Assert.assertTrue(model.timestampLastHeartbeat.isEmpty)

    model.updateHeartbeat(timestampCurrent)

    Assert.assertEquals(timestampCurrent, model.timestampLastHeartbeat.get)
    Assert.assertTrue(model.online)
    
    Assert.assertEquals(0, model.offlineEventCount)
    Assert.assertEquals(1, model.onlineEventCount)
  }

  /**
   * Test a model state update and confirm that it makes the sensor model online, and properly
   * records when it happened. Also, no online event was triggered because no offline event was triggered.
   *
   */
  @Test def testStateUpdate(): Unit = {
    val model = new TestObject(None, None, modelCreationTime)
    
    val timestampCurrent = 10000l

    model.setOnline(false)
    
    Assert.assertEquals(0, model.offlineEventCount)
    Assert.assertEquals(0, model.onlineEventCount)

    Assert.assertTrue(model.timestampLastUpdate.isEmpty)

    model.stateUpdated(timestampCurrent)

    Assert.assertEquals(timestampCurrent, model.timestampLastUpdate.get)
    Assert.assertTrue(model.online)
    
    Assert.assertEquals(0, model.offlineEventCount)
    Assert.assertEquals(0, model.onlineEventCount)
  }

  /**
   * Test a model state update and confirm that it makes the sensor model online, and properly
   * records when it happened. Also, an online event was triggered because an offline event had been signalled.
   *
   */
  @Test def tesStateUpdateEmitOnline(): Unit = {
    val model = new TestObject(None, None, modelCreationTime)
    
    val timestampCurrent = 10000l

    model.setOnline(false)
    model.setOfflineSignaled(true)
    
    Assert.assertEquals(0, model.offlineEventCount)
    Assert.assertEquals(0, model.onlineEventCount)

    Assert.assertTrue(model.timestampLastUpdate.isEmpty)

    model.stateUpdated(timestampCurrent)

    Assert.assertEquals(timestampCurrent, model.timestampLastUpdate.get)
    Assert.assertTrue(model.online)
    
    Assert.assertEquals(0, model.offlineEventCount)
    Assert.assertEquals(1, model.onlineEventCount)
  }

  class TestObject(heartbeat: Option[Long], state: Option[Long], creation: Long) extends StandardHeartbeatMonitorable { 
    var onlineEventCount = 0
    var offlineEventCount = 0
    
    override def stateUpdateTimeLimit = state
    override def heartbeatUpdateTimeLimit = heartbeat
    
    val timestampItemCreation = creation
    
    override def emitOnlineEvent(timestamp: Long): Unit = {
      onlineEventCount = onlineEventCount + 1
    }
    
    override def emitOfflineEvent(timestamp: Long): Unit = {
      offlineEventCount = offlineEventCount + 1
    }
  }
}