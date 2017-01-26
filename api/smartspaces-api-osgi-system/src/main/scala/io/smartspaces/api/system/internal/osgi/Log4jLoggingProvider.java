/*
 * Copyright (C) 2017 Keith M. Hughes
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

package io.smartspaces.api.system.internal.osgi;

import io.smartspaces.system.core.logging.LoggingProvider;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.impl.Log4JLogger;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * Logging provider using Log4J.
 *
 * @author Keith M. Hughes
 */
public class Log4jLoggingProvider implements LoggingProvider {

  /**
   * The base log for the container.
   */
  private Log baseContainerLog;

  /**
   * Configure the provider.
   */
  public void configure() {

    baseContainerLog = LogFactory.getLog("smartspaces");
  }

  @Override
  public Log getLog() {
    return baseContainerLog;
  }

  @Override
  public Log getLog(String logName, String level, String filename) {

    Log log = LogFactory.getLog("smartspaces." + logName);

    return log;
  }

  @Override
  public boolean modifyLogLevel(Log log, String level) {
    
      log.error("Attempt to modify an unmodifiable logger");
 
    return false;
  }

  @Override
  public void releaseLog(Log log) {
    if (log == baseContainerLog) {
      throw new RuntimeException("Cannot release the base container log");
    }
  }
}
