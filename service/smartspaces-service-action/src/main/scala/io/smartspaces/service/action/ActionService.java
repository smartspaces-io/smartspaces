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
package io.smartspaces.service.action;

import io.smartspaces.SmartSpacesException;
import io.smartspaces.evaluation.ExecutionContext;
import io.smartspaces.resource.Version;
import io.smartspaces.resource.VersionRange;
import io.smartspaces.service.SupportedService;

/**
 * A service for working with actions.
 * 
 * @author Keith M. Hughes
 */
public interface ActionService extends SupportedService {

  /**
   * The name of the service.
   */
  String SERVICE_NAME = "action";

  /**
   * Register a new source for actions.
   * 
   * <p>
   * The action source gets the {@link #DEFAULT_VERSION}.
   * 
   * @param sourceName
   *          the name of the source
   * @param source
   *          the source
   */
      void registerActionSource(String sourceName, ActionSource source);

  /**
   * Register a new source for actions.
   * 
   * @param sourceName
   *          the name of the source
   * @param sourceVersion
   *          the source of the version
   * @param source
   *          the source
   */
      void registerActionSource(String sourceName, Version sourceVersion, ActionSource source);

  /**
   * Perform an action.
   * 
   * <p>
   * The action is looked up. An exception will be thrown if a source cannot be
   * found with the given name or an action with the given name.
   * 
   * @param sourceName
   *          the name of the action source
   * @param actionName
   *          the name of the action
   * @param context
   *          the context for the action execution
   */
      void performAction(String sourceName, String actionName, ExecutionContext context)
          throws SmartSpacesException;

  /**
   * Perform the given action reference.
   * 
   * @param actionReference
   *          the action reference
   * @param context
   *          the context for the action execution
   */
      void performActionReference(ActionReference actionReference,
          ExecutionContext context);

  /**
   * Perform an action.
   * 
   * @param actionSourceName
   *          the source name for the action
   * @param actionSourceVersionRange
   *          the version range for the action source, can be {@code null}
   * @param actionName
   *          the name of the action
   * @param data
   *          the context for the action execution
   */
      void performAction(String actionSourceName, VersionRange actionSourceVersionRange,
          String actionName, ExecutionContext context);
}