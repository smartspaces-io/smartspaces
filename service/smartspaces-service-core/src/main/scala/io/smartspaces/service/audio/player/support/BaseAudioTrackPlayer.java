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

package io.smartspaces.service.audio.player.support;

import java.util.List;

import com.google.common.collect.Lists;

import io.smartspaces.logging.ExtendedLog;
import io.smartspaces.service.audio.player.AudioTrackPlayer;
import io.smartspaces.service.audio.player.AudioTrackPlayerListener;
import io.smartspaces.service.audio.player.FilePlayableAudioTrack;

/**
 * Useful support for implementations of an {@code AudioTrackPlayer}.
 *
 * @author Keith M. Hughes
 */
public abstract class BaseAudioTrackPlayer implements AudioTrackPlayer {

  /**
   * The log to use.
   */
  protected ExtendedLog log;

  /**
   * All listeners for the player.
   */
  private final List<AudioTrackPlayerListener> listeners = Lists.newCopyOnWriteArrayList();

  /**
   * Construct a base player.
   *
   * @param log
   *          the logger to use
   */
  public BaseAudioTrackPlayer(ExtendedLog log) {
    this.log = log;
  }

  @Override
  public void startup() {
    // Nothing to do
  }

  @Override
  public void shutdown() {
    // Nothing to do
  }

  @Override
  public void addListener(AudioTrackPlayerListener listener) {
    listeners.add(listener);
  }

  @Override
  public void removeListener(AudioTrackPlayerListener listener) {
    listeners.remove(listener);
  }

  /**
   * Notify all listeners that a track has started playing.
   *
   * @param track
   *          the track
   */
  protected void notifyTrackStart(FilePlayableAudioTrack track) {
    for (AudioTrackPlayerListener listener : listeners) {
      try {
        listener.onAudioTrackStart(this, track);
      } catch (Exception e) {
        log.error("Error handling listener track start", e);
      }
    }
  }

  /**
   * Notify all listeners that a track has stoped playing.
   *
   * @param track
   *          the track
   */
  protected void notifyTrackStop(FilePlayableAudioTrack track) {
    for (AudioTrackPlayerListener listener : listeners) {
      try {
        listener.onAudioTrackStop(this, track);
      } catch (Exception e) {
        log.error("Error handling listener track start", e);
      }
    }
  }
}
