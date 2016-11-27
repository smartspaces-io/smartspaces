/*
 * Copyright (C) 2016 Keith M. Hughes
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

/**
 * A request for controller data.
 * 
 * @author Keith M. Hughes
 */
public class ControllerDataRequest {

  /**
   * The URI for data transfer.
   */
  private String transferUri;

  /**
   * The transfer type.
   */
  private ControllerDataRequestTransferType transferType;

  /**
   * Construct a new request.
   */
  public ControllerDataRequest() {
  }

  /**
   * Construct a new request.
   * 
   * @param transferType
   *          the transfer type
   * @param transferUri
   *          the URI for the transfer
   */
  public ControllerDataRequest(ControllerDataRequestTransferType transferType,
      String transferUri) {
    this.transferType = transferType;
    this.transferUri = transferUri;
  }

  /**
   * Get the transfer type.
   * 
   * @return the transfer type
   */
  public ControllerDataRequestTransferType getTransferType() {
    return transferType;
  }

  /**
   * Set the transfer type.
   * 
   * @param transferType
   *          the transfer type
   */
  public void setTransferType(ControllerDataRequestTransferType transferType) {
    this.transferType = transferType;
  }

  /**
   * Get the transfer URI.
   * 
   * @return the transfer URI
   */
  public String getTransferUri() {
    return transferUri;
  }

  /**
   * Set the transfer URI.
   * 
   * @param transferUri
   *          the transfer URI
   */
  public void setTransferUri(String transferUri) {
    this.transferUri = transferUri;
  }

  /**
   * The transfer types for a controller data request.
   * 
   * @author Keith M. Hughes
   */
  public enum ControllerDataRequestTransferType {

    /**
     * Copy the controller permanent data folder.
     */
    CONTROLLER_DATA_PERMANENT
  }
}
