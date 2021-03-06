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

package io.smartspaces.service.image.vision.opencv;

import org.opencv.core.Mat;
import org.opencv.highgui.VideoCapture;

import io.smartspaces.logging.ExtendedLog;
import io.smartspaces.service.image.video.VideoLoop;
import io.smartspaces.util.SmartSpacesUtilities;

/**
 * A {@link VideoLoop} which grabs video frames using OpenCV and processes them.
 *
 * @author Keith M. Hughes
 */
public class OpenCvVideoLoop extends VideoLoop<Mat> {

  /**
   * Time for the camera to initialize, in milliseconds.
   */
  public static final long CAMERA_INITIALIZATION_TIME = 1000;

  /**
   * The IS of the camera to use.
   */
  private final int cameraId;

  /**
   * The video capture device.
   */
  private VideoCapture capture;

  /**
   * Logger for the loop.
   */
  private final ExtendedLog log;

  /**
   * Construct a video loop.
   *
   * @param cameraId
   *          ID for the camera to use
   * @param log
   *          logger for the loop
   */
  public OpenCvVideoLoop(int cameraId, ExtendedLog log) {
    this.cameraId = cameraId;
    this.log = log;
  }

  @Override
  protected void setup() {
    capture = new VideoCapture(cameraId);

    // OpenCV sometimes needs a bit of time for the camera to fully initialize.
    SmartSpacesUtilities.delay(CAMERA_INITIALIZATION_TIME);
  }

  @Override
  protected void loop() throws InterruptedException {
    Mat frame = new Mat();
    capture.grab();
    capture.retrieve(frame);
    if (frame.empty()) {
      log.warn("No image");
      return;
    }

    notifyListenersNewVideoFrame(frame);
  }

  @Override
  protected void cleanup() {
    capture.release();
  }

  @Override
  protected void handleException(Exception e) {
    log.error("Error during video loop", e);
  }
}
