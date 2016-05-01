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

package io.smartspaces.domain.basic.pojo;

import io.smartspaces.domain.basic.Activity;
import io.smartspaces.domain.basic.ActivityDependency;
import io.smartspaces.domain.pojo.SimpleObject;

import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A POJO implementation of a {@link Activity}.
 *
 * <p>
 * This implementation includes a couple of extra methods.
 *
 * @author Keith M. Hughes
 */
public class SimpleActivity extends SimpleObject implements Activity {

  /**
   * For serialization.
   */
  private static final long serialVersionUID = 8627347933385579008L;

  /**
   * The identifying name of the activity.
   */
  private String identifyingName;

  /**
   * The descriptive name of the activity.
   */
  private String name;

  /**
   * The description of the activity.
   */
  private String description;

  /**
   * Version of the activity.
   */
  private String version;

  /**
   * When the activity was last uploaded.
   */
  private Date lastUploadDate;

  /**
   * When the activity was last started.
   */
  private Date lastStartDate;

  /**
   * The activity bundle content hash.
   */
  private String bundleContentHash;

  /**
   * The dependencies the activity has.
   */
  private List<ActivityDependency> dependencies = new ArrayList<>();

  /**
   * The meta data for this activity.
   */
  private Map<String, Object> metadata = new HashMap<>();

  @Override
  public String getIdentifyingName() {
    return identifyingName;
  }

  @Override
  public void setIdentifyingName(String identifyingName) {
    this.identifyingName = identifyingName;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public void setName(String name) {
    this.name = name;
  }

  @Override
  public String getDescription() {
    return description;
  }

  @Override
  public void setDescription(String description) {
    this.description = description;
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
  public Date getLastStartDate() {
    return lastStartDate;
  }

  @Override
  public void setLastStartDate(Date lastStartDate) {
    this.lastStartDate = lastStartDate;
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
  public void setDependencies(List<ActivityDependency> dependencies) {
    this.dependencies = dependencies;
  }

  @Override
  public List<ActivityDependency> getDependencies() {
    return Lists.newArrayList(dependencies);
  }

  @Override
  public void setMetadata(Map<String, Object> metadata) {
    this.metadata = metadata;
  }

  @Override
  public Map<String, Object> getMetadata() {
    return metadata;
  }
}
