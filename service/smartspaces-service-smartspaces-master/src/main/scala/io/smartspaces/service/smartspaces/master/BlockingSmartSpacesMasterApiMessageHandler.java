/*
 * Copyright (C) 2016 Keith M. Hughes
 * Copyright (C) 2014 Google Inc.
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

package io.smartspaces.service.smartspaces.master;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import com.google.common.collect.Lists;

/**
 * A master API callback that can be used for blocking calls.
 *
 * @author Keith M. Hughes
 */
public class BlockingSmartSpacesMasterApiMessageHandler implements
    SmartSpacesMasterApiMessageHandler {

  /**
   * The countdown latch for waiting on.
   */
  private CountDownLatch countdown;

  /**
   * The responses for the messages.
   */
  private List<Map<String, Object>> responses = Lists.newArrayList();

  /**
   * Construct a callback that only expects one callback.
   */
  public BlockingSmartSpacesMasterApiMessageHandler() {
    this(1);
  }

  /**
   * Construct a callback that requires multiple callbacks.
   *
   * @param count
   *          the number of callbacks expected
   */
  public BlockingSmartSpacesMasterApiMessageHandler(int count) {
    countdown = new CountDownLatch(count);
  }

  @Override
  public void
      onNewMasterApiMessage(SmartSpacesMasterClient client, Map<String, Object> response) {
    responses.add(response);

    countdown.countDown();
  }

  /**
   * Wait for all requested callbacks.
   *
   * <p>
   * This call blocks forever or until interrupted.
   *
   * @throws InterruptedException
   *           the call was interrupted
   */
  public void await() throws InterruptedException {
    countdown.await();
  }

  /**
   * Wait for all requested callbacks. Will only block for the requested
   * timeout.
   *
   * @param timeout
   *          the time to wait
   * @param unit
   *          the time units for the time to wait
   *
   * @return {@code true} if all callbacks came in within the expected timeout
   *
   * @throws InterruptedException
   *           the call was interrupted
   */
  public boolean await(long timeout, TimeUnit unit) throws InterruptedException {
    return countdown.await(timeout, unit);
  }

  /**
   * Get the responses from the call.
   *
   * @return the responses from the call
   */
  public List<Map<String, Object>> getResponses() {
    return responses;
  }
}
