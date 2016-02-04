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

package io.smartspaces.master.resource.deployment.internal;

import io.smartspaces.master.resource.deployment.DeploymentServer;
import io.smartspaces.master.resource.deployment.Feature;
import io.smartspaces.master.resource.deployment.FeatureRepository;
import io.smartspaces.master.resource.deployment.RemoteRepositoryMaster;
import io.smartspaces.master.resource.deployment.ResourceDependencyResolver;

import org.ros.osgi.common.RosEnvironment;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * A feature deployer for ROS nodes.
 *
 * TODO(keith): This is dead in the water for now.
 *
 * @author Keith M. Hughes
 */
public class RosDeploymentServer implements DeploymentServer {

  /**
   * The ROS Environment this server will run in.
   */
  private RosEnvironment rosEnvironment;

  /**
   * Resolves all bundles needed for a feature.
   */
  private ResourceDependencyResolver featureBundleResolver;

  /**
   * The repository master which makes features and bundles available.
   */
  private RemoteRepositoryMaster repositoryMaster;

  /**
   * The feature repository.
   */
  private FeatureRepository featureRepository;

  @Override
  public void startup() {
  }

  @Override
  public void shutdown() {
    // Nothing to do
  }

  @Override
  public void deployFeature(String node, List<String> features) {
    if (!canRun()) {
      rosEnvironment.getLog().error("Unable to deploy features at this time.");
      return;
    }

    List<Feature> bundles = new ArrayList<>();

    for (String featureId : features) {
      Feature feature = featureRepository.getFeature(featureId);
      if (feature != null) {
        bundles.add(feature);
      } else {
        rosEnvironment.getLog().error(String.format("Could not deploy feature %s", featureId));
      }
    }

    Set<String> allBundles = featureBundleResolver.getDependencies(bundles);

    Set<String> bundleUris = repositoryMaster.getBundleUris(allBundles);
  }

  /**
   * Can the service be run?
   *
   * @return True if it can be run, false otherwise.
   */
  public boolean canRun() {
    return true;
  }

  /**
   * @param featureBundleResolver
   *          the featureBundleResolver to set
   */
  public void setFeatureBundleResolver(ResourceDependencyResolver featureBundleResolver) {
    this.featureBundleResolver = featureBundleResolver;
  }

  /**
   * @param featureBundleResolver
   *          the featureBundleResolver to set
   */
  public void unsetFeatureBundleResolver(ResourceDependencyResolver featureBundleResolver) {
    this.featureBundleResolver = null;
  }

  /**
   * @param repositoryMaster
   *          the repositoryMaster to set
   */
  public void setRepositoryMaster(RemoteRepositoryMaster repositoryMaster) {
    this.repositoryMaster = repositoryMaster;
  }

  /**
   * @param repositoryMaster
   *          the repositoryMaster to set
   */
  public void unsetRepositoryMaster(RemoteRepositoryMaster repositoryMaster) {
    this.repositoryMaster = null;
  }

  /**
   * @param featureRepository
   *          the featureRepository to set
   */
  public void setFeatureRepository(FeatureRepository featureRepository) {
    this.featureRepository = featureRepository;
  }

  /**
   * @param featureRepository
   *          the featureRepository to set
   */
  public void unsetFeatureRepository(FeatureRepository featureRepository) {
    this.featureRepository = null;
  }

  /**
   * Set the Ros Environment the server should run in.
   *
   * @param rosEnvironment
   */
  public void setRosEnvironment(RosEnvironment rosEnvironment) {
    this.rosEnvironment = rosEnvironment;
  }

  /**
   * Remove the ROS Environment that was being used.
   *
   * @param rosEnvironment
   */
  public void unsetRosEnvironment(RosEnvironment rosEnvironment) {
    this.rosEnvironment = null;
  }
}
