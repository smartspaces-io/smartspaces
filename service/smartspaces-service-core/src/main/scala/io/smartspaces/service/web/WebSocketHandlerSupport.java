/*
 * Copyright (C) 2016 Keith M. Hughes
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

package io.smartspaces.service.web;

/**
 * A useful support class for {@link WebSocketMessageHandler} instances
 *
 * @author Keith M. Hughes
 */
public class WebSocketHandlerSupport<M> implements WebSocketMessageHandler<M> {

 
  @Override
  public void onConnect() {
    // Default is nothing.
  }

  @Override
  public void onClose() {
    // Default is nothing.
  }

  @Override
  public void onNewMessage(M message) {
    // Default is nothing.
  }
}
