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

package io.smartspaces.service.web.server.internal.netty;

import java.io.InputStream;
import java.util.concurrent.ScheduledExecutorService;

import io.smartspaces.logging.ExtendedLog;
import io.smartspaces.service.web.server.WebServer;
import io.smartspaces.service.web.server.WebServerService;
import io.smartspaces.service.web.server.internal.BaseWebServerService;
import io.smartspaces.system.SmartSpacesEnvironment;
import io.smartspaces.util.io.FileSupport;
import io.smartspaces.util.io.FileSupportImpl;
import io.smartspaces.util.web.MapExtensionMimeResolver;
import io.smartspaces.util.web.MimeResolver;

/**
 * A {@link WebServerService} which gives NETTY web servers.
 *
 * @author Keith M. Hughes
 */
public class NettyWebServerService extends BaseWebServerService {

  /**
   * Where the default MIME types are found.
   */
  public static final String BUNDLE_LOCATION_WEB_SERVER_MIME_TYPES =
      "io/smartspaces/util/web/mime.types";

  /**
   * The line splitter regex for the internal MIME file.
   */
  public static final String MIME_TYPE_LINE_SPLITTER_REGEX = "\\r?\\n";

  /**
   * The regex for splitting lines of the internal MIME file into their
   * component pieces.
   */
  public static final String MIME_FILE_COMPONENT_SPLITTING_REGEX = "\\s+";

  /**
   * The comment character for the internal MIME file.
   */
  public static final String MIME_FILE_COMMENT_CHARACTER = "#";

  /**
   * The default HTTP MIME resolver to use.
   */
  private MimeResolver defaultHttpMimeResolver;

  /**
   * The file support to use.
   */
  private FileSupport fileSupport = FileSupportImpl.INSTANCE;

  @Override
  public String getName() {
    return WebServerService.SERVICE_NAME;
  }

  @Override
  public void startup() {
    // Because of tests.
    SmartSpacesEnvironment spaceEnvironment = getSpaceEnvironment();
    defaultHttpMimeResolver =
        newDefaultMimeResolver(spaceEnvironment != null ? spaceEnvironment.getLog() : null);
  }

  @Override
  public synchronized WebServer newWebServer(String serverName, int port, ExtendedLog log) {
    WebServer server = newWebServer(log);

    server.setPort(port);
    server.setServerName(serverName);

    return server;
  }

  @Override
  public synchronized WebServer newWebServer(ExtendedLog log) {
    ScheduledExecutorService threadPool = getSpaceEnvironment().getExecutorService();

    WebServer server = new NettyWebServer(threadPool, threadPool, log);
    server.setDefaultMimeResolver(getDefaultHttpMimeResolver());

    addServer(server);

    return server;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T extends MimeResolver> T getDefaultHttpMimeResolver() {
    return (T) defaultHttpMimeResolver;
  }

  @Override
  public void setDefaultHttpMimeResolver(MimeResolver resolver) {
    defaultHttpMimeResolver = resolver;
  }

  /**
   * Create a new MIME resolver which maps standard MIME types.
   *
   * @param log
   *          the logger to use while creating the resolver.
   *
   * @return the resolver
   */
  private MapExtensionMimeResolver newDefaultMimeResolver(ExtendedLog log) {
    MapExtensionMimeResolver resolver = new MapExtensionMimeResolver(log);

    try {
      InputStream mimeResource = MapExtensionMimeResolver.class.getClassLoader()
          .getResourceAsStream(BUNDLE_LOCATION_WEB_SERVER_MIME_TYPES);
      if (mimeResource != null) {
        if (log != null) {
          log.info(String.format("Loading internal MIME file %s",
                  BUNDLE_LOCATION_WEB_SERVER_MIME_TYPES));
        }
        String mimeFile = fileSupport.inputStreamAsString(mimeResource);
        String[] lines = mimeFile.split(MIME_TYPE_LINE_SPLITTER_REGEX);
        for (String line : lines) {
          line = line.trim();
          if (!line.isEmpty() && !line.startsWith(MIME_FILE_COMMENT_CHARACTER)) {
            String[] parts = line.split(MIME_FILE_COMPONENT_SPLITTING_REGEX);
            if (parts.length >= 2) {
              String mimeType = parts[0];
              for (int i = 1; i < parts.length; i++) {
                resolver.addMimeType(parts[i], mimeType);
              }
            }
          }
        }
      } else {
        if (log != null) {
          log.warn(String.format("Could not read MIME file %s. MIME resolver is empty",
                  BUNDLE_LOCATION_WEB_SERVER_MIME_TYPES));
        }
      }
    } catch (Exception e) {
      if (log != null) {
        log.warn(String.format("Could not read MIME file %s. MIME resolver is empty",
                BUNDLE_LOCATION_WEB_SERVER_MIME_TYPES), e);
      }
    }
    return resolver;
  }

}
