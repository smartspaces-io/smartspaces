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

package io.smartspaces.service.audio.player.jukebox.support;

import io.smartspaces.logging.ExtendedLog;

/**
 * A base {@link JukeboxOperation} which provides some support for
 * implementations.
 *
 * @author Keith M. Hughes
 */
public abstract class BaseJukeboxOperation implements JukeboxOperation {

  /**
   * The jukebox the operation is for.
   */
  protected final InternalAudioJukebox audioJukebox;

  /**
   * Log where all should be written.
   */
  protected final ExtendedLog log;

  /**
   * Construct the jukebox operation.
   *
   * @param audioJukebox
   *          the jukebox running this operation
   * @param log
   *          the logger to use
   */
  public BaseJukeboxOperation(InternalAudioJukebox audioJukebox, ExtendedLog log) {
    this.audioJukebox = audioJukebox;
    this.log = log;
  }
}
