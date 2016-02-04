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

package io.smartspaces.service.audio.player.support;

import io.smartspaces.service.audio.player.AudioRepository;
import io.smartspaces.service.audio.player.AudioTrackMetadata;
import io.smartspaces.service.audio.player.FilePlayableAudioTrack;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * A base implementation of an in-memory repository for audio.
 *
 * <p>
 * The {@link #startup()} and {@link #shutdown()} do nothing.
 *
 * @author Keith M. Hughes
 */
public class InMemoryAudioRepository implements AudioRepository {

  /**
   * Map of track ID to playable tracks.
   */
  private final Map<String, FilePlayableAudioTrack> tracks = new HashMap<>();

  @Override
  public void startup() {
    onStartup();
  }

  /**
   * Handle any additional startup.
   *
   * <p>
   * This is meant to be overriden.
   */
  protected void onStartup() {
    // Default is do nothing.
  }

  @Override
  public void shutdown() {
    onShutdown();
  }

  /**
   * Handle any additional shutdown.
   *
   * <p>
   * This is meant to be overriden.
   */
  protected void onShutdown() {
    // Default is do nothing.
  }

  @Override
  public AudioTrackMetadata getTrackMetadata(String id) {
    FilePlayableAudioTrack ptrack = getPlayableTrack(id);
    if (ptrack != null) {
      return ptrack.getMetadata();
    } else {
      return null;
    }
  }

  @Override
  public FilePlayableAudioTrack getPlayableTrack(String id) {
    return tracks.get(id);
  }

  @Override
  public Collection<FilePlayableAudioTrack> getAllPlayableTracks() {
    return Lists.newArrayList(tracks.values());
  }

  /**
   * Add in a new track.
   *
   * @param track
   *          the track to add
   */
  @VisibleForTesting
  public void addTrack(FilePlayableAudioTrack track) {
    tracks.put(track.getMetadata().getId(), track);
  }
}
