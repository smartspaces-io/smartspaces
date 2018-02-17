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

import io.smartspaces.logging.ExtendedLog;
import io.smartspaces.scope.ManagedScope;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import io.reactivex.Observable;

import scala.Option;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The standard event observable registry.
 * 
 * @author Keith M. Hughes
 */
public class StandardEventObservableRegistry implements EventObservableRegistry {
  
  private static final scala.Option<String> NONE = scala.Option.apply(null);

  /**
   * The map of observables.
   */
  private Map<String, Observable<?>> observables = new HashMap<>();

  /**
   * The map of observable names to future observers
   */
  private ListMultimap<String, FutureObservers<?>> futureObservers = ArrayListMultimap.create();

  /**
   * The logger for this registry.
   */
  private ExtendedLog log;

  /**
   * Construct a new registry.
   * 
   * @param log
   *          the logger for this registry
   */
  public StandardEventObservableRegistry(ExtendedLog log) {
    this.log = log;
  }

  @Override
  public synchronized EventObservableRegistry registerObservable(String observableName,
      Observable<?> observable) {
    return registerObservable(observableName, NONE, observable);
  }

  @Override
  public synchronized EventObservableRegistry registerObservable(
      String observableName,
      Option<String> nameScope,
      Observable<?> observable) {
    
    String finalObservableName = scopeObservableName(observableName, nameScope);
    
    log.formatInfo("Registering event observable %s", finalObservableName);
    
    observables.put(finalObservableName, observable);

    List<FutureObservers<?>> futures = futureObservers.removeAll(finalObservableName);
    for (FutureObservers<?> future : futures) {
      future.subscribe();
    }

    return this;
  }

  @Override
  public synchronized EventObservableRegistry unregisterObservable(String observableName) {
    return unregisterObservable(observableName, NONE);
  }

  @Override
  public synchronized EventObservableRegistry unregisterObservable(String observableName, Option<String> nameScope) {
    String finalObservableName = scopeObservableName(observableName, nameScope);
    
    log.formatInfo("Removing event observable %s", finalObservableName);
   
    observables.remove(finalObservableName);

    return this;
  }

  @Override
  @SuppressWarnings("unchecked")
  public synchronized <T extends Observable<?>> T getObservable(String observableName) {
    return getObservable(observableName, NONE);
  }

  @Override
  @SuppressWarnings("unchecked")
  public synchronized <T extends Observable<?>> T getObservable(String observableName, Option<String> nameScope) {
    String finalObservableName = scopeObservableName(observableName, nameScope);
    
    log.formatInfo("Getting event observable %s", finalObservableName);
    
    return (T) observables.get(finalObservableName);
  }

  @Override
  public synchronized <T extends Observable<?>> T getObservable(String observableName,
      ObservableCreator<T> creator) {
    return getObservable(observableName, NONE, creator);
  }

  @Override
  public synchronized <T extends Observable<?>> T getObservable(String observableName, 
      Option<String> nameScope,
      ObservableCreator<T> creator) {
    String finalObservableName = scopeObservableName(observableName, nameScope);
    
    log.formatInfo("Getting event observable %s with creator", finalObservableName);
   
    T observable = getObservable(finalObservableName);

    if (observable == null) {
      observable = creator.newObservable();

      registerObservable(finalObservableName, observable);
    }

    return observable;
  }

  @Override
  public <T> boolean connectObservers(String observableName, ManagedScope scope,
      BaseObserver<T>... observers) {
    return connectObservers(observableName, NONE, scope, observers);
  }

  @Override
  public <T> boolean connectObservers(String observableName, 
      Option<String> nameScope, ManagedScope scope,
      BaseObserver<T>... observers) {
    String finalObservableName = scopeObservableName(observableName, nameScope);
    
    log.formatInfo("Connecting observers to event observable %s", finalObservableName);
    
    Observable<? extends T> observable = getObservable(finalObservableName);
    if (observable != null) {
      if (observers != null) {
        subscribeScopableObservers(observable, observers, scope);
      }
      return true;
    } else {
      return false;
    }
  }

  @Override
  public synchronized <T> void connectObserversWhenAvailable(String observableName,
      ManagedScope scope, BaseObserver<T>... observers) {
    connectObserversWhenAvailable(observableName, NONE, scope, observers);
  }

  @Override
  public synchronized <T> void connectObserversWhenAvailable(String observableName, 
      Option<String> nameScope,
      ManagedScope scope, BaseObserver<T>... observers) {
    String finalObservableName = scopeObservableName(observableName, nameScope);
    
    log.formatInfo("Connect observers to event observable %s when available", finalObservableName);
    
    if (observers != null) {
      Observable<? extends T> observable = getObservable(finalObservableName);
      if (observable != null) {
        subscribeScopableObservers(observable, observers, scope);
      } else {
        futureObservers.put(finalObservableName,
            new FutureObservers<T>(observableName, scope, observers));
      }
    }
  }

  @Override
  public String scopeObservableName(String observableName, Option<String> nameScope) {
    if (nameScope.isDefined()) {
      return observableName + "." + nameScope.get();
    } else {
      return observableName;
    }
  }

  /**
   * Subscribe a collection of observables to an observer and place them in the
   * supplied scope.
   * 
   * @param observable
   *          the observable
   * @param observers
   *          the observers to connect to the observable
   * @param scope
   *          the scope to place the observers in
   */
  private <T> void subscribeScopableObservers(Observable<? extends T> observable,
      BaseObserver<T>[] observers, ManagedScope scope) {
    for (BaseObserver<T> observer : observers) {
      observable.subscribe(observer);

      scope.addResource(observer);
    }
  }

  /**
   * A collection of observers whose obserbale is not yet registered.
   * 
   * @author Keith M. Hughes
   *
   * @param <T>
   *          The type of the observable argument
   */
  private class FutureObservers<T> {

    /**
     * The name of the observable.
     */
    public final String observableName;

    /**
     * The scope to place the observers in.
     */
    public final ManagedScope scope;

    /**
     * The observers.
     */
    public final BaseObserver<T>[] observers;

    /**
     * Construct a new future.
     * 
     * @param observableName
     *          the name of the observable
     * @param scope
     *          the scope to place the observers in
     * @param observers
     *          the observers
     */
    public FutureObservers(String observableName, ManagedScope scope, BaseObserver<T>[] observers) {
      this.observableName = observableName;
      this.scope = scope;
      this.observers = observers;
    }

    /**
     * Subscribe the observers to the observable.
     */
    public void subscribe() {
      Observable<? extends T> observable = getObservable(observableName);

      subscribeScopableObservers(observable, observers, scope);
    }
  }
}
