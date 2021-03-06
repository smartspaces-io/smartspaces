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

import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;

import io.smartspaces.SimpleSmartSpacesException;
import io.smartspaces.SmartSpacesException;
import io.smartspaces.logging.ExtendedLog;
import io.smartspaces.service.web.server.HttpPostBody;
import io.smartspaces.util.web.CommonMimeTypes;
import io.smartspaces.util.web.HttpConstants;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.http.HttpChunk;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.multipart.Attribute;
import org.jboss.netty.handler.codec.http.multipart.FileUpload;
import org.jboss.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import org.jboss.netty.handler.codec.http.multipart.HttpPostRequestDecoder.EndOfDataDecoderException;
import org.jboss.netty.handler.codec.http.multipart.InterfaceHttpData;
import org.jboss.netty.handler.codec.http.multipart.InterfaceHttpData.HttpDataType;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * A Netty-based {@link HttpPostBody}.
 *
 * @author Keith M. Hughes
 */
public class NettyHttpPostBody implements HttpPostBody {

  /**
   * The handler for the POST request. It can be {@code null}.
   */
  private NettyHttpPostRequestHandler handler;

  /**
   * The parameters that were part of the post.
   */
  private Map<String, String> parameters = new HashMap<>();

  /**
   * The decoder.
   */
  private HttpPostRequestDecoder decoder;

  /**
   * The request that started everything off.
   */
  private NettyHttpRequest request;

  /**
   * The response.
   */
  private NettyHttpResponse response;

  /**
   * The web server handler handling the request.
   */
  private NettyWebServerHandler webServerHandler;

  /**
   * FileUpload container for this particular upload.
   */
  private FileUpload fileUpload;

  /**
   * {@code true} if this is a form post.
   */
  private boolean isForm;

  /**
   * Create a new instance.
   *
   * @param request
   *          incoming HTTP request
   * @param response
   *          outgoing HTTP response
   * @param decoder
   *          decoder to use
   * @param handler
   *          the HTTP POST handler, can be {@code null}
   * @param webServerHandler
   *          underlying web server handler
   */
  public NettyHttpPostBody(NettyHttpRequest request, NettyHttpResponse response,
      HttpPostRequestDecoder decoder, NettyHttpPostRequestHandler handler,
      NettyWebServerHandler webServerHandler) {
    this.request = request;
    this.response = response;
    this.decoder = decoder;
    this.handler = handler;
    this.webServerHandler = webServerHandler;

    String contentType =
        request.getUnderlyingRequest().headers().get(HttpConstants.HEADER_NAME_CONTENT_TYPE);
    isForm = CommonMimeTypes.MIME_TYPE_FORM_MULTIPART.equals(contentType)
        || CommonMimeTypes.MIME_TYPE_FORM_URLENCODED.equals(contentType);
  }

  /**
   * Get the original Netty HTTP request.
   *
   * @return the original Netty HTTP request
   */
  HttpRequest getNettyHttpRequest() {
    return request.getUnderlyingRequest();
  }

  /**
   * Add a new chunk of data to the upload.
   *
   * @param ctx
   *          the context for the channel handling
   * @param chunk
   *          the chunked data
   *
   * @throws Exception
   *           problem adding chunk
   *
   */
  void addChunk(ChannelHandlerContext ctx, HttpChunk chunk) throws Exception {
    if (!chunk.getContent().readable() && !chunk.isLast()) {
      return;
    }

    decoder.offer(chunk);
    try {
      while (decoder.hasNext()) {
        InterfaceHttpData data = decoder.next();
        if (data != null) {
          processHttpData(data);
        }
        if (chunk.isLast()) {
          break;
        }
      }
    } catch (EndOfDataDecoderException e) {
      getLog().error("Error while adding HTTP chunked POST data", e);
    }
  }

  /**
   * Clean up anything from the upload.
   */
  void clean() {
    decoder.cleanFiles();
  }

  /**
   * Complete processing of the file upload.
   */
  void completeNonChunked() {
    try {
      for (InterfaceHttpData data : decoder.getBodyHttpDatas()) {
        processHttpData(data);
      }
    } catch (Exception e) {
      getLog().error("Error while completing HTTP chunked POST data", e);
    }
  }

  /**
   * The body upload is complete. Handle it as needed.
   */
  void bodyUploadComplete() {
    if (handler != null) {
      handleBodyUploadCompleteThroughHandler();
    } else {
      getLog().formatError("HTTP post web request not handled due to no handle for URI %s",
          request.getUri());
    }
  }

  /**
   * Handle the body upload completion through the handler.
    */
  private void handleBodyUploadCompleteThroughHandler() {
    try {
      handler.handleWebRequest(request, this, response);
    } catch (Exception e) {
      getLog().formatError(e, "Exception when handling web request %s", request.getUri());
    }
  }

  /**
   * Process a clump of of the HTTP data.
   *
   * @param data
   *          the data
   */
  private void processHttpData(InterfaceHttpData data) {
    if (data.getHttpDataType() == HttpDataType.Attribute) {
      Attribute attribute = (Attribute) data;
      try {
        parameters.put(attribute.getName(), attribute.getValue());
      } catch (IOException e1) {
        // Error while reading data from File, only print name and error
        getLog().error("Form post BODY Attribute: " + attribute.getHttpDataType().name() + ": "
            + attribute.getName() + " Error while reading value:", e1);
      }
    } else if (data.getHttpDataType() == HttpDataType.FileUpload) {
      fileUpload = (FileUpload) data;
      if (fileUpload.isCompleted()) {
        getLog().formatInfo("File %s uploaded", fileUpload.getFilename());
      } else {
        getLog().error("File to be continued but should not!");
      }
    } else {
      getLog().formatWarn("Unprocessed form post data type %s", data.getHttpDataType().name());
    }
  }

  @Override
  public String getContentType() {
    return request.getUnderlyingRequest().headers().get(CONTENT_TYPE);
  }

  @Override
  public boolean isFormPost() {
    return isForm;
  }

  @Override
  public boolean isMultipart() {
    return decoder.isMultipart();
  }

  @Override
  public boolean hasFile() {
    return fileUpload != null;
  }

  @Override
  public boolean moveTo(File destination) {
    if (hasFile()) {
      try {
        fileUpload.renameTo(destination);

        return true;
      } catch (Exception e) {
        throw SmartSpacesException.newFormattedException(e, "Unable to save uploaded file to %s",
            destination);
      }
    } else {
      return false;
    }
  }

  @Override
  public boolean copyTo(OutputStream destination) {
    if (hasFile()) {
      try {
        ChannelBuffer channelBuffer = fileUpload.getChannelBuffer();
        channelBuffer.getBytes(0, destination, channelBuffer.readableBytes());

        return true;
      } catch (Exception e) {
        throw SmartSpacesException.newFormattedException(e,
            "Unable to save uploaded file to output stream");
      }
    } else {
      return false;
    }
  }

  @Override
  public String getFormName() {
    return fileUpload.getName();
  }

  @Override
  public String getFilename() {
    if (hasFile()) {
      return fileUpload.getFilename();
    } else {
      return null;
    }
  }

  @Override
  public Map<String, String> getParameters() {
    return parameters;
  }

  @Override
  public byte[] getContent() throws SmartSpacesException {
    if (!isFormPost()) {
      ChannelBuffer content = request.getUnderlyingRequest().getContent();
      int readableBytes = content.readableBytes();

      byte[] contentBytes = new byte[readableBytes];
      content.getBytes(0, contentBytes);

      return contentBytes;
    } else {
      throw new SimpleSmartSpacesException(
          "The HTTP POST was multipart and attempting to get the content");
    }
  }

  /**
   * Get the log for the uploader.
   *
   * @return the log
   */
  private ExtendedLog getLog() {
    return webServerHandler.getWebServer().getLog();
  }
}
