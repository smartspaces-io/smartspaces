/*
 * Copyright (C) 2016 Keith M. Hughes
 * Copyright (C) 2013 Google Inc.
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

package io.smartspaces.service.image.video;

import io.smartspaces.resource.managed.ManagedResource;

/**
 * A listener for frames captured during with a {@link VideoLoop}.
 *
 * @param <T>
 *          the type of video frame
 *
 * @author Keith M. Hughes
 */
public interface VideoFrameProcessor<T> extends ManagedResource {

  /**
   * A new frame has come in.
   *
   * @param frame
   *          the new frame
   *
   * @return any processing on the frame
   */
  T onNewVideoFrame(T frame);
}
