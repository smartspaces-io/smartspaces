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

import java.util.ArrayList;
import java.util.List;

/**
 * A request for a commit of container resources.
 *
 * @author Keith M. Hughes
 */
public class ContainerResourceDeploymentCommitRequest {

  /**
   * The ID for a full deployment transaction.
   */
  private String transactionId;

  /**
   * The items which are part of the commit request.
   */
  private List<ContainerResourceDeploymentItem> items = new ArrayList<>();

  /**
   * Construct a commit request.
   */
  public ContainerResourceDeploymentCommitRequest() {
  }

  /**
   * Construct a commit request.
   *
   * @param transactionId
   *          transaction ID for the request
   */
  public ContainerResourceDeploymentCommitRequest(String transactionId) {
    this.transactionId = transactionId;
  }

  /**
   * Get the transaction ID for the request.
   *
   * @return the transaction ID
   */
  public String getTransactionId() {
    return transactionId;
  }

  /**
   * Set the transaction ID for the request.
   *
   * @param transactionId
   *          the transaction ID
   */
  public void setTransactionId(String transactionId) {
    this.transactionId = transactionId;
  }

  /**
   * Get all items in the request.
   *
   * @return all items in the request
   */
  public List<ContainerResourceDeploymentItem> getItems() {
    return items;
  }

  /**
   * set all items in the request.
   *
   * @param items
   *          all items in the request
   */
  public void setItems(List<ContainerResourceDeploymentItem> items) {
    this.items = items;
  }

  /**
   * Add a new item into the request.
   *
   * @param item
   *          the item to add
   */
  public void addItem(ContainerResourceDeploymentItem item) {
    items.add(item);
  }
}
