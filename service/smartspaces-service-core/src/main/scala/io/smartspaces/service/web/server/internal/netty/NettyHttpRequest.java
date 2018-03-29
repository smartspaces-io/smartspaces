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

package io.smartspaces.service.web.server.internal.netty;

import java.net.HttpCookie;
import java.net.SocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.jboss.netty.handler.codec.http.Cookie;
import org.jboss.netty.handler.codec.http.CookieDecoder;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import io.smartspaces.SmartSpacesException;
import io.smartspaces.logging.ExtendedLog;
import io.smartspaces.service.web.server.HttpRequest;

/**
 * An HTTP request that proxies the Netty HTTP request.
 *
 * @author Keith M. Hughes
 */
public class NettyHttpRequest implements HttpRequest {

  /**
   * The proxied request.
   */
  private org.jboss.netty.handler.codec.http.HttpRequest request;

  /**
   * The remote address for the sender of the HTTP request.
   */
  private SocketAddress remoteAddress;

  /**
   * The logger for this request.
   */
  private ExtendedLog log;

  /**
   * The headers for the request.
   */
  private Multimap<String, String> headers;

  /**
   * Construct a new request.
   *
   * @param request
   *          the Netty HTTP request
   * @param remoteAddress
   *          the remote address for the request
   * @param log
   *          the logger for the request
   */
  public NettyHttpRequest(org.jboss.netty.handler.codec.http.HttpRequest request,
      SocketAddress remoteAddress, ExtendedLog log) {
    this.request = request;
    this.remoteAddress = remoteAddress;
    this.log = log;
    headers = null;
  }

  @Override
  public SocketAddress getRemoteAddress() {
    return remoteAddress;
  }

  @Override
  public String getMethod() {
    return request.getMethod().getName();
  }

  @Override
  public URI getUri() {
    try {
      return new URI(request.getUri());
    } catch (URISyntaxException e) {
      // Should never, ever happen
      throw new SmartSpacesException(String.format("Illegal URI syntax %s", request.getUri()), e);
    }
  }

  @Override
  public Map<String, String> getUriQueryParameters() {
    Map<String, String> params = new HashMap<>();

    String rawQuery = getUri().getRawQuery();
    if (rawQuery != null && !rawQuery.isEmpty()) {
      String[] components = rawQuery.split("\\&");
      for (String component : components) {
        int pos = component.indexOf('=');
        if (pos != -1) {
          String decode = component.substring(pos + 1);
          try {
            decode = URLDecoder.decode(decode, "UTF-8");
          } catch (Exception e) {
            // Don't care
          }
          params.put(component.substring(0, pos).trim(), decode);
        } else {
          params.put(component.trim(), "");
        }
      }
    }

    return params;
  }

  @Override
  public ExtendedLog getLog() {
    return log;
  }

  /**
   * Return the map of key:value pairs of strings making up the header for this
   * request.
   *
   * @return the map of all header key:value pairs
   */
  @Override
  public Multimap<String, String> getHeaders() {
    if (headers == null) {
      buildHeaders();
    }
    return headers;
  }

  /**
   * Get the values of the header string associated with the given key.
   *
   * @param key
   *          the key for the desired headers
   *
   * @return the set of values for the header, or {@code null} if the key isn't
   *         present
   */
  @Override
  public Set<String> getHeader(String key) {
    if (headers == null) {
      buildHeaders();
    }
    if (headers.containsKey(key.toLowerCase())) {
      return Sets.newHashSet(headers.get(key));
    }

    return null;
  }

  /**
   * Build up the request headers.
   */
  private void buildHeaders() {
    headers = HashMultimap.create();

    for (Entry<String, String> header : request.headers()) {
      headers.put(header.getKey().toLowerCase(), header.getValue());
    }
  }

  @Override
  public HttpCookie getCookie(String key) {
    Collection<HttpCookie> cookies = getCookies();
    for (HttpCookie cookie : cookies) {
      if (key.equals(cookie.getName())) {
        return cookie;
      }
    }

    return null;
  }

  @Override
  public Set<HttpCookie> getCookies() {

    Collection<String> cookieHeader = getHeader("Cookie");
    if (cookieHeader == null) {
      return new HashSet<>();
    }
    Collection<HttpCookie> cookies = new HashSet<>();
    for (String cookie : cookieHeader) {
      cookies.addAll(Collections2.transform(new CookieDecoder().decode(cookie),
          new Function<Cookie, HttpCookie>() {
            @Override
            public HttpCookie apply(final Cookie cookie) {
              return convertFromNettyCookie(cookie);
            }
          }));
    }
    return Sets.newHashSet(cookies);
  }

  /**
   * Convert a Netty cookie to a Java HTTP cookie.
   *
   * @param cookie
   *          the Netty cookie
   *
   * @return the Java cookie
   */
  private HttpCookie convertFromNettyCookie(Cookie cookie) {
    HttpCookie httpCookie = new HttpCookie(cookie.getName(), cookie.getValue());
    httpCookie.setComment(cookie.getComment());
    httpCookie.setDomain(cookie.getDomain());
    httpCookie.setMaxAge(cookie.getMaxAge());
    httpCookie.setPath(cookie.getPath());
    httpCookie.setPortlist(createPortString(cookie.getPorts()));
    httpCookie.setVersion(cookie.getVersion());
    httpCookie.setSecure(cookie.isSecure());
    httpCookie.setDiscard(cookie.isDiscard());

    return httpCookie;
  }

  /**
   * Create the port string for a cookie.
   *
   * @param ports
   *          the set of ports from the Netty cookie
   *
   * @return the port string for Java cookies
   */
  private String createPortString(Set<Integer> ports) {
    StringBuilder portString = new StringBuilder();
    Iterator<Integer> iter = ports.iterator();
    while (iter.hasNext()) {
      portString.append(String.valueOf(iter.next()));
      if (iter.hasNext()) {
        portString.append(",");
      }
    }

    return portString.toString();
  }

  @Override
  public String toString() {
    return "NettyHttpRequest [remoteAddress=" + getRemoteAddress() + ", uri=" + getUri()
        + ", headers=" + getHeaders() + ", cookies=" + getCookies() + "]";
  }

}
