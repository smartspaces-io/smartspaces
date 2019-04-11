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

package io.smartspaces.resource.managed

import org.scalatest.junit.JUnitSuite
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import io.smartspaces.logging.ExtendedLog
import io.smartspaces.resource.DependentResource
import io.smartspaces.resource.NamedResource
import org.junit.Before
import org.junit.Test
import org.mockito.{Matchers, Mockito}
import java.lang.{Iterable => JIterable}

import scala.collection.mutable.ArrayBuffer
import scala.collection.JavaConverters._

/**
  * Tests for the {@link StandardManagedResources} class.
  *
  * @author Keith M. Hughes
  */
class StandardManagedResourcesTest extends JUnitSuite {

  private var resources: StandardManagedResources = _

  private var log: ExtendedLog = _

  @Before def setup(): Unit = {
    log = Mockito.mock(classOf[ExtendedLog])

    resources = new StandardManagedResources(log)

  }

  /**
    * Test that a clean startup and shutdown works.
    *
    * This test contains non-dependency nodes only.
    */
  @Test def testCleanStartup(): Unit = {
    val resource1 = Mockito.mock(classOf[ManagedResource])
    val resource2 = Mockito.mock(classOf[ManagedResource])

    resources.addResource(resource1)
    resources.addResource(resource2)

    resources.startupResources()
    resources.shutdownResources()

    Mockito.verify(resource1, Mockito.times(1)).startup()
    Mockito.verify(resource1, Mockito.times(1)).shutdown()
    Mockito.verify(resource2, Mockito.times(1)).startup()
    Mockito.verify(resource2, Mockito.times(1)).shutdown()
  }

  /**
    * Test that a broken startup works.
    *
    * This test contains non-dependency nodes only.
    */
  @Test def testBrokenStartup(): Unit = {

    val resource1 = Mockito.mock(classOf[ManagedResource])
    val resource2 = Mockito.mock(classOf[ManagedResource])
    val resource3 = Mockito.mock(classOf[ManagedResource])

    val e = new Error()
    Mockito.doThrow(e).when(resource2).startup()

    resources.addResource(resource1)
    resources.addResource(resource2)
    resources.addResource(resource3)

    try {
      resources.startupResources()

      fail()
    } catch {
      case e1: Exception =>
        assertEquals(e, e1.getCause())
    }

    Mockito.verify(resource1, Mockito.times(1)).startup()
    Mockito.verify(resource1, Mockito.times(1)).shutdown()
    Mockito.verify(resource2, Mockito.times(1)).startup()
    Mockito.verify(resource2, Mockito.never()).shutdown()
    Mockito.verify(resource3, Mockito.never()).startup()
    Mockito.verify(resource3, Mockito.never()).shutdown()
  }

  /**
    * Test that a broken shutdown works.
    *
    * This test contains non-dependency nodes only.
    */
  @Test def testBrokenShutdown(): Unit = {
    val resource1 = Mockito.mock(classOf[ManagedResource])
    val resource2 = Mockito.mock(classOf[ManagedResource])
    val resource3 = Mockito.mock(classOf[ManagedResource])

    val e = new Error()
    Mockito.doThrow(e).when(resource2).shutdown()

    resources.addResource(resource1)
    resources.addResource(resource2)
    resources.addResource(resource3)

    resources.startupResources()
    resources.shutdownResources()

    Mockito.verify(resource1, Mockito.times(1)).startup()
    Mockito.verify(resource1, Mockito.times(1)).shutdown()
    Mockito.verify(resource2, Mockito.times(1)).startup()
    Mockito.verify(resource2, Mockito.times(1)).shutdown()
    Mockito.verify(resource3, Mockito.times(1)).startup()
    Mockito.verify(resource3, Mockito.times(1)).shutdown()

    Mockito.verify(log, Mockito.times(1)).error(Matchers.anyString(), Matchers.eq(e))
  }

  /**
    * Test that a clean startup and shutdown works.
    *
    * This test contains non-dependency nodes only.
    */
  @Test def testCleanStartupDependencies(): Unit = {
    val resource1 = Mockito.mock(classOf[ManagedResource])
    val resource2 = Mockito.mock(classOf[ManagedResource])

    val startupList = ArrayBuffer[String]()
    val shutdownList = ArrayBuffer[String]()

    val resourceA = new TestNameDependencyManagedResource("a", List("b", "c"), startupList, shutdownList)
    val resourceB = new TestNameDependencyManagedResource("b", List(), startupList, shutdownList)
    val resourceC = new TestNameDependencyManagedResource("c", List(), startupList, shutdownList)

    resources.addResource(resourceB)
    resources.addResource(resource1)
    resources.addResource(resourceA)
    resources.addResource(resource2)
    resources.addResource(resourceC)

    resources.startupResources()
    resources.shutdownResources()

    Mockito.verify(resource1, Mockito.times(1)).startup()
    Mockito.verify(resource1, Mockito.times(1)).shutdown()
    Mockito.verify(resource2, Mockito.times(1)).startup()
    Mockito.verify(resource2, Mockito.times(1)).shutdown()
  }


  class TestNameDependencyManagedResource(
    name: String,
    dependencies: Iterable[String],
    startupList: ArrayBuffer[String],
    shutdownList: ArrayBuffer[String]) extends ManagedResource with NamedResource with DependentResource[String] {

    override def getDependencies(): JIterable[String] = dependencies.asJava

    override def getName(): String = name

    override def startup(): Unit = {
      startupList += name
    }

    override def shutdown(): Unit = {
      shutdownList += name
    }

    override def toString(): String = s"Test managed resource ${name}"
  }
}
