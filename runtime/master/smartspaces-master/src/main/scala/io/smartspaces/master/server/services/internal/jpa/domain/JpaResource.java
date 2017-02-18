/*
 * Copyright (C) 2017 Keith M. Hughes
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

package io.smartspaces.master.server.services.internal.jpa.domain;

import io.smartspaces.domain.basic.Resource;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Version;

/**
 * A JPA implementation of a {@link Resource}.
 *
 * @author Keith M. Hughes
 */
@Entity
@Table(name = "resources")
@NamedQueries({
    @NamedQuery(name = "resourceAll", query = "select r from JpaResource r"),
    @NamedQuery(
        name = "resourceByNameAndVersion",
        query = "select r from JpaResource r where r.identifyingName = :identifyingName and r.version = :version"),
    @NamedQuery(name = "countResourceAll", query = "select count(r) from JpaResource r"), })
public class JpaResource implements Resource {

  /**
   * For serialization.
   */

  /**
   * The persistence ID for the resource.
   */
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Column(nullable = false, length = 64)
  private String id;

  /**
   * The identifying identifyingName of the resource.
   */
  @Column(nullable = false, length = 512)
  private String identifyingName;

  /**
   * Version of the resource.
   */
  @Column(nullable = false, length = 32)
  private String version;

  /**
   * When the resource was last uploaded.
   */
  @Column(nullable = true)
  private Date lastUploadDate;

  /**
   * The hash of the resource content bundle.
   */
  @Column(nullable = true)
  private String bundleContentHash;

  /**
   * The database version. Used for detecting concurrent modifications.
   */
  @Version
  private long databaseVersion;

  @Override
  public String getId() {
    return id;
  }

  @Override
  public String getIdentifyingName() {
    return identifyingName;
  }

  @Override
  public void setIdentifyingName(String identifyingName) {
    this.identifyingName = identifyingName;
  }

  @Override
  public String getVersion() {
    return version;
  }

  @Override
  public void setVersion(String version) {
    this.version = version;
  }

  @Override
  public Date getLastUploadDate() {
    return lastUploadDate;
  }

  @Override
  public void setLastUploadDate(Date lastUploadDate) {
    this.lastUploadDate = lastUploadDate;
  }

  @Override
  public String getBundleContentHash() {
    return bundleContentHash;
  }

  @Override
  public void setBundleContentHash(String bundleContentHash) {
    this.bundleContentHash = bundleContentHash;
  }

  @Override
  public String toString() {
    return "JpaResourceO [id=" + id + ", identifyingName=" + identifyingName + ", version=" + version + ", lastUploadDate="
        + lastUploadDate + ", bundleContentHash=" + bundleContentHash
        + "]";
  }
}
