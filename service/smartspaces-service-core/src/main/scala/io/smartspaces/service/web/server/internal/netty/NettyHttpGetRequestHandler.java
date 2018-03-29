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

import java.io.IOException;
import java.net.HttpCookie;
import java.util.Set;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.http.HttpRequest;

/**
 * Handle HTTP GET requests from Netty.
 *
 * @author Keith M. Hughes
 */
public interface NettyHttpGetRequestHandler extends NettyHttpRequestHandler {

  /**
   * Handle the web request.
   *
   * @param ctx
   *          the channel handler context
   * @param nettyRequest
   *          the Netty HTTP request
   * @param cookiesToAdd
   *          cookies to be set on the response
   *
   * @throws IOException
   *           something bad happened
   */
  void handleWebRequest(ChannelHandlerContext ctx, HttpRequest nettyRequest,
      Set<HttpCookie> cookiesToAdd) throws IOException;
}
