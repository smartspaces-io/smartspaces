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

import com.sun.speech.freetts.VoiceManager;

import io.smartspaces.logging.ExtendedLog;
import io.smartspaces.service.BaseSupportedService;
import io.smartspaces.service.speech.synthesis.SpeechSynthesisPlayer;
import io.smartspaces.service.speech.synthesis.SpeechSynthesisService;

/**
 * A speech synthesis service based on FreeTTS.
 *
 * @author Keith M. Hughes
 */
public class FreeTtsSpeechSynthesisService extends BaseSupportedService implements
    SpeechSynthesisService {

  /**
   * The voice manager for getting voices.
   */
  private VoiceManager voiceManager;

  @Override
  public String getName() {
    return SpeechSynthesisService.SERVICE_NAME;
  }

  @Override
  public void startup() {
    System.setProperty("freetts.voices",
        "com.sun.speech.freetts.en.us.cmu_us_kal.KevinVoiceDirectory");
    voiceManager = VoiceManager.getInstance();
  }

  @Override
  public SpeechSynthesisPlayer newPlayer(ExtendedLog log) {
    return new FreeTtsSpeechSynthesisPlayer(voiceManager, getSpaceEnvironment()
        .getExecutorService(), log);
  }

  @Override
  public SpeechSynthesisPlayer newPlayer() {
    return newPlayer(getSpaceEnvironment().getLog());
  }
}
