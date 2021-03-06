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

import static org.jboss.netty.channel.Channels.pipeline;

import java.io.File;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelException;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.ChannelGroupFuture;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.codec.http.HttpRequestDecoder;
import org.jboss.netty.handler.codec.http.HttpResponseEncoder;
import org.jboss.netty.handler.ssl.SslContext;
import org.jboss.netty.handler.ssl.util.SelfSignedCertificate;
import org.jboss.netty.handler.stream.ChunkedWriteHandler;

import com.google.common.collect.Lists;

import io.smartspaces.SimpleSmartSpacesException;
import io.smartspaces.SmartSpacesException;
import io.smartspaces.logging.ExtendedLog;
import io.smartspaces.messaging.codec.MessageCodec;
import io.smartspaces.service.web.server.HttpGetRequestHandler;
import io.smartspaces.service.web.server.HttpOptionsRequestHandler;
import io.smartspaces.service.web.server.HttpPostRequestHandler;
import io.smartspaces.service.web.server.HttpStaticContentRequestHandler;
import io.smartspaces.service.web.server.WebServer;
import io.smartspaces.service.web.server.WebServerWebSocketHandlerFactory;
import io.smartspaces.util.net.NetworkBindSimpleSmartSpacesException;
import io.smartspaces.util.web.MimeResolver;

/**
 * A web server based on Netty.
 *
 * @author Keith M. Hughes
 */
public class NettyWebServer implements WebServer {

  /**
   * Name of the server.
   */
  private String serverName;

  /**
   * Port the server should run on.
   */
  private int port;

  /**
   * The server handler for requests.
   */
  private NettyWebServerHandler serverHandler;

  /**
   * Server handler for web sockets.
   */

  private Channel serverChannel;

  /**
   * All channels we know about in the server.
   */
  private ChannelGroup allChannels;

  /**
   * Factory for all channels coming into the server.
   */
  private NioServerSocketChannelFactory channelFactory;

  /**
   * Threadpool for the boss threads.
   */
  private ScheduledExecutorService bossThreadPool;

  /**
   * Threadpool for the worker threads.
   */
  private ScheduledExecutorService workerThreadPool;

  /**
   * HTTP headers to be sent on all responses.
   */
  private Map<String, String> globalHttpContentHeaders = new HashMap<>();

  /**
   * Logger for the web server.
   */
  private ExtendedLog log;

  /**
   * Bootstrap for the server.
   */
  private ServerBootstrap bootstrap;

  /**
   * {@code true} if running in debug mode.
   */
  private boolean debugMode;

  /**
   * {@code true} if support being a secure server.
   */
  private boolean secureServer;

  /**
   * The certificate chain file for an SSL connection. Can be {@code null}.
   */
  private File sslCertChainFile;

  /**
   * The key file for an SSL connection. Can be {@code null}.
   */
  private File sslKeyFile;

  /**
   * The SSL context for HTTPS connections. Will be {@code null} if the server
   * is not labeled secure.
   */
  private SslContext sslContext;

  /**
   * The default MIME resolver to use.
   */
  private MimeResolver defaultMimeResolver;

  /**
   * The complete collection of static content handlers.
   */
  private List<HttpGetRequestHandler> staticContentRequestHandlers = new ArrayList<>();

  /**
   * The complete collection of dynamic GET request handlers.
   */
  private List<HttpGetRequestHandler> getRequestHandlers = new ArrayList<>();

  /**
   * The complete collection of dynamic POST request handlers.
   */
  private List<HttpPostRequestHandler> postRequestHandlers = new ArrayList<>();

  /**
   * The complete collection of dynamic OPTIONS request handlers.
   */
  private List<HttpOptionsRequestHandler> optionsRequestHandlers = new ArrayList<>();

  /**
   * Create a web server using a singular thread pool.
   *
   * @param threadPool
   *          thread pool to use
   * @param log
   *          logger
   */
  public NettyWebServer(ScheduledExecutorService threadPool, ExtendedLog log) {
    this(threadPool, threadPool, log);
  }

  /**
   * Create a server with differentiated thread pools.
   *
   * @param bossThreadPool
   *          thread pool to use for boss threads
   * @param workerThreadPool
   *          thread pool to use for workers
   * @param log
   *          logger
   */
  public NettyWebServer(ScheduledExecutorService bossThreadPool,
      ScheduledExecutorService workerThreadPool, ExtendedLog log) {
    this.bossThreadPool = bossThreadPool;
    this.workerThreadPool = workerThreadPool;
    this.log = log;

    serverHandler = new NettyWebServerHandler(this);
  }

  @Override
  public void startup() {

    allChannels = new DefaultChannelGroup(serverName);

    channelFactory = new NioServerSocketChannelFactory(bossThreadPool, workerThreadPool);

    bootstrap = new ServerBootstrap(channelFactory);

    if (secureServer) {
      try {
        if (sslCertChainFile == null) {
          SelfSignedCertificate ssc = new SelfSignedCertificate();
          sslCertChainFile = ssc.certificate();
          sslKeyFile = ssc.privateKey();
        }

        sslContext = SslContext.newServerContext(sslCertChainFile, sslKeyFile);
      } catch (Exception e) {
        throw new SimpleSmartSpacesException("Could not create a secure web server", e);
      }
    }

    // Set up the event pipeline factory.
    bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
      @Override
      public ChannelPipeline getPipeline() throws Exception {
        // Create a default pipeline implementation.
        ChannelPipeline pipeline = pipeline();

        if (sslContext != null) {
          pipeline.addLast("ssl", sslContext.newHandler());
        }

        pipeline.addLast("decoder", new HttpRequestDecoder());
        // pipeline.addLast("aggregator", new
        // HttpChunkAggregator(4615604));
        pipeline.addLast("encoder", new HttpResponseEncoder());
        pipeline.addLast("chunkedWriter", new ChunkedWriteHandler());
        pipeline.addLast("handler", serverHandler);

        return pipeline;
      }
    });

    try {
      serverChannel = bootstrap.bind(new InetSocketAddress(port));
    } catch (ChannelException e) {
      if (e.getCause() instanceof BindException) {
        throw new NetworkBindSimpleSmartSpacesException(
            String.format("web server component could not be started, port %d in use", port), e);
      } else {
        throw e;
      }
    }
    allChannels.add(serverChannel);
  }

  @Override
  public void shutdown() {
    if (allChannels != null) {
      ChannelGroupFuture future = allChannels.close();
      future.awaitUninterruptibly();

      channelFactory = null;
      allChannels = null;

      bootstrap.shutdown();
      bootstrap = null;
    }
  }

  @Override
  public void addStaticContentHandler(String uriPrefix, File baseDir) {
    addStaticContentHandler(uriPrefix, baseDir, null);
  }

  @Override
  public void addStaticContentHandler(String uriPrefix, File baseDir,
      Map<String, String> extraHttpContentHeaders) {
    addStaticContentHandler(uriPrefix, baseDir, extraHttpContentHeaders, null, null);
  }

  @Override
  public void addStaticContentHandler(String uriPrefix, File baseDir,
      Map<String, String> extraHttpContentHeaders, String fallbackFilePath,
      HttpGetRequestHandler fallbackHandler) {
    
    HttpGetRequestHandler handler = newStaticContentHandler(uriPrefix, baseDir,
        extraHttpContentHeaders, fallbackFilePath, fallbackHandler);
    
    serverHandler.addHttpGetRequestHandler(new NettyHttpGetRequestHandlerHandler(serverHandler,
        uriPrefix, true, handler, extraHttpContentHeaders));

    staticContentRequestHandlers.add(handler);
  }
  
  @Override
  public HttpGetRequestHandler newStaticContentHandler(String uriPrefix, File baseDir) {
    return newStaticContentHandler(uriPrefix, baseDir, null);
  }

  @Override
  public HttpGetRequestHandler newStaticContentHandler(String uriPrefix, File baseDir,
      Map<String, String> extraHttpContentHeaders) {
    return newStaticContentHandler(uriPrefix, baseDir, extraHttpContentHeaders, null, null);
    
  }

  @Override
  public HttpGetRequestHandler newStaticContentHandler(String uriPrefix, File baseDir,
      Map<String, String> extraHttpContentHeaders, String fallbackFilePath,
      HttpGetRequestHandler fallbackHandler) {
    log.formatInfo("Creating web server static content handler for %s at %s (port=%d)", 
        baseDir.getAbsolutePath(), uriPrefix, port);
    if (!baseDir.exists()) {
      throw new SmartSpacesException(
          String.format("Cannot find static web content folder %s", baseDir.getAbsolutePath()));
    }

    NettyStaticContentHandler staticContentHandler = new NettyStaticContentHandler(serverHandler,
        uriPrefix, baseDir, extraHttpContentHeaders, fallbackFilePath, fallbackHandler);
    staticContentHandler.setAllowLinks(isDebugMode());
    if (isDebugMode()) {
      getLog().warn("Enabling web-server link following because of debug mode -- not secure.");
    }

    staticContentHandler.setMimeResolver(defaultMimeResolver);
    
    return staticContentHandler;
  }

  @Override
  public void addGetRequestHandler(String uriPrefix, boolean usePath,
      HttpGetRequestHandler handler) {
    addGetRequestHandler(uriPrefix, usePath, handler, null);
  }

  @Override
  public void addGetRequestHandler(String uriPrefix, boolean usePath, HttpGetRequestHandler handler,
      Map<String, String> extraHttpContentHeaders) {
    log.formatInfo("Adding web server GET handler at %s (usePath=%b, port=%d)", uriPrefix, usePath,
        port);
    serverHandler.addHttpGetRequestHandler(new NettyHttpGetRequestHandlerHandler(serverHandler,
        uriPrefix, usePath, handler, extraHttpContentHeaders));
    getRequestHandlers.add(handler);
  }

  @Override
  public void addOptionsRequestHandler(String uriPrefix, boolean usePath,
      HttpOptionsRequestHandler handler) {
    addOptionsRequestHandler(uriPrefix, usePath, handler, null);
  }

  @Override
  public void addOptionsRequestHandler(String uriPrefix, boolean usePath,
      HttpOptionsRequestHandler handler, Map<String, String> extraHttpContentHeaders) {
    log.formatInfo("Adding web server OPTIONS handler at %s (usePath=%b, port=%d)", uriPrefix,
        usePath, port);
    serverHandler.addHttpOptionsRequestHandler(new NettyHttpOptionsRequestHandlerHandler(
        serverHandler, uriPrefix, usePath, handler, extraHttpContentHeaders));
    optionsRequestHandlers.add(handler);
  }

  @Override
  public void addPostRequestHandler(String uriPrefix, boolean usePath,
      HttpPostRequestHandler handler) {
    addPostRequestHandler(uriPrefix, usePath, handler, null);
  }

  @Override
  public void addPostRequestHandler(String uriPrefix, boolean usePath,
      HttpPostRequestHandler handler, Map<String, String> extraHttpContentHeaders) {
    log.formatInfo("Adding web server POST handler at %s (usePath=%b, port=%d)", uriPrefix, usePath,
        port);
    serverHandler.addHttpPostRequestHandler(new NettyHttpPostRequestHandlerHandler(serverHandler,
        uriPrefix, usePath, handler, extraHttpContentHeaders));
    postRequestHandlers.add(handler);
  }

  @Override
  public List<HttpGetRequestHandler> getStaticContentRequestHandlers() {
    return Lists.newArrayList(staticContentRequestHandlers);
  }

  @Override
  public List<HttpGetRequestHandler> getRequestHandlers() {
    return Lists.newArrayList(getRequestHandlers);
  }

  @Override
  public List<HttpPostRequestHandler> getPostRequestHandlers() {
    return Lists.newArrayList(postRequestHandlers);
  }

  @Override
  public <M> void setWebSocketHandlerFactory(String webSocketUriPrefix,
      WebServerWebSocketHandlerFactory<M> webSocketHandlerFactory,
      MessageCodec<M, String> messageCodec) {
    serverHandler.setWebSocketHandlerFactory(webSocketUriPrefix, webSocketHandlerFactory,
        messageCodec);
  }

  @Override
  public String getServerName() {
    return serverName;
  }

  @Override
  public void setServerName(String serverName) {
    this.serverName = serverName;
  }

  @Override
  public void setPort(int port) {
    this.port = port;
  }

  @Override
  public int getPort() {
    return port;
  }

  @Override
  public void addContentHeader(String name, String value) {
    globalHttpContentHeaders.put(name, value);
  }

  @Override
  public void addContentHeaders(Map<String, String> headers) {
    globalHttpContentHeaders.putAll(headers);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T extends MimeResolver> T getDefaultMimeResolver() {
    return (T) defaultMimeResolver;
  }

  @Override
  public void setDefaultMimeResolver(MimeResolver resolver) {
    defaultMimeResolver = resolver;
  }

  @Override
  public void setDebugMode(boolean debugMode) {
    this.debugMode = debugMode;
  }

  /**
   * Check if this server is running in debug mode.
   *
   * @return {@code true} if this server is running in debug mode
   */
  public boolean isDebugMode() {
    return debugMode;
  }

  @Override
  public boolean isSecureServer() {
    return secureServer;
  }

  @Override
  public void setSecureServer(boolean secureServer) {
    this.secureServer = secureServer;
  }

  @Override
  public void setSslCertificates(File sslCertChainFile, File sslKeyFile) {
    if (((sslCertChainFile == null) && (sslKeyFile != null))
        || (sslCertChainFile != null) && (sslKeyFile == null)) {
      throw new SimpleSmartSpacesException(
          "Both a certificate chain file and a private key file must be supplied");

    }

    if (sslCertChainFile != null && !sslCertChainFile.isFile()) {
      throw new SimpleSmartSpacesException(String.format(
          "The certificate chain file %s does not exist", sslCertChainFile.getAbsolutePath()));
    }

    if (sslKeyFile != null && !sslKeyFile.isFile()) {
      throw new SimpleSmartSpacesException(
          String.format("The private key file %s does not exist", sslKeyFile.getAbsolutePath()));
    }

    this.sslCertChainFile = sslCertChainFile;
    this.sslKeyFile = sslKeyFile;
  }

  /**
   * Get the worker thread pool.
   *
   * @return thread pool to use for worker threads
   */
  public ExecutorService getWorkerThreadPool() {
    return workerThreadPool;
  }

  /**
   * A new channel was opened. Register it so it can be properly shut down.
   *
   * @param channel
   *          channel that has been opened
   */
  public void channelOpened(Channel channel) {
    allChannels.add(channel);
  }

  /**
   * Get the content headers which should go onto every HTTP response.
   *
   * @return the globalHttpContentHeaders
   */
  public Map<String, String> getGlobalHttpContentHeaders() {
    return globalHttpContentHeaders;
  }

  /**
   * Get the web server's logger.
   *
   * @return the logger
   */
  public ExtendedLog getLog() {
    return log;
  }
}
