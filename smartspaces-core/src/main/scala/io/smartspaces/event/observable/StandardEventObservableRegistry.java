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

import io.smartspaces.scope.ManagedScope;

import io.reactivex.Observable;

import java.util.HashMap;
import java.util.Map;

/**
 * The standard event observable registry.
 * 
 * @author Keith M. Hughes
 */
public class StandardEventObservableRegistry implements EventObservableRegistry {

  /**
   * The map of observables.
   */
  private Map<String, Observable<?>> observables = new HashMap<>();

  @Override
  public synchronized EventObservableRegistry registerObservable(String observableName,
      Observable<?> observable) {
    observables.put(observableName, observable);

    return this;
  }

  @Override
  public synchronized EventObservableRegistry unregisterObservable(String observableName) {
    observables.remove(observableName);

    return this;
  }

  @Override
  @SuppressWarnings("unchecked")
  public synchronized <T extends Observable<?>> T getObservable(String observableName) {
    return (T) observables.get(observableName);
  }

  @Override
  public synchronized <T extends Observable<?>> T getObservable(String observableName,
      ObservableCreator<T> creator) {
    T observable = getObservable(observableName);

    if (observable == null) {
      observable = creator.newObservable();

      registerObservable(observableName, observable);
    }

    return observable;
  }

  @Override
  public <T> boolean connectObservers(String observableName, ManagedScope scope,
      BaseObserver<T>... observers) {
    Observable<? extends T> observable = getObservable(observableName);
    if (observable != null) {
      if (observers != null) {
        for (BaseObserver<T> observer : observers) {
          observable.subscribe(observer);

          scope.addResource(observer);
        }
      }
      return true;
    } else {
      return false;
    }
  }

}
