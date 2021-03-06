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

package io.smartspaces.service.speech.synthesis.internal.freetts;

import java.util.concurrent.ScheduledExecutorService;

import com.sun.speech.freetts.Voice;
import com.sun.speech.freetts.VoiceManager;

import io.smartspaces.SmartSpacesException;
import io.smartspaces.logging.ExtendedLog;
import io.smartspaces.service.speech.synthesis.SpeechSynthesisPlayer;

/**
 * A speech synthesis player based on FreeTTS.
 *
 * @author Keith M. Hughes
 */
public class FreeTtsSpeechSynthesisPlayer implements SpeechSynthesisPlayer {

  /**
   * The voice manager to use.
   */
  private VoiceManager voiceManager;

  /**
   * The FreeTTS voice for this player.
   */
  private Voice voice;

  /**
   * Threadpool for this player.
   */
  private ScheduledExecutorService executorService;

  /**
   * The log.
   */
  private ExtendedLog log;

  public FreeTtsSpeechSynthesisPlayer(VoiceManager voiceManager,
      ScheduledExecutorService executorService, ExtendedLog log) {
    this.voiceManager = voiceManager;
    this.executorService = executorService;
    this.log = log;
  }

  @Override
  public void startup() {
    voice = voiceManager.getVoice("kevin16");

    if (voice == null) {
      throw new SmartSpacesException("Cannot find a voice named " + "kevin16"
          + ".  Please specify a different voice.");
    }

    try {
      voice.allocate();
    } catch (Exception e) {
      throw new SmartSpacesException("Could not allocate voice!", e);
    }
  }

  @Override
  public void shutdown() {
    if (voice != null) {
      voice.deallocate();
      voice = null;
    }
  }

  @Override
  public void speak(final String text, boolean sync) {
    if (sync) {
      voice.speak(text);
    } else {
      executorService.submit(new Runnable() {
        @Override
        public void run() {
          voice.speak(text);
        }
      });
    }
  }
}
