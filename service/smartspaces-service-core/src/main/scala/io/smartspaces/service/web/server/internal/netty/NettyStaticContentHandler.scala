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

import java.io.File
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.util.{ HashMap => JHashMap }
import java.util.{ Map => JMap }
import java.util.regex.Pattern

import org.jboss.netty.handler.codec.http.HttpResponseStatus

import io.smartspaces.service.web.server.HttpGetRequestHandler
import io.smartspaces.service.web.server.HttpStaticContentRequestHandler
import io.smartspaces.util.io.FileSupportImpl
import io.smartspaces.util.web.HttpConstants
import io.smartspaces.util.web.MimeResolver
import io.smartspaces.service.web.server.HttpResponse
import io.smartspaces.service.web.server.HttpRequest

/**
 * Handle static web content using Netty.
 *
 * @author Keith M. Hughes
 */
class NettyStaticContentHandler(parentHandler: NettyWebServerHandler, uriPrefixBase: String,
  baseDir: File, _extraHttpContentHeaders: JMap[String, String], fallbackFilePath: String,
  fallbackHandler: HttpGetRequestHandler)
  extends HttpGetRequestHandler with HttpStaticContentRequestHandler {

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
   * The URI prefix that makes it easy to remove the front.
   */
  val uriPrefix = {
    val uriPrefix = new StringBuilder()
    if (!uriPrefixBase.startsWith("/")) {
      uriPrefix.append('/')
    }
    uriPrefix.append(uriPrefixBase)
    if (!uriPrefixBase.endsWith("/")) {
      uriPrefix.append('/')
    }
    uriPrefix.toString()
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

  override def handleGetHttpRequest(request: HttpRequest, response: HttpResponse): Unit = {
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
      parentHandler.sendError(response.asInstanceOf[NettyHttpResponse].getChannelHandlerContext(), status)
      return
    }

    if (!fileSupport.exists(file)) {
      if (fallbackFilePath != null) {
        val fallbackFile = fileSupport.newFile(baseDir, fallbackFilePath)
        if (fileSupport.exists(fallbackFile)) {
          file = fallbackFile
        } else {
          handleNonFileFallback(request, response, originalUrl)
          return
        }
      } else {
        handleNonFileFallback(request, response, originalUrl)
        return
      }
    }

    val writer = new NettyStaticContentResponseWriter(parentHandler, fallbackHandler, mimeResolver)
    writer.writeResponse(file, request.asInstanceOf[NettyHttpRequest], response.asInstanceOf[NettyHttpResponse])
  }

  private def handleNonFileFallback(request: HttpRequest, response: HttpResponse, originalUrl: String): Unit = {
    if (fallbackHandler != null) {
      fallbackHandler.handleGetHttpRequest(request, response)
    } else {
      val status = HttpResponseStatus.NOT_FOUND
      parentHandler.getWebServer().getLog().warn(s"HTTP [${status.getCode()}] ${originalUrl} --> (File Not Found)")
      parentHandler.sendError(response.asInstanceOf[NettyHttpResponse].getChannelHandlerContext, status)
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
}
