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

package io.smartspaces.service.web.server.internal.netty

import org.jboss.netty.handler.codec.http.HttpHeaders.addHeader
import org.jboss.netty.handler.codec.http.HttpHeaders.getHeader
import org.jboss.netty.handler.codec.http.HttpHeaders.isKeepAlive
import org.jboss.netty.handler.codec.http.HttpHeaders.setContentLength
import org.jboss.netty.handler.codec.http.HttpVersion.HTTP_1_1

import io.smartspaces.SimpleSmartSpacesException
import io.smartspaces.service.web.server.HttpStaticContentRequestHandler
import io.smartspaces.util.io.FileSupport
import io.smartspaces.util.io.FileSupportImpl
import io.smartspaces.util.web.MimeResolver

import org.jboss.netty.channel.Channel
import org.jboss.netty.channel.ChannelFuture
import org.jboss.netty.channel.ChannelFutureListener
import org.jboss.netty.channel.ChannelFutureProgressListener
import org.jboss.netty.channel.ChannelHandlerContext
import org.jboss.netty.channel.DefaultFileRegion
import org.jboss.netty.channel.FileRegion
import org.jboss.netty.handler.codec.http.CookieEncoder
import org.jboss.netty.handler.codec.http.DefaultHttpResponse
import org.jboss.netty.handler.codec.http.HttpHeaders
import org.jboss.netty.handler.codec.http.HttpMethod
import org.jboss.netty.handler.codec.http.HttpRequest
import org.jboss.netty.handler.codec.http.HttpResponse
import org.jboss.netty.handler.codec.http.HttpResponseStatus
import org.jboss.netty.handler.ssl.SslHandler
import org.jboss.netty.handler.stream.ChunkedFile

import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.io.RandomAccessFile
import java.lang.{ Long => JLong }
import java.net.HttpCookie
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.util.{ HashMap => JHashMap }
import java.util.{ Map => JMap }
import java.util.{ Set => JSet }
import java.util.regex.Matcher
import java.util.regex.Pattern

import scala.collection.JavaConverters._

/**
 * Handle static web content using Netty.
 *
 * @author Keith M. Hughes
 */
class NettyStaticContentHandler(parentHandler: NettyWebServerHandler, uriPrefixBase: String,
  baseDir: File, _extraHttpContentHeaders: JMap[String, String], fallbackFilePath: String,
  fallbackHandler: NettyHttpDynamicGetRequestHandlerHandler)
  extends NettyHttpGetRequestHandler with HttpStaticContentRequestHandler {

  /**
   * The first portion of a content range header.
   */
  private val CONTENT_RANGE_PREFIX = "bytes "

  /**
   * The separator between the start and end of the range.
   */
  private val CONTENT_RANGE_RANGE_SEPARATOR = "-"

  /**
   * The separator between the range and the file size in a content range
   * header.
   */
  private val CONTENT_RANGE_RANGE_SIZE_SEPARATOR = "/"

  /**
   * Chunk size to use for copying content.
   */
  private val COPY_CHUNK_SIZE = 8192

  /**
   * Regex for an HTTP range header.
   */
  private val RANGE_HEADER_REGEX = Pattern.compile("bytes=(\\d+)\\-(\\d+)?")

  /**
   * The URI prefix to be handled by this handler.
   */
  private val uriPrefix = {
    val sanitizedUriPrefix = new StringBuilder()
    if (!uriPrefixBase.startsWith(CONTENT_RANGE_RANGE_SIZE_SEPARATOR)) {
      sanitizedUriPrefix.append('/')
    }
    sanitizedUriPrefix.append(uriPrefixBase)
    if (!uriPrefixBase.endsWith(CONTENT_RANGE_RANGE_SIZE_SEPARATOR)) {
      sanitizedUriPrefix.append('/')
    }
    sanitizedUriPrefix.toString()
  }

  /**
   * Extra headers to add to the response.
   */
  private val extraHttpContentHeaders: JMap[String, String] = new JHashMap()
  if (_extraHttpContentHeaders != null) {
    extraHttpContentHeaders.putAll(_extraHttpContentHeaders)
  }

  /**
   * Should this web-server allow links to be accessed? (Wander outside the root
   * filesystem.) Useful for debugging & development.
   */
  private var allowLinks = false

  /**
   * The MIME resolver to use for responding to requests.
   *
   * <p>
   * Can be {@code null}.
   */
  private var mimeResolver: MimeResolver = null

  /**
   * The file support to use.
   */
  private val fileSupport = FileSupportImpl.INSTANCE

  override def setMimeResolver(resolver: MimeResolver): Unit = {
    mimeResolver = resolver
  }

  override def getMimeResolver[T <: MimeResolver](): T = {
    mimeResolver.asInstanceOf[T]
  }

  override def isHandledBy(request: NettyHttpRequest): Boolean = {
    if (request.getUri.getPath.startsWith(uriPrefix)) {
      val method = request.getMethod()
      method == HttpMethod.GET || method == HttpMethod.HEAD
    } else {
      false
    }
  }

  override def handleWebRequest(ctx: ChannelHandlerContext, request: NettyHttpRequest,
    cookiesToAdd: JSet[HttpCookie]) {
    var url = request.getUri().getPath
    val originalUrl = url

    // Strip off query parameters, if any, as we don't care.
    val pos = url.indexOf('?')
    if (pos != -1) {
      url = url.substring(0, pos)
    }

    val luriPrefixLength = uriPrefix.length()
    val filepath = URLDecoder.decode(
      url.substring(url.indexOf(uriPrefix) + luriPrefixLength),
      StandardCharsets.UTF_8.name())

    var file = fileSupport.newFile(baseDir, filepath)

    // Refuse to process if the path wanders outside of the base directory.
    if (!allowLinks && !fileSupport.isParent(baseDir, file)) {
      val status = HttpResponseStatus.FORBIDDEN
      parentHandler.getWebServer().getLog().warn(
        s"HTTP [${status.getCode()}] ${originalUrl} --> (Path attempts to leave base directory)")
      parentHandler.sendError(ctx, status)
      return 
    }

    if (!fileSupport.exists(file)) {
      if (fallbackFilePath != null) {
        val fallbackFile = fileSupport.newFile(baseDir, fallbackFilePath)
        if (fileSupport.exists(fallbackFile)) {
          file = fallbackFile
        } else {
          handleNonFileFallback(ctx, request, cookiesToAdd, originalUrl)
          return 
        }
      } else {
        handleNonFileFallback(ctx, request, cookiesToAdd, originalUrl)
        return 
      }
    }

    val raf = {
      try {
        new RandomAccessFile(file, "r")
      } catch {
        case fnfe: FileNotFoundException =>
          handleNonFileFallback(ctx, request, cookiesToAdd, originalUrl)
          return 
      }
    }
    val fileLength = raf.length()

    // Start with an initial OK response which will be modified as needed.
    val response = new DefaultHttpResponse(HTTP_1_1, HttpResponseStatus.OK)

    setMimeType(filepath, response)

    parentHandler.addHttpResponseHeaders(response, extraHttpContentHeaders)
    parentHandler.addHeaderIfNotExists(response, HttpHeaders.Names.ACCEPT_RANGES,
      HttpHeaders.Values.BYTES)

    if (cookiesToAdd != null) {
      val encoder = new CookieEncoder(true)
      cookiesToAdd.asScala.foreach { value =>
        encoder.addCookie(NettyHttpResponse.createNettyCookie(value))
        addHeader(response, HttpHeaders.Names.SET_COOKIE, encoder.encode())
      }
    }

    val rangeRequest = {
      try {
        parseRangeRequest(request, fileLength)
      } catch {
        case e: Exception =>
          try {
            val status = HttpResponseStatus.REQUESTED_RANGE_NOT_SATISFIABLE
            parentHandler.getWebServer().getLog().error(s"[${status.getCode()}] HTTP ${originalUrl} --> ${e.getMessage()}")
            response.setStatus(status)
            parentHandler.sendError(ctx, status)
          } finally {
            try {
              raf.close()
            } catch {
              case e1: Exception =>
                parentHandler.getWebServer().getLog().warn("Unable to close static content file", e1)
            }
          }
          return 
      }
    }

    var status = HttpResponseStatus.OK
    if (rangeRequest == null) {
      setContentLength(response, fileLength)
    } else {
      setContentLength(response, rangeRequest.getRangeLength())
      addHeader(response, HttpHeaders.Names.CONTENT_RANGE,
        CONTENT_RANGE_PREFIX + rangeRequest.begin + CONTENT_RANGE_RANGE_SEPARATOR
          + rangeRequest.end + CONTENT_RANGE_RANGE_SIZE_SEPARATOR + fileLength)
      status = HttpResponseStatus.PARTIAL_CONTENT
      response.setStatus(status)
    }

    val ch = ctx.getChannel()

    // Write the initial line and the header.
    var writeFuture = ch.write(response)

    // Write the content if there have been no errors and we are a GET
    // request.
    if (HttpMethod.GET == request.getMethod()) {
      if (ch.getPipeline().get(classOf[SslHandler]) != null) {
        // Cannot use zero-copy with HTTPS.
        writeFuture = ch.write(new ChunkedFile(raf, 0, fileLength, COPY_CHUNK_SIZE))
      } else {
        // No encryption - use zero-copy.
        val region =
          new DefaultFileRegion(
            raf.getChannel(),
            if (rangeRequest != null) {
              rangeRequest.begin
            } else {
              0l
            },
            if (rangeRequest != null) {
              rangeRequest.getRangeLength()
            } else {
              fileLength
            })
        writeFuture = ch.write(region)
        writeFuture.addListener(new ChannelFutureProgressListener() {
          override def operationComplete(future: ChannelFuture): Unit = {
            region.releaseExternalResources()
          }

          override def operationProgressed(arg0: ChannelFuture, arg1: Long, arg2: Long, arg3: Long): Unit = {
            // Do nothing
          }
        })
      }
    }

    // Decide whether to close the connection or not.
    if (!isKeepAlive(request.getUnderlyingRequest)) {
      // Close the connection when the whole content is written out.
      writeFuture.addListener(ChannelFutureListener.CLOSE)
    }

    parentHandler.getWebServer().getLog().trace(s"[${status.getCode()}] HTTP ${originalUrl} --> ${file.getPath()}")
  }

  private def handleNonFileFallback(ctx: ChannelHandlerContext, request: NettyHttpRequest,
    cookiesToAdd: JSet[HttpCookie], originalUrl: String): Unit = {
    if (fallbackHandler != null) {
      fallbackHandler.handleWebRequest(ctx, request, cookiesToAdd)
    } else {
      val status = HttpResponseStatus.NOT_FOUND
      parentHandler.getWebServer().getLog().warn(s"HTTP [${status.getCode()}] ${originalUrl} --> (File Not Found)")
      parentHandler.sendError(ctx, status)
    }
  }

  /**
   * Set the MIME type of the content, if we can.
   *
   * @param filepath
   *          the filepath for the content
   * @param response
   *          the HTTP response
   */
  private def setMimeType(filepath: String, response: HttpResponse): Unit = {
    if (mimeResolver != null) {
      val mimeType = mimeResolver.resolve(filepath)
      if (mimeType != null) {
        HttpHeaders.setHeader(response, HttpHeaders.Names.CONTENT_TYPE, mimeType)
      }
    }
  }

  /**
   * Get a range header from the request, if there is one.
   *
   * @param request
   *          the request
   * @param availableLength
   *          the available number of bytes for the file requested
   *
   * @return a parsed range header, or {@code null} if there is no range request
   *         header or there was some sort of error
   */
  private def parseRangeRequest(request: NettyHttpRequest, availableLength: Long): RangeRequest = {
    def rangeHeader = getHeader(request.getUnderlyingRequest, HttpHeaders.Names.RANGE)
    if (rangeHeader == null || rangeHeader.trim().isEmpty()) {
      return null
    }

    val m = RANGE_HEADER_REGEX.matcher(rangeHeader)
    if (!m.matches()) {
      throw new SimpleSmartSpacesException(
        s"Unsupported HTTP range header, illegal syntax: ${rangeHeader}")
    }

    val range = new RangeRequest()
    range.begin = JLong.parseLong(m.group(1))
    val endMatch = m.group(2)
    range.end = if (endMatch != null && !endMatch.trim().isEmpty()) {
      JLong.parseLong(endMatch)
    } else {
      availableLength - 1
    }

    if (range.end < range.begin) {
      null
    } else if (range.end >= availableLength) {
      throw new SimpleSmartSpacesException(
        s"Unsupported HTTP range header, length requested is more than actual length: ${rangeHeader}")
    } else {
      range
    }
  }

  /**
   * Allow files linked outside the root filesystem to be accessed.
   *
   * @param allowLinks
   *          {@code true} if following links should be allowed
   */
  def setAllowLinks(allowLinks: Boolean): Unit = {
    this.allowLinks = allowLinks
  }

  /**
   * An HTTP range request.
   *
   * @author Keith M. Hughes
   */
  class RangeRequest() {

    var begin: Long = 0

    var end: Long = 0

    /**
     * Get the number of bytes in the range.
     *
     * @return the number of bytes in the range
     */
    def getRangeLength(): Long = {
      end - begin + 1
    }
  }
}
