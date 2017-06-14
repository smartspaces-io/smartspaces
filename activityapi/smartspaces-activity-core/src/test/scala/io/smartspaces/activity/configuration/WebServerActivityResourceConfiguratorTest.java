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

import io.smartspaces.activity.Activity;
import io.smartspaces.activity.ActivityFilesystem;
import io.smartspaces.configuration.SimpleConfiguration;
import io.smartspaces.logging.ExtendedLog;
import io.smartspaces.service.web.server.WebServer;
import io.smartspaces.util.io.FileSupport;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.File;

/**
 * Tests for the {@link WebServerActivityResourceConfigurator}.
 *
 * @author Keith M. Hughes
 */
public class WebServerActivityResourceConfiguratorTest {
  private WebServerActivityResourceConfigurator configurator;
  private FileSupport fileSupport;
  private WebServer webServer;
  private Activity activity;
  private SimpleConfiguration configuration;
  private ActivityFilesystem filesystem;
  private File installDirectory = new File("test").getAbsoluteFile();
  private String activityName = "testActivity";
  private ExtendedLog log;

  @Before
  public void setup() {
    fileSupport = Mockito.mock(FileSupport.class);
    configurator = new WebServerActivityResourceConfigurator(fileSupport);
    webServer = Mockito.mock(WebServer.class);
    activity = Mockito.mock(Activity.class);

    log = Mockito.mock(ExtendedLog.class);
    
    Mockito.when(activity.getName()).thenReturn(activityName);
    Mockito.when(activity.getLog()).thenReturn(log);

    filesystem = Mockito.mock(ActivityFilesystem.class);
    Mockito.when(activity.getActivityFilesystem()).thenReturn(filesystem);
    Mockito.when(filesystem.getInstallDirectory()).thenReturn(installDirectory);

    configuration = SimpleConfiguration.newConfiguration();
    Mockito.when(activity.getConfiguration()).thenReturn(configuration);
  }

  /**
   * Test a normal configure.
   */
  @Test
  public void testConfigure() {
    int testPort = WebServerActivityResourceConfigurator.CONFIGURATION_VALUE_DEFAULT_WEB_SERVER_PORT + 100;
    String webSocketUri = "foo/bar/bletch";
    String initialUrl = "snafu";
    String query = "oorgle";
    String contentLocation = "webapp";

    configuration.setProperty("space.activity.webapp.web.server.port", Integer.toString(testPort));
    configuration.setProperty("space.activity.webapp.content.location", contentLocation);
    configuration.setProperty("space.activity.webapp.web.server.websocket.uri", webSocketUri);
    configuration.setProperty("space.activity.webapp.url.initial", initialUrl);
    configuration.setProperty("space.activity.webapp.url.query_string", query);
    
    File contentDir = new File(installDirectory, contentLocation);
    Mockito.when(fileSupport.resolveFile(installDirectory, contentLocation)).thenReturn(contentDir);
    Mockito.when(fileSupport.isDirectory(contentDir)).thenReturn(true);

    configurator.configure(null, activity, webServer);

    Mockito.verify(webServer).setPort(testPort);
    Mockito.verify(webServer).addStaticContentHandler("/" + activityName, contentDir);

    Assert.assertEquals(webSocketUri, configurator.getWebSocketUriPrefix());
    String webBaseUrl = "http://localhost:" + testPort;
    String webContentUrl = webBaseUrl + "/" + activityName;
    Assert.assertEquals(webBaseUrl, configurator.getWebBaseUrl());
    Assert.assertEquals(webContentUrl, configurator.getWebContentUrl());
    Assert.assertEquals(webContentUrl + "/" + initialUrl + "?" + query,
        configurator.getWebInitialPage());
  }

  /**
   * Test an https configure.
   */
  @Test
  public void testHttpsConfigure() {
    int testPort = WebServerActivityResourceConfigurator.CONFIGURATION_VALUE_DEFAULT_WEB_SERVER_PORT + 100;
    String webSocketUri = "foo/bar/bletch";
    String initialUrl = "snafu";
    String query = "oorgle";
    String contentLocation = "webapp";

    configuration.setProperty("space.activity.webapp.web.server.port", Integer.toString(testPort));
    configuration.setProperty("space.activity.webapp.content.location", contentLocation);
    configuration.setProperty("space.activity.webapp.web.server.websocket.uri", webSocketUri);
    configuration.setProperty("space.activity.webapp.url.initial", initialUrl);
    configuration.setProperty("space.activity.webapp.url.query_string", query);
    configuration.setProperty("space.activity.webapp.web.server.secure", "true");

    File contentDir = new File(installDirectory, contentLocation);
    Mockito.when(fileSupport.resolveFile(installDirectory, contentLocation)).thenReturn(contentDir);
    Mockito.when(fileSupport.isDirectory(contentDir)).thenReturn(true);
    
    configurator.configure(null, activity, webServer);

    Mockito.verify(webServer).setPort(testPort);
    Mockito.verify(webServer).addStaticContentHandler("/" + activityName, contentDir);

    Assert.assertEquals(webSocketUri, configurator.getWebSocketUriPrefix());
    String webBaseUrl = "https://localhost:" + testPort;
    String webContentUrl = webBaseUrl + "/" + activityName;
    Assert.assertEquals(webBaseUrl, configurator.getWebBaseUrl());
    Assert.assertEquals(webContentUrl, configurator.getWebContentUrl());
    Assert.assertEquals(webContentUrl + "/" + initialUrl + "?" + query,
        configurator.getWebInitialPage());
  }
}
