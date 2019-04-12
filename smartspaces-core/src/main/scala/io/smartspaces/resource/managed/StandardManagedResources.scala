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

import io.smartspaces.SmartSpacesException
import io.smartspaces.logging.ExtendedLog
import java.util.{Collections => JCollections}
import java.util.{List => JList}

import io.smartspaces.resource.{DependentResource, NamedResource}
import io.smartspaces.util.graph.DependencyResolver

import scala.collection.JavaConverters._


/**
 * A standard implementation of {@link ManagedResources}.
 *
 * @author Keith M. Hughes
 */
class StandardManagedResources(private val log: ExtendedLog) extends ManagedResources {

  /**
   * The managed resources.
   */
  private var resources = List[ManagedResource]()

  private var startedResources = List[ManagedResource]()

  /**
   * [[true]] if the collection has been officially started.
   */
  private var started = false

  override def addResource( resource: ManagedResource): Unit = {
    if (started) {
      try {
        // Will only add if starts up properly
        resource.startup()


        addStartedResource(resource)
      } catch {
        case e: Throwable =>
          throw new SmartSpacesException("Could not start up managed resource", e)
      }
    } else {
      this.synchronized {
        resources = resource :: resources
      }
    }
  }

  override def addStartedResource(resource: ManagedResource ) {
    this.synchronized {
      startedResources = resource :: startedResources
    }
  }

  override def getResources():  JList[ManagedResource] = {
    this.synchronized {
      JCollections.unmodifiableList(resources.asJava)
    }
  }

  override def clear(): Unit = {
    this.synchronized {
      resources = List()
    }
  }

  override def startupResources(): Unit = {
    var myStartedResources = List[ManagedResource]()

    var nondependentResources = List[ManagedResource]()

    val dependencyResolver = new DependencyResolver[String, ManagedResource]()

    // resources has been added in ascending order, so go in reverse.
    resources.foreach { resource =>
      if (resource.isInstanceOf[DependentResource[String]]) {
        val nameDependency = if (resource.isInstanceOf[NamedResource]) {
          resource.asInstanceOf[NamedResource].getName()
        } else {
          resource.hashCode().toString
        }

        dependencyResolver.addNode(nameDependency, resource)
        val dependencies = resource.asInstanceOf[DependentResource[String]].getDependencies()
        dependencies.asScala.foreach(dependencyResolver.addNodeDependencies(nameDependency, _))
      } else {
        nondependentResources = resource :: nondependentResources
      }
    }

    dependencyResolver.resolve()
    dependencyResolver.getDataOrdering().asScala.foreach { resource =>
      if (resource != null) {
        try {
          resource.startup()

          myStartedResources = resource :: myStartedResources
        } catch {
          case e: Throwable => {
            shutdownResources(myStartedResources)

            throw new SmartSpacesException("Could not start up all managed resources", e)
          }
        }
      } else {
        log.warn("Got dependency managed resource that did not exist. Check if all managed resource dependencies are defined.")
      }
    }

    // resources has been added in ascending order, so go in reverse.
    nondependentResources.foreach { resource =>
      try {
        resource.startup()

        myStartedResources = resource :: myStartedResources
      } catch  {
        case e: Throwable => {
          shutdownResources(myStartedResources)

          throw new SmartSpacesException("Could not start up all managed resources", e)
        }
      }
    }

    startedResources = myStartedResources

    started = true
  }

  override def shutdownResources(): Unit = {
    this.synchronized {
      shutdownResources(startedResources)
    }
  }

  override def shutdownResourcesAndClear(): Unit = {
    this.synchronized {
      shutdownResources()
      clear()
    }
  }

  /**
   * Shut down the specified resources.
   *
   * @param resources
   *          some resources to shut down
   */
  private def shutdownResources(resources: List[ManagedResource]): Unit = {
    resources.foreach { resource =>
      try {
        resource.shutdown()
      } catch {
        case e: Throwable =>
         log.error("Could not shut down resource", e)
      }
    }
  }
}
