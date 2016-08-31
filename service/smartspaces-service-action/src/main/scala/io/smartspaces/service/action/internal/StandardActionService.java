/*
 * Copyright (C) 2015 Keith M. Hughes
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

package io.smartspaces.service.action.internal;

import com.google.common.collect.Maps;

import io.smartspaces.SimpleSmartSpacesException;
import io.smartspaces.evaluation.ExecutionContext;
import io.smartspaces.resource.NamedVersionedResourceCollection;
import io.smartspaces.resource.Version;
import io.smartspaces.resource.VersionRange;
import io.smartspaces.service.BaseSupportedService;
import io.smartspaces.service.action.Action;
import io.smartspaces.service.action.ActionReference;
import io.smartspaces.service.action.ActionService;
import io.smartspaces.service.action.ActionSource;

import java.util.Map;

/**
 * A service for actions.
 * 
 * @author Keith M. Hughes
 */
public class StandardActionService extends BaseSupportedService implements ActionService {

  /**
   * The default version to give action sources.
   */
  public static Version DEFAULT_VERSION = new Version(0, 0, 0);

  /**
   * The action sources.
   */
  private NamedVersionedResourceCollection<ActionSource> sources =
      NamedVersionedResourceCollection.newNamedVersionedResourceCollection();

  @Override
  public String getName() {
    return SERVICE_NAME;
  }

  @Override
  public void registerActionSource(String sourceName, ActionSource source) {
    registerActionSource(sourceName, DEFAULT_VERSION, source);
  }

  @Override
  public void registerActionSource(String sourceName, Version sourceVersion, ActionSource source) {
    sources.addResource(sourceName, sourceVersion, source);
  }

  @Override
  public void performAction(String actionSourceName, String actionName, ExecutionContext context) {
    performAction(actionSourceName, null, actionName, context);
  }

  @Override
  public void performActionReference(ActionReference actionReference, ExecutionContext context) {
    context.setValues(actionReference.getData());
    performAction(actionReference.getActionSource(), actionReference.getActionSourceVersionRange(),
        actionReference.getActionName(), context);
  }

  @Override
  public void performAction(String actionSourceName, VersionRange actionSourceVersionRange,
      String actionName, ExecutionContext context) {
    ActionSource source = actionSourceVersionRange != null
        ? sources.getResource(actionSourceName, actionSourceVersionRange)
        : sources.getHighestResource(actionSourceName);
    if (source != null) {
      Action action = source.getAction(actionName);
      if (action != null) {
        action.perform(context);
      } else {
        throw new SimpleSmartSpacesException(String.format("Action %s:%s for version %s not found",
            actionSourceName, actionName, actionSourceVersionRange));
      }
    } else {
      throw new SimpleSmartSpacesException(
          String.format("No action source found for action %s:%s for version %s", actionSourceName,
              actionName, actionSourceVersionRange));
    }
  }

  /**
   * Merge the action reference data with the supplied data.
   * 
   * @param actionReference
   *          the action reference
   * @param data
   *          the supplied data
   * 
   * @return the merged data
   */
  private Map<String, ? extends Object> getMergedData(ActionReference actionReference,
      Map<String, Object> data) {
    Map<String, Object> mergedMap = Maps.newHashMap(actionReference.getData());
    mergedMap.putAll(data);

    return mergedMap;
  }
}
