/*
 * Copyright (C) 2018 Keith M. Hughes
 *
 * Licensed under the Apache License, Version 2.0 (the "License") you may not
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

package io.smartspaces.event.observable

import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.scalatest.junit.JUnitSuite

import io.smartspaces.logging.ExtendedLog

import scala.collection.mutable.HashSet

/**
 * Tests for the event publisher subject.
 *
 * @author Keith M. Hughes
 */
class EventPublisherSubjectTest extends JUnitSuite {

  @Mock var log: ExtendedLog = null

  var subject: EventPublisherSubject[String] = _

  @Before def setup(): Unit = {
    MockitoAnnotations.initMocks(this)

    subject = new EventPublisherSubject[String](log)
  }

  /**
   * One observer, all events succeed.
   */
  @Test def allSingleSucceed(): Unit = {
    val captured1 = new HashSet[String]()

    val observer1 = new BaseObserver[String] {
      override def onNext(value: String): Unit = {
        captured1.add(value)
      }
    }

    subject.subscribe(observer1)

    subject.onNext("foo")
    subject.onNext("bar")
    subject.onNext("bletch")

    Assert.assertEquals(HashSet("foo", "bar", "bletch"), captured1)
  }

  /**
   * 3 observers, all catch everything.
   */
  @Test def allThreeSucceed(): Unit = {
    val captured1 = new HashSet[String]()

    val observer1 = new BaseObserver[String] {
      override def onNext(value: String): Unit = {
        captured1.add(value)
      }
    }

    subject.subscribe(observer1)

    val captured2 = new HashSet[String]()

    val observer2 = new BaseObserver[String] {
      override def onNext(value: String): Unit = {
        captured2.add(value)
      }
    }

    subject.subscribe(observer2)

    val captured3 = new HashSet[String]()

    val observer3 = new BaseObserver[String] {
      override def onNext(value: String): Unit = {
        captured3.add(value)
      }
    }

    subject.subscribe(observer3)

    subject.onNext("foo")
    subject.onNext("bar")
    subject.onNext("bletch")

    val all = HashSet("foo", "bar", "bletch")
    Assert.assertEquals(all, captured1)
    Assert.assertEquals(all, captured2)
    Assert.assertEquals(all, captured3)
  }

  /**
   * Have 3 observers and have one of them throw an exception for the middle event.
   */
  @Test def middleFailOnce(): Unit = {
    val captured1 = new HashSet[String]()

    val observer1 = new BaseObserver[String] {
      override def onNext(value: String): Unit = {
        captured1.add(value)
      }
    }

    subject.subscribe(observer1)

    val captured2 = new HashSet[String]()

    val observer2 = new BaseObserver[String] {
      override def onNext(value: String): Unit = {
        if (value == "bar") {
          throw new Exception()
        } else {
          captured2.add(value)
        }
      }
    }

    subject.subscribe(observer2)

    val captured3 = new HashSet[String]()

    val observer3 = new BaseObserver[String] {
      override def onNext(value: String): Unit = {
        captured3.add(value)
      }
    }

    subject.subscribe(observer3)

    subject.onNext("foo")
    subject.onNext("bar")
    subject.onNext("bletch")

    val all = HashSet("foo", "bar", "bletch")
    Assert.assertEquals(all, captured1)
    Assert.assertEquals(all, captured3)
    
     Assert.assertEquals(HashSet("foo", "bletch"), captured2)
 }

}