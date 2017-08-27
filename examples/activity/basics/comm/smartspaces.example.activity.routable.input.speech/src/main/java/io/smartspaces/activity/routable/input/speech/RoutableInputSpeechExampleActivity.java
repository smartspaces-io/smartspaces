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

package io.smartspaces.activity.routable.input.speech;

import java.util.Map;

import io.smartspaces.activity.impl.route.BaseRoutableActivity;
import io.smartspaces.service.speech.synthesis.SpeechSynthesisPlayer;
import io.smartspaces.service.speech.synthesis.SpeechSynthesisService;

/**
 * An example Speech synthesis activity which listens on a route and speaks what
 * was sent.
 * 
 * @author Keith M. Hughes
 */
public class RoutableInputSpeechExampleActivity extends BaseRoutableActivity {

  /**
   * The speech player.
   */
  private SpeechSynthesisPlayer speechPlayer;

  @Override
  public void onActivitySetup() {
    SpeechSynthesisService speechSynthesisService = getSpaceEnvironment().getServiceRegistry()
        .getRequiredService(SpeechSynthesisService.SERVICE_NAME);

    speechPlayer = speechSynthesisService.newPlayer();

    addManagedResource(speechPlayer);
  }

  @Override
  public void onNewRouteMessage(String channelId, Map<String, Object> message) {
    if (isActivated() && "speech".equals(channelId)) {
      String toSpeak = (String) message.get("message");
      if (toSpeak != null) {
        speechPlayer.speak(toSpeak, true);
      }
    }
  }
}
