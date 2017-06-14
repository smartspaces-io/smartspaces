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

package io.smartspaces.activity.configuration;

import io.smartspaces.SimpleSmartSpacesException;
import io.smartspaces.activity.Activity;
import io.smartspaces.activity.ActivityResourceConfigurator;
import io.smartspaces.configuration.Configuration;
import io.smartspaces.service.web.server.WebServer;
import io.smartspaces.system.SmartSpacesEnvironment;
import io.smartspaces.util.io.FileSupport;
import io.smartspaces.util.io.FileSupportImpl;

import java.io.File;

/**
 * Activity resource configurator for web servers.
 *
 * @author Keith M. Hughes
 */
public class WebServerActivityResourceConfigurator
    implements ActivityResourceConfigurator<WebServer> {

  /**
   * Configuration property suffix giving the port the web server should be
   * started on.
   */
  public static final String CONFIGURATION_NAME_SUFFIX_WEBAPP_WEB_SERVER_PORT = ".web.server.port";

  /**
   * Configuration property suffix giving the websocket URI for the web server
   * on.
   */
  public static final String CONFIGURATION_NAME_SUFFIX_WEBAPP_WEB_SERVER_WEBSOCKET_URI =
      ".web.server.websocket.uri";

  /**
   * Configuration property suffix giving location of the webapp content.
   * Relative paths give relative to app install directory.
   */
  public static final String CONFIGURATION_NAME_SUFFIX_WEBAPP_CONTENT_LOCATION =
      ".content.location";

  /**
   * The default value for the webapp content location.
   */
  public static final String CONFIGURATION_VALUE_DEFAULT_WEBAPP_CONTENT_LOCATION = "webapp";

  /**
   * Configuration property suffix for enabling cross origin content.
   */
  public static final String CONFIGURATION_NAME_SUFFIX_WEBAPP_ENABLE_CROSS_ORIGIN =
      ".enable.cross.origin";

  /**
   * Configuration property suffix for whether the server is secure or not.
   */
  public static final String CONFIGURATION_NAME_SUFFIX_WEBAPP_WEB_SERVER_SECURE =
      ".web.server.secure";

  /**
   * Configuration property suffix for obtaining the file path for the SSL
   * certificate file.
   */
  public static final String CONFIGURATION_NAME_SUFFIX_WEBAPP_SSL_CERTIFICATE = ".ssl.certificate";

  /**
   * Configuration property suffix for obtaining the file path for the SSL
   * private key.
   */
  public static final String CONFIGURATION_NAME_SUFFIX_WEBAPP_SSL_PRIVATE_KEY = ".ssl.privatekey";

  /**
   * Default port to give to the web server.
   */
  public static final int CONFIGURATION_VALUE_DEFAULT_WEB_SERVER_PORT = 9000;

  /**
   * Host identifier to use if not specified in configuration.
   */
  public static final String CONFIGURATION_VALUE_DEFAULT_WEB_SERVER_HOST = "localhost";

  /**
   * Cross-origin enable if not specified in configuration.
   */
  public static final boolean CONFIGURATION_VALUE_DEFAULT_WEB_SERVER_ENABLE_CROSS_ORIGIN = true;

  /**
   * The base URL that this web server works with (hostname and port).
   */
  private String webBaseUrl;

  /**
   * URL for the web activity.
   */
  private String webContentUrl;

  /**
   * Port the web server will run on.
   */
  private int webServerPort;

  /**
   * The path to the web content. This is the absolute path portion of the URL.
   */
  private String webContentPath;

  /**
   * The base directory of the web content being served.
   */
  private File webContentBaseDir;

  /**
   * Prefix of the URI for the web socket connections.
   */
  private String webSocketUriPrefix;

  /**
   * The full URL for the web server's initial page.
   */
  private String webInitialPage;

  /**
   * The cross origin policy for this server.
   */
  private boolean crossOriginAllowed;

  /**
   * The file support for the configurator.
   */
  private FileSupport fileSupport;

  /**
   * Create a resource configurator with the standard file support.
   */
  public WebServerActivityResourceConfigurator() {
    this(FileSupportImpl.INSTANCE);
  }

  /**
   * Create a configurator where the file support is supplied.
   * 
   * @param fileSupport
   *          the file support to use
   */
  public WebServerActivityResourceConfigurator(FileSupport fileSupport) {
    this.fileSupport = fileSupport;
  }

  @Override
  public void configure(String resourceName, Activity activity, WebServer webServer) {
    resourceName = resourceName != null ? resourceName.trim() : "";
    String configurationPrefix =
        resourceName.isEmpty() ? WebActivityConfiguration.CONFIGURATION_NAME_PREFIX_WEBAPP
            : WebActivityConfiguration.CONFIGURATION_NAME_PREFIX_WEBAPP + "." + resourceName;

    Configuration configuration = activity.getConfiguration();

    webSocketUriPrefix = configuration.getPropertyString(
        configurationPrefix + CONFIGURATION_NAME_SUFFIX_WEBAPP_WEB_SERVER_WEBSOCKET_URI);

    webServerPort = configuration.getPropertyInteger(
        configurationPrefix + CONFIGURATION_NAME_SUFFIX_WEBAPP_WEB_SERVER_PORT,
        CONFIGURATION_VALUE_DEFAULT_WEB_SERVER_PORT);
    webServer.setPort(webServerPort);

    String serverName = (resourceName.isEmpty()) ? String.format("%sWebServer", activity.getName())
        : String.format("%s_%s_WebServer", activity.getName(), resourceName);
    webServer.setServerName(serverName);

    boolean secure = configuration.getPropertyBoolean(
        configurationPrefix + CONFIGURATION_NAME_SUFFIX_WEBAPP_WEB_SERVER_SECURE, false);
    webServer.setSecureServer(secure);

    if (secure) {
      String certificatePath = configuration.getPropertyString(
          configurationPrefix + CONFIGURATION_NAME_SUFFIX_WEBAPP_SSL_CERTIFICATE);
      String privateKeyPath = configuration.getPropertyString(
          configurationPrefix + CONFIGURATION_NAME_SUFFIX_WEBAPP_SSL_PRIVATE_KEY);

      boolean hasCertificatePath = certificatePath != null && !certificatePath.trim().isEmpty();
      boolean hasPrivateKeyPath = privateKeyPath != null && !privateKeyPath.trim().isEmpty();

      if (hasCertificatePath && hasPrivateKeyPath) {
        webServer.setSslCertificates(new File(certificatePath.trim()),
            new File(privateKeyPath.trim()));
      } else if (hasCertificatePath ^ hasPrivateKeyPath) {
        throw new SimpleSmartSpacesException(
            "Both a certificate chain file and a private key file must be supplied.");
      }
    }

    boolean debugMode = configuration
        .getPropertyBoolean(WebActivityConfiguration.CONFIGURATION_NAME_WEBAPP_DEBUG, false);
    webServer.setDebugMode(debugMode);

    crossOriginAllowed = configuration.getPropertyBoolean(
        configurationPrefix + CONFIGURATION_NAME_SUFFIX_WEBAPP_ENABLE_CROSS_ORIGIN,
        CONFIGURATION_VALUE_DEFAULT_WEB_SERVER_ENABLE_CROSS_ORIGIN);

    String webServerHost =
        configuration.getPropertyString(SmartSpacesEnvironment.CONFIGURATION_NAME_HOST_ADDRESS,
            CONFIGURATION_VALUE_DEFAULT_WEB_SERVER_HOST);

    webContentPath = "/" + activity.getName();
    webBaseUrl = ((secure) ? "https" : "http") + "://" + webServerHost + ":" + webServerPort;
    webContentUrl = webBaseUrl + webContentPath;

    StringBuilder webInitialPageBuilder = new StringBuilder();
    webInitialPageBuilder.append(webContentUrl).append(WebActivityConfiguration.WEB_PATH_SEPARATOR)
        .append(configuration.getPropertyString(
            configurationPrefix + WebActivityConfiguration.CONFIGURATION_NAME_SUFFIX_INITIAL_PAGE,
            WebActivityConfiguration.DEFAULT_INITIAL_PAGE));

    String queryString = configuration.getPropertyString(configurationPrefix
        + WebActivityConfiguration.CONFIGURATION_NAME_SUFFIX_INITIAL_URL_QUERY_STRING);
    if (queryString != null) {
      webInitialPageBuilder.append(WebActivityConfiguration.WEB_QUERY_STRING_SEPARATOR)
          .append(queryString.trim());
    }

    webInitialPage = webInitialPageBuilder.toString();

    String contentLocation = configuration.getPropertyString(
        configurationPrefix + CONFIGURATION_NAME_SUFFIX_WEBAPP_CONTENT_LOCATION,
        CONFIGURATION_VALUE_DEFAULT_WEBAPP_CONTENT_LOCATION);
    webContentBaseDir = fileSupport
        .resolveFile(activity.getActivityFilesystem().getInstallDirectory(), contentLocation);

    if (fileSupport.isDirectory(webContentBaseDir)) {
      activity.getLog().formatInfo(
          "Adding static content directory for base webapp to webserver: %s",
          webContentBaseDir.getAbsolutePath());
      webServer.addStaticContentHandler(webContentPath, webContentBaseDir);
      System.out.println(webContentBaseDir);
    } else if (fileSupport.isFile(webContentBaseDir)) {
      activity.getLog().formatWarn(
          "Static content directory for base webapp for webserver is not a directory: %s",
          webContentBaseDir.getAbsolutePath());
    }
  }

  /**
   * Get the base URL for this server.
   *
   * @return the base URL for the web server
   */
  public String getWebBaseUrl() {
    return webBaseUrl;
  }

  /**
   * Get the main URL for web content for the server.
   *
   * @return the main URL for web content
   */
  public String getWebContentUrl() {
    return webContentUrl;
  }

  /**
   * Get the port used by the web server.
   *
   * @return the web server port
   */
  public int getWebServerPort() {
    return webServerPort;
  }

  /**
   * Get the path for web content.
   *
   * @return the path for web content
   */
  public String getWebContentPath() {
    return webContentPath;
  }

  /**
   * Get the base directory for web content in the file system.
   *
   * @return the base directory for web content in the file system
   */
  public File getWebContentBaseDir() {
    return webContentBaseDir;
  }

  /**
   * Get the URI prefix for web socket connections.
   *
   * @return the URI prefix for web socket connections
   */
  public String getWebSocketUriPrefix() {
    return webSocketUriPrefix;
  }

  /**
   * Set the URI prefix for web socket connections.
   *
   * @param webSocketUriPrefix
   *          the URI prefix for web socket connections
   */
  public void setWebSocketUriPrefix(String webSocketUriPrefix) {
    this.webSocketUriPrefix = webSocketUriPrefix;
  }

  /**
   * Get the full URL for the web server's initial page.
   *
   * @return the full URL for the web server's initial page
   */
  public String getWebInitialPage() {
    return webInitialPage;
  }

  /**
   * @return {@code true} if cross origin content should be allowed.
   */
  public boolean getCrossOriginAllowed() {
    return crossOriginAllowed;
  }

}
