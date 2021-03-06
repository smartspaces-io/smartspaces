/*
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

package org.ros.internal.transport.queue;

import org.apache.commons.logging.Log;
import org.ros.concurrent.CancellableLoop;
import org.ros.concurrent.EventDispatcher;
import org.ros.concurrent.ListenerGroup;
import org.ros.concurrent.MessageBlockingQueue;
import org.ros.concurrent.SignalRunnable;
import org.ros.log.RosLogFactory;
import org.ros.message.MessageListener;

import java.util.concurrent.ExecutorService;

/**
 * @author damonkohler@google.com (Damon Kohler)
 *
 * @param <T>
 *          the message type
 */
public class MessageDispatcher<T> extends CancellableLoop {

  private static final Log log = RosLogFactory.getLog(MessageDispatcher.class);

  private final MessageBlockingQueue<LazyMessage<T>> lazyMessages;
  private final ListenerGroup<MessageListener<T>> messageListeners;

  /**
   * Ensures that a messages are not dispatched twice when adding a listener
   * while latch mode is enabled.
   */
  private final Object mutex;

  private boolean latchMode;
  private LazyMessage<T> latchedMessage;

  public MessageDispatcher(MessageBlockingQueue<LazyMessage<T>> lazyMessages,
      ExecutorService executorService) {
    this.lazyMessages = lazyMessages;
    messageListeners = new ListenerGroup<MessageListener<T>>(executorService);
    mutex = new Object();
    latchMode = false;
  }

  /**
   * Adds the specified {@link MessageListener} to the internal
   * {@link ListenerGroup}. If {@link #latchMode} is {@code true}, the
   * {@link #latchedMessage} will be immediately dispatched to the specified
   * {@link MessageListener}.
   *
   * @see ListenerGroup#add(Object, int)
   */
  public void addListener(MessageListener<T> messageListener, int limit) {
    if (log.isDebugEnabled()) {
      log.debug("Adding listener to Message Dispatcher.");
    }
    synchronized (mutex) {
      EventDispatcher<MessageListener<T>> eventDispatcher =
          messageListeners.add(messageListener, limit);
      if (latchMode && latchedMessage != null) {
        eventDispatcher.signal(newSignalRunnable(latchedMessage));
      }
    }
  }

  /**
   * Returns a newly allocated {@link SignalRunnable} for the specified
   * {@link LazyMessage}.
   *
   * @param lazyMessage
   *          the {@link LazyMessage} to signal {@link MessageListener}s with
   * @return the newly allocated {@link SignalRunnable}
   */
  private SignalRunnable<MessageListener<T>> newSignalRunnable(final LazyMessage<T> lazyMessage) {
    return new SignalRunnable<MessageListener<T>>() {
      @Override
      public void run(MessageListener<T> messageListener) {
        messageListener.onNewMessage(lazyMessage.get());
      }
    };
  }

  /**
   * @param enabled
   *          {@code true} if latch mode should be enabled, {@code false}
   *          otherwise
   */
  public void setLatchMode(boolean enabled) {
    latchMode = enabled;
  }

  /**
   * @return {@code true} if latch mode is enabled, {@code false} otherwise
   */
  public boolean getLatchMode() {
    return latchMode;
  }

  @Override
  public void loop() throws InterruptedException {
    LazyMessage<T> lazyMessage = lazyMessages.take();
    synchronized (mutex) {
      latchedMessage = lazyMessage;
      // if (log.isDebugEnabled()) {
      // log.debug("Dispatching message: " + latchedMessage.get());
      // }
      messageListeners.signal(newSignalRunnable(latchedMessage));
    }
  }

  @Override
  protected void handleInterruptedException(InterruptedException e) {
    messageListeners.shutdown();
  }
}