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

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import io.smartspaces.logging.ExtendedLog;
import rx.Observable;
import rx.Subscriber;

/**
 * A reactive RXJava observable for Smartspace events.
 *
 * @param <T>
 *          the type of the event
 * 
 * @author Keith M. Hughes
 */
public class EventObservable<T> extends Observable<T> {

  /**
   * The subscribers for the observable.
   */
  private List<Subscriber<? super T>> subscribers;

  /**
   * The logger to use.
   */
  private ExtendedLog log;

  /**
   * Construct a new observable.
   * 
   * @param subscribers
   *          the subscriber list
   * @param log
   *          the logger to use
   */
  public EventObservable(ExtendedLog log) {
    this(new CopyOnWriteArrayList<Subscriber<? super T>>(), log);
  }

  /**
   * Construct a new observable.
   * 
   * @param subscribers
   *          the subscriber list
   * @param log
   *          the logger to use
   */
  private EventObservable(final List<Subscriber<? super T>> subscribers, ExtendedLog log) {
    // This constructor is necessary so that Java will let the list be part of
    // a callback
    // being created in a constructor.
    super(new OnSubscribe<T>() {
      @Override
      public void call(Subscriber<? super T> subscriber) {
        subscribers.add(subscriber);
      }
    });

    this.subscribers = subscribers;
    this.log = log;
  }

  /**
   * Emit an event to all subscribers.
   * 
   * @param event
   *          the event to emit
   * 
   * @return this observable
   */
  public EventObservable<T> emitEvent(T event) {
    for (Subscriber<? super T> subscriber : subscribers) {
      if (!subscriber.isUnsubscribed()) {
        try {
          subscriber.onNext(event);
        } catch (Throwable e) {
          log.error("Error while calling onNext for subscriber", e);
        }
      }
    }

    return this;
  }
}