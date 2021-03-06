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

package io.smartspaces.service.audio.player.internal.binary;

import io.smartspaces.logging.ExtendedLog;
import io.smartspaces.service.BaseSupportedService;
import io.smartspaces.service.audio.player.AudioTrackPlayer;
import io.smartspaces.service.audio.player.AudioTrackPlayerService;
import io.smartspaces.util.process.NativeApplicationRunnerFactory;

/**
 * A {@link AudioTrackPlayerService} which gives track players which are
 * natively run.
 *
 * @author Keith M. Hughes
 */
public class NativeAudioTrackPlayerService extends BaseSupportedService implements
    AudioTrackPlayerService {

  /**
   * The factory for native activity runners.
   */
  private NativeApplicationRunnerFactory runnerFactory;

  /**
   * Construct a new player service.
   *
   * @param runnerFactory
   *          the runner factory to use
   */
  public NativeAudioTrackPlayerService(NativeApplicationRunnerFactory runnerFactory) {
    this.runnerFactory = runnerFactory;
  }

  @Override
  public String getName() {
    return AudioTrackPlayerService.SERVICE_NAME;
  }

  @Override
  public AudioTrackPlayer newTrackPlayer(ExtendedLog log) {
    return new NativeAudioTrackPlayer(runnerFactory, log);
  }
}
