/*
 * Copyright (C) 2016 Keith M. Hughes
 * Copyright (C) 2013 Google Inc.
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

package io.smartspaces.container.control.message.container.resource.deployment;

import io.smartspaces.resource.NamedVersionedResourceDependencyReference;

import java.util.HashSet;
import java.util.Set;

/**
 * A request for resource deployments to the container.
 *
 * @author Keith M. Hughes
 */
public class ContainerResourceDeploymentQueryRequest {

  /**
   * ID for the transaction for the deployment request.
   */
  private String transactionId;

  /**
   * The queries.
   */
  private Set<NamedVersionedResourceDependencyReference> queries = new HashSet<>();

  /**
   * Construct a new query.
   */
  public ContainerResourceDeploymentQueryRequest() {
  }

  /**
   * Construct a new query.
   *
   * @param transactionId
   *          transaction ID for the query
   */
  public ContainerResourceDeploymentQueryRequest(String transactionId) {
    this.transactionId = transactionId;
  }

  /**
   * Get the transaction ID for the query.
   *
   * @return the transaction ID
   */
  public String getTransactionId() {
    return transactionId;
  }

  /**
   * Set the transaction ID for the query.
   *
   * @param transactionId
   *          the transaction ID
   */
  public void setTransactionId(String transactionId) {
    this.transactionId = transactionId;
  }

  /**
   * Add in a new query.
   *
   * @param query
   *          the query to add
   */
  public void addQuery(NamedVersionedResourceDependencyReference query) {
    queries.add(query);
  }

  /**
   * Get the queries.
   *
   * @return the queries
   */
  public Set<NamedVersionedResourceDependencyReference> getQueries() {
    return queries;
  }

  /**
   * Set the queries.
   *
   * @param queries
   *          the queries
   */
  public void setQueries(Set<NamedVersionedResourceDependencyReference> queries) {
    this.queries = queries;
  }
}
