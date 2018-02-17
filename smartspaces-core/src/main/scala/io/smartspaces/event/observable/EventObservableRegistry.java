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

package io.smartspaces.event.observable;

import io.reactivex.Observable;
import io.smartspaces.scope.ManagedScope;
import scala.Option;

/**
 * The service for creating event observables and maintaining a global registry
 * of observables.
 * 
 * @author Keith M. Hughes
 */
public interface EventObservableRegistry {

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
  EventObservableRegistry registerObservable(String observableName, Observable<?> observable);

  /**
   * Add in a new observable.
   * 
   * @param observableName
   *          the name of the observable
   * @param nameScope
   *          the potential name scope
   * @param observable
   *          the observable
   * 
   * @return this service
   */
  EventObservableRegistry registerObservable(String observableName, Option<String> nameScope, Observable<?> observable);

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
  EventObservableRegistry unregisterObservable(String observableName);

  /**
   * Remove an observable.
   * 
   * <p>
   * Does nothing if the observable wasn't there.
   * 
   * @param observableName
   *          the name of the observable
   * @param nameScope
   *          the potential name scope
   * 
   * @return this service
   */
  EventObservableRegistry unregisterObservable(String observableName, Option<String> nameScope);

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
   * @param observableName
   *          the name of the observable
   * @param nameScope
   *          the potential name scope
   * 
   * @return the named observable, or {@code null} if no such observable
   */
  <T extends Observable<?>> T getObservable(String observableName, Option<String> nameScope);
  
  /**
   * Connect the given observer to the named observable.
   * 
   * <p>
   * The observer will be added to the managed scope so it will be disconnected when the scope shuts down.
   * 
   * @param observableName
   *           the name of the observable
   * @param scope
   *           the managed scope to place the observer in
   * @param observers
   *           the observers to add
   *           
   * @return {@code true} if the observable with the given name was found
   */
  <T> boolean connectObservers(String observableName, ManagedScope scope, BaseObserver<T>... observers);
  
  /**
   * Connect the given observer to the named observable.
   * 
   * <p>
   * The observer will be added to the managed scope so it will be disconnected when the scope shuts down.
   * 
   * @param observableName
   *           the name of the observable
   * @param nameScope
   *          the potential name scope
   * @param scope
   *           the managed scope to place the observer in
   * @param observers
   *           the observers to add
   *           
   * @return {@code true} if the observable with the given name was found
   */
  <T> boolean connectObservers(String observableName, Option<String> nameScope, ManagedScope scope, BaseObserver<T>... observers);
  
  /**
   * Connect the given observer to the named observable when the observable is registered.
   * 
   * <p>
   * The observer will be added to the managed scope so it will be disconnected when the scope shuts down.
   * 
   * @param observableName
   *           the name of the observable
   * @param scope
   *           the managed scope to place the observer in
   * @param observers
   *           the observers to add
   */
  <T> void connectObserversWhenAvailable(String observableName, ManagedScope scope, BaseObserver<T>... observers);
  
  /**
   * Connect the given observer to the named observable when the observable is registered.
   * 
   * <p>
   * The observer will be added to the managed scope so it will be disconnected when the scope shuts down.
   * 
   * @param observableName
   *           the name of the observable
   * @param nameScope
   *          the potential name scope
   * @param scope
   *           the managed scope to place the observer in
   * @param observers
   *           the observers to add
   */
  <T> void connectObserversWhenAvailable(String observableName, Option<String> nameScope, ManagedScope scope, BaseObserver<T>... observers);

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

  /**
   * Get the observable with a given name.
   * 
   * <p>
   * If the observable doesn't exist yet, it will be created.
   * 
   * @param observableName
   *          the name of the observable
   * @param nameScope
   *          the potential name scope
   * @param creator
   *          the creator for new observables
   * 
   * @return the named observable
   */
  <T extends Observable<?>> T getObservable(String observableName, Option<String> nameScope, ObservableCreator<T> creator);
  
  /**
   * Scope an observable name in a uniform way if there is a scope for the name.
   * 
   * @param observableName
   *        the base observable name
   * @param nameScope
   *        a potential name scope
   *        
   * @return a potentially modified observable name
   */
  String scopeObservableName(String observableName, Option<String> nameScope);
}