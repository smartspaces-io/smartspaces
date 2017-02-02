/*
 * Copyright (C) 2016 Keith M. Hughes
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

package io.smartspaces.interaction.behavior.speech;

import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.logging.Log;

import io.smartspaces.service.speech.synthesis.SpeechSynthesisPlayer;
import io.smartspaces.service.speech.synthesis.SpeechSynthesisService;
import io.smartspaces.system.SmartSpacesEnvironment;

/**
 * The standard speech source that uses routes.
 *
 * @author Keith M. Hughes
 */
class SequentialSpeechSpeaker(private val spaceEnvironment: SmartSpacesEnvironment, private val log: Log) extends SpeechSpeaker {

  /**
   * The queue of text to speak.
   */
  private val speechToSpeak: LinkedBlockingQueue[String] = new LinkedBlockingQueue[String]()

  /**
   * The player for speech.
   */
  private var speechPlayer: SpeechSynthesisPlayer = null

  /**
   * The future for the thread that calls the speech player.
   */
  private var speechFuture: Future[_] = null

  override def startup(): Unit = {
    val speechSynthesisService: SpeechSynthesisService = spaceEnvironment.getServiceRegistry()
      .getRequiredService(SpeechSynthesisService.SERVICE_NAME)
    speechPlayer = speechSynthesisService.newPlayer(log)
    speechPlayer.startup()

    speechFuture = spaceEnvironment.getExecutorService().submit(new Runnable() {
      override def run(): Unit = {
        speakNextContent()
      }
    });
  }

  override def shutdown(): Unit = {
    speechFuture.cancel(true)
    speechPlayer.shutdown()
  }

  override def addSpeech(content: String): Unit = {
    speechToSpeak.offer(content)
  }

  /**
   * Keep speaking until the speaking thread is interrupted.
   *
   * <p>
   * Blocks while waiting for something in the speech queue.
   */
  private def speakNextContent(): Unit = {
    while (!Thread.interrupted()) {
      try {
        val content = speechToSpeak.take()
        speechPlayer.speak(content, true)
      } catch {
        case e: InterruptedException =>
          log.warn("Sequential speech speaker interrupted in speech loop");
      }
    }
  }
}
