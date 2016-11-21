/*
 * Copyright (C) 2016 Keith M. Hughes.
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

package io.smartspaces.communications.network.zeroconf.selector

import io.smartspaces.SmartSpacesException
import io.smartspaces.communications.network.zeroconf.ZeroconfServiceInfo

import scala.collection.JavaConversions._

import java.util.ArrayList
import java.util.Comparator
import java.util.HashMap
import java.util.List
import java.util.Map
import java.util.PriorityQueue
import java.util.Random
import java.util.concurrent.TimeUnit

/**
 * A {@link ZeroconfServiceSelector} which is for lightweight services.
 *
 * <p>
 * Lightweight means that there are few providers for the service that change
 * infrequently.
 *
 * @author Keith M. Hughes
 */
class LightweightZeroconfServiceSelector[T <: ZeroconfServiceInfo] extends ZeroconfServiceSelector[T] {

  /**
   * Services stored by priority.
   */
  private val servicesByPriority: Map[Integer, Services[T]] = new HashMap()

  /**
   * The services ordered by priority.
   */
  private val priorityQueue: PriorityQueue[Services[T]] = new PriorityQueue[Services[T]](10, new Comparator[Services[T]]() {
    override def compare(o1: Services[T], o2: Services[T]): Int = {
      return o1.priority - o2.priority
    }
  })

  /**
   * A list of requests for a service which have been unfulfilled.
   */
  private val pending: List[ZeroconfServiceRequest[T]] = new ArrayList()

  override def addService(serviceInfo: T): Unit = {
    val priority = serviceInfo.priority
    var services = servicesByPriority.get(priority)
    if (services == null) {
      services = new Services[T](priority)
      servicesByPriority.put(priority, services)
      priorityQueue.offer(services)
    }

    services.addService(serviceInfo)

    // If pending wasn't empty, then we have no other service other than
    // what was given just now.
    if (!pending.isEmpty()) {
      pending.foreach {
        _.setService(serviceInfo)
      }
      pending.clear()
    }
  }

  override def removeService(serviceInfo: T) = {
    val services = servicesByPriority.get(serviceInfo.priority())
    if (services != null) {
      services.removeService(serviceInfo)

      if (services.isEmpty()) {
        servicesByPriority.remove(serviceInfo.priority())
        priorityQueue.remove(services)
      }
    }
  }

  override def selectService(): Option[T] = {
    val services = priorityQueue.peek()
    if (services != null) {
      return services.selectService()
    } else {
      throw new SmartSpacesException("No zeroconf services registered")
    }
  }

  override def getService(): Option[T] = {
    try {
      if (areServicesAvailable()) {
        return selectService()
      } else {
        return newServiceRequest().getService()
      }
    } catch {
      case e: InterruptedException => throw new SmartSpacesException("Interrupted while getting a zeroconf service record")
    }
  }

  override def getService(timeout: Long, unit: TimeUnit): Option[T] = {
    try {
      if (areServicesAvailable()) {
        return selectService()
      } else {
        return newServiceRequest().getService(timeout, unit)
      }
    } catch {
      case e: InterruptedException => throw new SmartSpacesException("Interrupted while getting a zeroconf service record")
    }
  }

  override def areServicesAvailable(): Boolean = {
    return !priorityQueue.isEmpty()
  }

  /**
   * Create and queue a new service request.
   *
   * @return the queued service request
   */
  private def newServiceRequest(): ZeroconfServiceRequest[T] = {
    val request = new ZeroconfServiceRequest[T]()
    pending.add(request)
    return request
  }
}

/**
 * A set of {@info ZeroconfServiceInfo} objects of a given priority.
 *
 * @author Keith M. Hughes
 */
private class Services[T <: ZeroconfServiceInfo](val priority: Int) {

  /**
   * Random number generator for this class.
   */
  private val random = new Random(System.currentTimeMillis())

  /**
   * All services in the collection.
   */
  private val services: List[T] = new ArrayList()

  /**
   * The total weight of all services in the collection.
   */
  private var totalWeight: Int = 0

  /**
   * Add the service into the collection.
   *
   * <p>
   * If the service was there already, it will be replaced with the new one
   * just in case some values have changed.
   *
   * @param serviceInfo
   *          the service to add.
   */
  def addService(serviceInfo: T): Unit = {
    // It could be considered awful to use an arraylist for this, but
    // there will never be so many services moving in and out of service
    // that this matters and using an arraylist makes the selection
    // process
    val i = services.indexOf(serviceInfo)
    if (i != -1) {
      val oldInfo = services.get(i)
      services.set(i, serviceInfo)
      totalWeight += serviceInfo.weight() - oldInfo.weight
    } else {
      services.add(serviceInfo)
      totalWeight += serviceInfo.weight
    }
  }

  /**
   * Remove the service from the collection.
   *
   * <p>
   * Does nothing if the service wasn't there.
   *
   * @param serviceInfo
   *          the service to remove
   */
  def removeService(serviceInfo: T) = {
    services.remove(serviceInfo)
  }

  /**
   * Randomly select a service.
   *
   * @return the selected service
   */
  def selectService(): Option[T] = {
    var value = random.nextInt(totalWeight)

    var current: Long = 0
    services.foreach { (service) =>
      current += service.weight
      if (value < current) {
        return Option(service)
      }
    }

    None
  }

  /**
   * Is the collection empty?
   *
   * @return {@code true} if there are no services.
   */
  def isEmpty(): Boolean = {
    return services.isEmpty()
  }
}

