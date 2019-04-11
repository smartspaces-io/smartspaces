/**
 *
 */
package io.smartspaces.master.resource.deployment.internal;

import io.smartspaces.resource.NamedVersionedResourceDependency;
import io.smartspaces.resource.SimpleNamedVersionedResource;

import java.util.Collection;

import org.apache.felix.bundlerepository.Repository;

/**
 * @author Keith M. Hughes
 */
public class SolutionImpl implements Solution {

  private SolutionState state;

  private final Repository repository;
  private final Collection<NamedVersionedResourceDependency> dependencies;

  /**
   * The subset of the repository that we are interested in
   */
  public SolutionImpl(Repository repository, Collection<NamedVersionedResourceDependency> dependencies) {
    this.repository = repository;
    this.dependencies = dependencies;
  }

  @Override
  public void addDependency(SimpleNamedVersionedResource resource) {
    // TODO Auto-generated method stub

  }

  @Override
  public void setState(SolutionState state) {
    this.state = state;
  }
}
