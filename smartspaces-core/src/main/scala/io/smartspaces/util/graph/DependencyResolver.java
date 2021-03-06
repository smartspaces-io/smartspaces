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

package io.smartspaces.util.graph;

import io.smartspaces.SmartSpacesException;

import com.google.common.collect.Sets;

import java.util.*;

/**
 * A {@link GraphWalkerObserver} which gives a dependency mapping.
 *
 * <p>
 * If a depends on b, a will appear after b in the ordering. The sort is stable
 * with respect to the order of nodes added to the resolver, that is nodes which
 * have no dependency connection to each other will appear in the order added.
 *
 * <p>
 * Does not allow acyclic graphs and will throw an exception during the walk.
 *
 * @param <I>
 *          type of IDs in the graph
 * @param <T>
 *          type of the data in the graph
 *
 * @author Keith M. Hughes
 */
public class DependencyResolver<I, T> {

  /**
   * The ordering of the data.
   */
  private List<WalkableGraphNode<I, T>> ordering = new ArrayList<>();

  /**
   * The nodes added to the resolver.
   *
   * <p>
   * Making this a linked hash set is what makes the dependency sort stable.
   */
  private Set<WalkableGraphNode<I, T>> nodes = Sets.newLinkedHashSet();

  /**
   * The walker that will walk the graph.
   */
  private DepthFirstGraphWalker<I, T> walker = new DepthFirstGraphWalker<I, T>();

  /**
   * Add a new node to the graph.
   *
   * @param nodeId
   *          the ID of the node
   * @param data
   *          the node data
   */
  public void addNode(I nodeId, T data) {
    WalkableGraphNode<I, T> node = walker.getNode(nodeId);
    node.setData(data);

    nodes.add(node);
  }

  /**
   * Add a collection of dependencies to a node.
   *
   * @param nodeId
   *          the ID of the node the dependencies will be added to
   * @param dependencyIds
   *          the IDs of the dependencies
   */
  public void addNodeDependencies(I nodeId, I... dependencyIds) {
    WalkableGraphNode<I, T> node = walker.getNode(nodeId);
    if (dependencyIds != null) {
      for (I neighborId : dependencyIds) {
        node.addNeighbor(walker.getNode(neighborId));
      }
    }
  }

  /**
   * Add a collection of dependencies for a node.
   *
   * @param nodeId
   *          the ID of the node the dependencies will be added to
   * @param dependencyIds
   *          the IDs of neighbors
   */
  public void addNodeDependencies(I nodeId, Collection<I> dependencyIds) {
    WalkableGraphNode<I, T> node = walker.getNode(nodeId);
    if (dependencyIds != null) {
      for (I neighborId : dependencyIds) {
        node.addNeighbor(walker.getNode(neighborId));
      }
    }
  }

  /**
   * Calculate the dependency ordering.
   */
  public void resolve() {
    MyGraphWalkerObserver observer = new MyGraphWalkerObserver();

    walker.setDirected(true);

    for (WalkableGraphNode<I, T> node : nodes) {
      if (!node.isDiscovered()) {
        walker.walkNode(node, observer);
      }
    }
  }

  /**
   * Get the final node ordering.
   *
   * @return the ordering
   */
  public List<WalkableGraphNode<I, T>> getNodeOrdering() {
    return Collections.unmodifiableList(ordering);
  }

  /**
   * Get the final data ordering.
   *
   * @return the ordering
   */
  public List<T> getDataOrdering() {
    List<T> result = new ArrayList<>();

    for (WalkableGraphNode<I, T> node : ordering) {
      result.add(node.getData());
    }

    return result;
  }

  private class MyGraphWalkerObserver extends BaseGraphWalkerObserver<I, T> {

    @Override
    public void observeGraphNodeAfter(WalkableGraphNode<I, T> node) {
      ordering.add(node);
    }

    @Override
    public void observeGraphEdge(WalkableGraphNode<I, T> nodeFrom, WalkableGraphNode<I, T> nodeTo,
                                 GraphWalkerEdgeClassification classification) {
      if (classification.equals(GraphWalkerEdgeClassification.BACK)) {
        throw new SmartSpacesException(String.format("Cycle in dependency graph from %s to %s",
                nodeFrom.getData(), nodeTo.getData()));
      }
    }

  }
}
