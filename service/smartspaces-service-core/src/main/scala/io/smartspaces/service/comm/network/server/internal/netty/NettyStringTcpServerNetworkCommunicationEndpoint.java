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

package io.smartspaces.service.comm.network.server.internal.netty;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicLong;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.codec.frame.DelimiterBasedFrameDecoder;
import org.jboss.netty.handler.codec.string.StringDecoder;
import org.jboss.netty.handler.codec.string.StringEncoder;

import com.google.common.collect.Lists;

import io.smartspaces.SimpleSmartSpacesException;
import io.smartspaces.logging.ExtendedLog;
import io.smartspaces.service.comm.network.server.TcpServerClientConnection;
import io.smartspaces.service.comm.network.server.TcpServerNetworkCommunicationEndpoint;
import io.smartspaces.service.comm.network.server.TcpServerNetworkCommunicationEndpointListener;
import io.smartspaces.service.comm.network.server.TcpServerRequest;

/**
 * A Netty-based {@link TcpServerNetworkCommunicationEndpoint} using strings for
 * messaging.
 *
 * @author Keith M. Hughes
 */
public class NettyStringTcpServerNetworkCommunicationEndpoint
    implements TcpServerNetworkCommunicationEndpoint<String> {

  /**
   * The delimiters for the incoming string messages.
   */
  private final ChannelBuffer[] delimiters;

  /**
   * Charset for the strings.
   */
  private final Charset charset;

  /**
   * The port the server is listening to.
   */
  private final int serverPort;

  /**
   * The bootstrap for the TCP server.
   */
  private ServerBootstrap bootstrap;

  /**
   * The listeners to endpoint events.
   */
  private final List<TcpServerNetworkCommunicationEndpointListener<String>> listeners =
      Lists.newCopyOnWriteArrayList();

  /**
   * Executor service for this endpoint.
   */
  private final ExecutorService executorService;

  /**
   * Logger for this endpoint.
   */
  private final ExtendedLog log;

  /**
   * The collection of connections.
   */
  private final Map<Integer, InternalClientConnection> clientConnections = new HashMap<>();

  /**
   * Creator of connection IDs.
   */
  private final AtomicLong connectionIdFactory = new AtomicLong(System.currentTimeMillis());

  /**
   * Construct a new endpoint.
   *
   * @param delimiters
   *          the delimiters for messages
   * @param charset
   *          the character set for messages
   * @param serverPort
   *          the server port to listen on
   * @param executorService
   *          the executor service for threads
   * @param log
   *          the logger to use
   */
  public NettyStringTcpServerNetworkCommunicationEndpoint(ChannelBuffer[] delimiters,
      Charset charset, int serverPort, ExecutorService executorService, ExtendedLog log) {
    this.delimiters = delimiters;
    this.charset = charset;
    this.serverPort = serverPort;
    this.executorService = executorService;
    this.log = log;
  }

  @Override
  public void startup() {
    // Configure the server.
    bootstrap =
        new ServerBootstrap(new NioServerSocketChannelFactory(executorService, executorService));

    // Set up the pipeline factory.
    bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
      @Override
      public ChannelPipeline getPipeline() throws Exception {
        ChannelPipeline pipeline = Channels.pipeline();

        pipeline.addLast("frameDecoder",
            new DelimiterBasedFrameDecoder(Integer.MAX_VALUE, delimiters));
        pipeline.addLast("stringDecoder", new StringDecoder(charset));
        pipeline.addLast("stringEncoder", new StringEncoder(charset));
        pipeline.addLast("handler", new NettyTcpServerHandler());

        return pipeline;
      }
    });

    // Bind and start to accept incoming connections.
    bootstrap.bind(new InetSocketAddress(serverPort));

    log.info("TCP server started");
  }

  @Override
  public void shutdown() {
    listeners.clear();

    if (bootstrap != null) {
      closeAllChannels();

      bootstrap.shutdown();
      bootstrap = null;
    }
  }

  @Override
  public int getServerPort() {
    return serverPort;
  }

  @Override
  public void addListener(TcpServerNetworkCommunicationEndpointListener<String> listener) {
    listeners.add(listener);
  }

  @Override
  public void removeListener(TcpServerNetworkCommunicationEndpointListener<String> listener) {
    listeners.remove(listener);
  }

  @Override
  public void sendMessageAllChannels(String message) {
    for (InternalClientConnection clientConnection : clientConnections.values()) {
      try {
        clientConnection.sendMessage(message);
      } catch (Throwable e) {
        log.error("Could not write message to connection " + clientConnection, e);
      }
    }
  }

  @Override
  public void closeAllChannels() {
    for (InternalClientConnection clientConnection : clientConnections.values()) {
      try {
        clientConnection.close();
      } catch (Throwable e) {
        log.error("Could not close connection " + clientConnection, e);
      }
    }
  }

  @Override
  public String toString() {
    return "NettyStringTcpServerNetworkCommunicationEndpoint [serverPort=" + serverPort + "]";
  }

  /**
   * Handle a new connection.
   *
   * @param event
   *          the event that happened
   */
  private void handleNewConnection(ChannelStateEvent event) {
    String connectionId = newConnectionId();
    InternalClientConnection connection =
        new InternalClientConnection(event.getChannel(), connectionId);
    synchronized (clientConnections) {
      clientConnections.put(event.getChannel().getId(), connection);
    }

    for (TcpServerNetworkCommunicationEndpointListener<String> listener : listeners) {
      try {
        listener.onNewTcpConnection(this, connection);
      } catch (Throwable e) {
        log.error("Error while handing TCP connection", e);
      }
    }
  }

  /**
   * Handle the message received by the handler.
   *
   * @param event
   *          the event which happened
   */
  private void handleMessageReceived(MessageEvent event) {
    InternalClientConnection connection = null;
    synchronized (clientConnections) {
      connection = clientConnections.get(event.getChannel().getId());
    }

    NettyStringTcpServerRequest request = new NettyStringTcpServerRequest(event, connection);

    for (TcpServerNetworkCommunicationEndpointListener<String> listener : listeners) {
      try {
        listener.onTcpRequest(this, request);
      } catch (Throwable e) {
        log.error("Error while handing TCP message", e);
      }
    }
  }

  /**
   * Handle a connection that closed.
   *
   * @param event
   *          the event that happened
   */
  private void handleClosedConnection(ChannelStateEvent event) {
    InternalClientConnection connection = null;
    synchronized (clientConnections) {
      connection = clientConnections.remove(event.getChannel().getId());
    }

    // The connection may be removed from the map depending on who closed it.
    // Don't call the callback if not in the map as this means the server closed
    // the connection.
    if (connection != null) {
      for (TcpServerNetworkCommunicationEndpointListener<String> listener : listeners) {
        try {
          listener.onCloseTcpConnection(this, connection);
        } catch (Throwable e) {
          log.error("Error while handing TCP connection", e);
        }
      }
    }
  }

  /**
   * Close the connection to the client.
   * 
   * @param clientConnection
   *          the connection to close
   */
  public void closeClientConnection(InternalClientConnection clientConnection) {
    Channel channel = clientConnection.getChannel();

    synchronized (clientConnections) {
      clientConnections.remove(channel.getId());
    }

    channel.close();
  }

  /**
   * Create a new connection ID.
   *
   * @return the new connection ID
   */
  private String newConnectionId() {
    return Long.toHexString(connectionIdFactory.getAndAdd(1));
  }

  /**
   * Netty handler for incoming TCP requests.
   *
   * @author Keith M. Hughes
   */
  public class NettyTcpServerHandler extends SimpleChannelUpstreamHandler {

    @Override
    public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
      handleNewConnection(e);
    }

    @Override
    public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
      handleClosedConnection(e);
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
      handleMessageReceived(e);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
      log.error("Error during netty TCP server handler processing", e.getCause());
    }
  }

  /**
   * Netty-based version of the {@link TcpServerRequest}.
   *
   * @author Keith M. Hughes
   */
  private static class NettyStringTcpServerRequest implements TcpServerRequest<String> {

    /**
     * The message event from the request.
     */
    private final MessageEvent event;

    /**
     * The client connection.
     */
    private final InternalClientConnection clientConnection;

    /**
     * Construct a new request.
     *
     * @param event
     *          the netty message event
     * @param clientConnection
     *          the client connection;
     */
    public NettyStringTcpServerRequest(MessageEvent event,
        InternalClientConnection clientConnection) {
      this.event = event;
      this.clientConnection = clientConnection;
    }

    @Override
    public InetSocketAddress getRemoteAddress() {
      return (InetSocketAddress) event.getRemoteAddress();
    }

    @Override
    public String getMessage() {
      return (String) event.getMessage();
    }

    @Override
    public void sendMessage(String response) {
      clientConnection.sendMessage(response);
    }

    @Override
    public TcpServerClientConnection<String> getClientConnection() {
      return clientConnection;
    }
  }

  /**
   * The client connection.
   * 
   * @author Keith M. Hughes
   */
  private class InternalClientConnection implements TcpServerClientConnection<String> {

    /**
     * The channel for the connection.
     */
    private Channel channel;

    /**
     * The ID for the channel.
     */
    private String channelId;

    /**
     * Construct a new connection.
     * 
     * @param channel
     *          the channel for the connection
     * @param channelId
     *          the ID for the channel
     */
    public InternalClientConnection(Channel channel, String channelId) {
      this.channel = channel;
      this.channelId = channelId;
    }

    @Override
    public String getChannelId() {
      return channelId;
    }

    @Override
    public void sendMessage(String message) {
      if (isOpen()) {
        channel.write(message);
      } else {
        throw new SimpleSmartSpacesException(
            "Attempt to write on a closed TCP server client connection");
      }
    }

    @Override
    public boolean isOpen() {
      return channel.isOpen();
    }

    @Override
    public void close() {
      closeClientConnection(this);
    }

    @Override
    public SocketAddress getRemoteAddress() {
      return channel.getRemoteAddress();
    }

    /**
     * Get the channel for this connection.
     * 
     * @return the channel
     */
    public Channel getChannel() {
      return channel;
    }

    @Override
    public String toString() {
      return "TcpServerClientConnection[clientaddress=" + channel.getRemoteAddress() + "]";
    }
  }
}
