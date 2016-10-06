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

package io.smartspaces.service.event.observable;

import io.smartspaces.service.SupportedService;

import io.reactivex.Observable;

/**
 * The service for creating event observables and maintaining a global registry
 * of observables.
 * 
 * @author Keith M. Hughes
 */
public interface EventObservableService extends SupportedService {

  /**
   * The service name.
   */
  public static final String SERVICE_NAME = "event.observable";

  /**
   * Add in a new observable.
   * 
   * @param observableName
   *          the name of the observable
   * @param observable
   *          the observable
   * 
   * @return this service
   */
  EventObservableService registerObservable(String observableName, Observable<?> observable);

  /**
   * Remove an observable.
   * 
   * <p>
   * Does nothing if the observable wasn't there.
   * 
   * @param observableName
   *          the name of the observable
   * 
   * @return this service
   */
  EventObservableService unregisterObservable(String observableName);

  /**
   * Get the observable with a given name.
   * 
   * @param observableName
   *          the name of the observable
   * 
   * @return the named observable, or {@code null} if no such observable
   */
  <T extends Observable<?>> T getObservable(String observableName);

  /**
   * Get the observable with a given name.
   * 
   * <p>
   * If the observable doesn't exist yet, it will be created.
   * 
   * @param observableName
   *          the name of the observable
   * @param creator
   *          the creator for new observables
   * 
   * @return the named observable
   */
  <T extends Observable<?>> T getObservable(String observableName, ObservableCreator<T> creator);
}