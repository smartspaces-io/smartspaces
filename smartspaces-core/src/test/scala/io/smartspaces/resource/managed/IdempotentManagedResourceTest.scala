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

package io.smartspaces.resource.managed

import org.junit.Assert
import org.junit.Test
import org.scalatest.junit.JUnitSuite

/**
 * tests for the idempotent managed resource classes.
 * 
 * @author Keith M. Hughes
 */
class IdempotentManagedResourceTest extends JUnitSuite {
  
  /**
   * Test single calls to startup and shutdown for pure idempotent.
   */
  @Test
  def idempotentOnceUpAndDown(): Unit = {
    val testObject = new ItempotentManagedResourceTestObject()
    
    testObject.startup
    testObject.shutdown
    
    Assert.assertEquals(1, testObject.timesStartupCalled)
    Assert.assertEquals(1, testObject.timesShutdownCalled)
  }
  
  /**
   * Test two startup and one shutdown for pure idempotent.
   */
  @Test
  def idempotentTwiceStartOnceShutdown(): Unit = {
    val testObject = new ItempotentManagedResourceTestObject()
    
    testObject.startup
    testObject.startup
    testObject.shutdown
    
    Assert.assertEquals(1, testObject.timesStartupCalled)
    Assert.assertEquals(1, testObject.timesShutdownCalled)
  }
  
  /**
   * Test one startup and two shutdown for pure idempotent.
   */
  @Test
  def idempotentOnceStartTwiceShutdown(): Unit = {
    val testObject = new ItempotentManagedResourceTestObject()
    
    testObject.startup
    testObject.shutdown
    testObject.shutdown
    
    Assert.assertEquals(1, testObject.timesStartupCalled)
    Assert.assertEquals(1, testObject.timesShutdownCalled)
  }
  
  /**
   * Test no startup and one shutdown for pure idempotent.
   */
  @Test
  def idempotentNoneStartOnceShutdown(): Unit = {
    val testObject = new ItempotentManagedResourceTestObject()
    
    testObject.shutdown
    
    Assert.assertEquals(0, testObject.timesStartupCalled)
    Assert.assertEquals(0, testObject.timesShutdownCalled)
  }
   
  /**
   * Test single calls to startup and shutdown for usage count idempotent.
   */
  @Test
  def usageCountIdempotentOnceUpAndDown(): Unit = {
    val testObject = new UsageCountItempotentManagedResourceTestObject()
    
    testObject.startup
    testObject.shutdown
    
    Assert.assertEquals(1, testObject.timesStartupCalled)
    Assert.assertEquals(1, testObject.timesShutdownCalled)
  }
  
  /**
   * Test two startup and two shutdown for usage count idempotent.
   */
  @Test
  def usageCountIdempotentTwiceStartTwiceShutdown(): Unit = {
    val testObject = new UsageCountItempotentManagedResourceTestObject()
    
    testObject.startup
    testObject.startup
    testObject.shutdown
    testObject.shutdown
    
    Assert.assertEquals(1, testObject.timesStartupCalled)
    Assert.assertEquals(1, testObject.timesShutdownCalled)
  }
  
  /**
   * Test two startup and two shutdown for usage count idempotent.
   */
  @Test
  def usageCountIdempotentTwiceStartOnceShutdown(): Unit = {
    val testObject = new UsageCountItempotentManagedResourceTestObject()
    
    testObject.startup
    testObject.startup
    testObject.shutdown
    
    Assert.assertEquals(1, testObject.timesStartupCalled)
    Assert.assertEquals(0, testObject.timesShutdownCalled)
  }
  
  /**
   * Test one startup and two shutdown for usage count idempotent.
   */
  @Test
  def usageCountIdempotentOnceStartTwiceShutdown(): Unit = {
    val testObject = new UsageCountItempotentManagedResourceTestObject()
    
    testObject.startup
    testObject.shutdown
    testObject.shutdown
    
    Assert.assertEquals(1, testObject.timesStartupCalled)
    Assert.assertEquals(1, testObject.timesShutdownCalled)
  }
  
  /**
   * Test no startup and one shutdown for usage count idempotent.
   */
  @Test
  def usageCountIdempotentNoneStartOnceShutdown(): Unit = {
    val testObject = new UsageCountItempotentManagedResourceTestObject()
    
    testObject.shutdown
    
    Assert.assertEquals(0, testObject.timesStartupCalled)
    Assert.assertEquals(0, testObject.timesShutdownCalled)
  }
 
  class ItempotentManagedResourceTestObject extends IdempotentManagedResource {
    var timesStartupCalled = 0
    var timesShutdownCalled = 0
    
    override def onStartup(): Unit = {
      timesStartupCalled = timesStartupCalled + 1
    }
    
    override def onShutdown(): Unit = {
      timesShutdownCalled = timesShutdownCalled + 1
    }
  }
  
  class UsageCountItempotentManagedResourceTestObject extends UsageCountIdempotentManagedResource {
    var timesStartupCalled = 0
    var timesShutdownCalled = 0
    
    override def onStartup(): Unit = {
      timesStartupCalled = timesStartupCalled + 1
    }
    
    override def onShutdown(): Unit = {
      timesShutdownCalled = timesShutdownCalled + 1
    }
  }
}