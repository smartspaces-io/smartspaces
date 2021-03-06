/**
 * Copyright (C) 2016 Keith M. Hughes
 * Copyright 2016 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See
 * the License for the specific language governing permissions and limitations under the License.
 */

package io.smartspaces.event.observable;

import io.smartspaces.logging.ExtendedLog;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.plugins.RxJavaPlugins;
import io.reactivex.subjects.Subject;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Subject that, once an {@link Observer} has subscribed, emits all subsequently
 * observed items to the subscriber.
 * 
 * <p>
 * It logs when a subscriber throws an exception and continues on to the next subscriber.
 * 
 * <p>
 * Example usage:
 * <p>
 * 
 * <pre>
 * {
 *   &#64;code
 *   PublishSubject<Object> subject = PublishSubject.create();
 *   // observer1 will receive all onNext and onComplete events
 *   subject.subscribe(observer1);
 *   subject.onNext("one");
 *   subject.onNext("two");
 *   // observer2 will only receive "three" and onComplete
 *   subject.subscribe(observer2);
 *   subject.onNext("three");
 *   subject.onComplete();
 * }
 * </pre>
 *
 * @param <T>
 *          the type of items observed and emitted by the Subject
 */
public class EventPublisherSubject<T> extends Subject<T> {
  /** The terminated indicator for the subscribers array. */
  @SuppressWarnings("rawtypes")
  static final PublishDisposable[] TERMINATED = new PublishDisposable[0];
  /** An empty subscribers array to avoid allocating it all the time. */
  @SuppressWarnings("rawtypes")
  static final PublishDisposable[] EMPTY = new PublishDisposable[0];

  /** The array of currently subscribed subscribers. */
  final AtomicReference<PublishDisposable<T>[]> subscribers;

  /**
   * The error, write before terminating and read after checking subscribers.
   */
  Throwable error;

  /**
   * The logger to use.
   */
  private ExtendedLog log;

  /**
   * Constructs a EventPublisherSubject.
   * 
   * @param log
   *          the logger for the subject
   * @param <T>
   *          the value type
   * 
   * @return the new PublishSubject
   */
  public static <T> EventPublisherSubject<T> create(ExtendedLog log) {
    return new EventPublisherSubject<T>(log);
  }

  /**
   * Constructs a PublishSubject.
   */
  @SuppressWarnings("unchecked")
  EventPublisherSubject(ExtendedLog log) {
    this.log = log;

    subscribers = new AtomicReference<PublishDisposable<T>[]>(EMPTY);
  }

  @Override
  public void subscribeActual(Observer<? super T> t) {
    PublishDisposable<T> ps = new PublishDisposable<T>(t, this);
    t.onSubscribe(ps);
    if (add(ps)) {
      // if cancellation happened while a successful add, the remove()
      // didn't work
      // so we need to do it again
      if (ps.isDisposed()) {
        remove(ps);
      }
    } else {
      Throwable ex = error;
      if (ex != null) {
        t.onError(ex);
      } else {
        t.onComplete();
      }
    }
  }

  /**
   * Tries to add the given subscriber to the subscribers array atomically or
   * returns false if the subject has terminated.
   * 
   * @param ps
   *          the subscriber to add
   * @return true if successful, false if the subject has terminated
   */
  boolean add(PublishDisposable<T> ps) {
    for (;;) {
      PublishDisposable<T>[] a = subscribers.get();
      if (a == TERMINATED) {
        return false;
      }

      int n = a.length;
      @SuppressWarnings("unchecked")
      PublishDisposable<T>[] b = new PublishDisposable[n + 1];
      System.arraycopy(a, 0, b, 0, n);
      b[n] = ps;

      if (subscribers.compareAndSet(a, b)) {
        return true;
      }
    }
  }

  /**
   * Atomically removes the given subscriber if it is subscribed to the subject.
   * 
   * @param ps
   *          the subject to remove
   */
  @SuppressWarnings("unchecked")
  void remove(PublishDisposable<T> ps) {
    for (;;) {
      PublishDisposable<T>[] a = subscribers.get();
      if (a == TERMINATED || a == EMPTY) {
        return;
      }

      int n = a.length;
      int j = -1;
      for (int i = 0; i < n; i++) {
        if (a[i] == ps) {
          j = i;
          break;
        }
      }

      if (j < 0) {
        return;
      }

      PublishDisposable<T>[] b;

      if (n == 1) {
        b = EMPTY;
      } else {
        b = new PublishDisposable[n - 1];
        System.arraycopy(a, 0, b, 0, j);
        System.arraycopy(a, j + 1, b, j, n - j - 1);
      }
      if (subscribers.compareAndSet(a, b)) {
        return;
      }
    }
  }

  @Override
  public void onSubscribe(Disposable s) {
    if (subscribers.get() == TERMINATED) {
      s.dispose();
    }
  }

  @Override
  public void onNext(T t) {
    if (subscribers.get() == TERMINATED) {
      return;
    }
    if (t == null) {
      onError(new NullPointerException(
          "onNext called with null. Null values are generally not allowed in 2.x operators and sources."));
      return;
    }
    for (PublishDisposable<T> s : subscribers.get()) {
      try {
        s.onNext(t);
      } catch (Throwable e) {
        log.error("Error during onNext", e);
      }
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public void onError(Throwable t) {
    if (subscribers.get() == TERMINATED) {
      RxJavaPlugins.onError(t);
      return;
    }
    if (t == null) {
      t = new NullPointerException(
          "onError called with null. Null values are generally not allowed in 2.x operators and sources.");
    }
    error = t;

    for (PublishDisposable<T> s : subscribers.getAndSet(TERMINATED)) {
      try {
        s.onError(t);
      } catch (Throwable e) {
        log.error("Error during onError", e);
      }
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public void onComplete() {
    if (subscribers.get() == TERMINATED) {
      return;
    }
    for (PublishDisposable<T> s : subscribers.getAndSet(TERMINATED)) {
      try {
        s.onComplete();
      } catch (Throwable e) {
        log.error("Error during onComplete", e);
      }
    }
  }

  @Override
  public boolean hasObservers() {
    return subscribers.get().length != 0;
  }

  @Override
  public Throwable getThrowable() {
    if (subscribers.get() == TERMINATED) {
      return error;
    }
    return null;
  }

  @Override
  public boolean hasThrowable() {
    return subscribers.get() == TERMINATED && error != null;
  }

  @Override
  public boolean hasComplete() {
    return subscribers.get() == TERMINATED && error == null;
  }

  /**
   * Wraps the actual subscriber, tracks its requests and makes cancellation to
   * remove itself from the current subscribers array.
   *
   * @param <T>
   *          the value type
   */
  static final class PublishDisposable<T> extends AtomicBoolean implements Disposable {

    private static final long serialVersionUID = 3562861878281475070L;
    /** The actual subscriber. */
    final Observer<? super T> actual;
    /** The subject state. */
    final EventPublisherSubject<T> parent;

    /**
     * Constructs a PublishSubscriber, wraps the actual subscriber and the
     * state.
     * 
     * @param actual
     *          the actual subscriber
     * @param parent
     *          the parent PublishProcessor
     */
    PublishDisposable(Observer<? super T> actual, EventPublisherSubject<T> parent) {
      this.actual = actual;
      this.parent = parent;
    }

    public void onNext(T t) {
      if (!get()) {
        actual.onNext(t);
      }
    }

    public void onError(Throwable t) {
      if (get()) {
        RxJavaPlugins.onError(t);
      } else {
        actual.onError(t);
      }
    }

    public void onComplete() {
      if (!get()) {
        actual.onComplete();
      }
    }

    @Override
    public void dispose() {
      if (compareAndSet(false, true)) {
        parent.remove(this);
      }
    }

    @Override
    public boolean isDisposed() {
      return get();
    }
  }
}