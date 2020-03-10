/*
 * Copyright (C) 2014 Keith M. Hughes
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

package io.smartspaces.service.web;

import io.smartspaces.SimpleSmartSpacesException;
import io.smartspaces.SmartSpacesException;

import com.google.common.base.Charsets;
import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.*;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.DnsResolver;
import org.apache.http.conn.HttpConnectionFactory;
import org.apache.http.conn.ManagedHttpClientConnection;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.DefaultHttpResponseParserFactory;
import org.apache.http.impl.conn.ManagedHttpClientConnectionFactory;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.impl.conn.SystemDefaultDnsResolver;
import org.apache.http.impl.io.DefaultHttpRequestWriterFactory;
import org.apache.http.io.HttpMessageParserFactory;
import org.apache.http.io.HttpMessageWriterFactory;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Map.Entry;

import javax.net.ssl.SSLContext;

/**
 * An {@link RestWebClient} which uses Apache HttpClient.
 *
 * @author Keith M. Hughes
 */
public class HttpClientRestWebClient implements RestWebClient {

  /**
   * The default number of total connections.
   */
  public static final int TOTAL_CONNECTIONS_ALLOWED_DEFAULT = 20;

  /**
   * The HTTPClient instance which does the actual transfer.
   */
  private CloseableHttpClient httpClient;

  /**
   * The total number of connections allowed.
   */
  private final int totalConnectionsAllowed;

  /**
   * Construct a performer which allows a maximum of
   * {@link #TOTAL_CONNECTIONS_ALLOWED_DEFAULT} connections.
   */
  public HttpClientRestWebClient() {
    this(TOTAL_CONNECTIONS_ALLOWED_DEFAULT);
  }

  /**
   * Construct a performer with a specified maximum number of connections.
   *
   * @param totalConnectionsAllowed
   *          the maximum total number of connections allowed
   */
  public HttpClientRestWebClient(int totalConnectionsAllowed) {
    this.totalConnectionsAllowed = totalConnectionsAllowed;
  }

  @Override
  public void startup() {
    HttpMessageParserFactory<HttpResponse> responseParserFactory =
        new DefaultHttpResponseParserFactory();

    HttpMessageWriterFactory<HttpRequest> requestWriterFactory =
        new DefaultHttpRequestWriterFactory();

    HttpConnectionFactory<HttpRoute, ManagedHttpClientConnection> connFactory =
        new ManagedHttpClientConnectionFactory(requestWriterFactory, responseParserFactory);

    SSLContext sslcontext = SSLContexts.createSystemDefault();

    // Create a registry of custom connection socket factories for supported
    // protocol schemes.
    Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder
        .<ConnectionSocketFactory>create().register("http", PlainConnectionSocketFactory.INSTANCE)
        .register("https", new SSLConnectionSocketFactory(sslcontext)).build();

    // Use custom DNS resolver to override the system DNS resolution.
    DnsResolver dnsResolver = new SystemDefaultDnsResolver();

    // Create a connection manager with custom configuration.
    PoolingHttpClientConnectionManager connManager =
        new PoolingHttpClientConnectionManager(socketFactoryRegistry, connFactory, dnsResolver);

    // Create socket configuration
    SocketConfig socketConfig = SocketConfig.custom().setTcpNoDelay(true).build();
    // Configure the connection manager to use socket configuration either
    // by default or for a specific host.
    connManager.setDefaultSocketConfig(socketConfig);

    // Validate connections after 1 sec of inactivity
    connManager.setValidateAfterInactivity(1000);
    connManager.setMaxTotal(totalConnectionsAllowed);

    httpClient = HttpClients.custom().setConnectionManager(connManager).build();
  }

  @Override
  public void shutdown() {
    if (httpClient != null) {
      try {
        httpClient.close();
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }

      httpClient = null;
    }
  }

  @Override
  public String performGet(String sourceUri, Map<String, String> headers)
      throws SmartSpacesException {
    return performGet(sourceUri, Charsets.UTF_8, headers);
  }

  @Override
  public String performGet(String sourceUri, Charset charset, Map<String, String> headers)
      throws SmartSpacesException {
    try {
      HttpGet request = new HttpGet(sourceUri);

      placeHeadersInRequest(headers, request);

      ResponseHandler<String> responseHandler = newResponseHandler(charset);

      return httpClient.execute(request, responseHandler);
    } catch (Exception e) {
      throw new SimpleSmartSpacesException(String.format("REST GET call to %s failed.", sourceUri), e);
    }
  }

  @Override
  public RestWebClientResponse performGetFull(String sourceUri, Map<String, String> headers)
      throws SmartSpacesException {
    return performGetFull(sourceUri, Charsets.UTF_8, headers);
  }

  @Override
  public RestWebClientResponse performGetFull(String sourceUri, Charset charset, Map<String, String> headers)
      throws SmartSpacesException {
    try {
      HttpGet request = new HttpGet(sourceUri);

      placeHeadersInRequest(headers, request);

      ResponseHandler<RestWebClientResponse> responseHandler = newResponseHandlerFull(charset);

      return httpClient.execute(request, responseHandler);
    } catch (Exception e) {
      throw new SimpleSmartSpacesException(String.format("REST GET call to %s failed.", sourceUri), e);
    }
  }

  @Override
  public String performPut(String sourceUri, String putContent, Map<String, String> headers)
      throws SmartSpacesException {
    return performPut(sourceUri, putContent, Charsets.UTF_8, headers);
  }

  @Override
  public String performPut(String sourceUri, String putContent, Charset charset,
      Map<String, String> headers) throws SmartSpacesException {

    try {
      HttpPut request = new HttpPut(sourceUri);
      request.setEntity(new StringEntity(putContent, charset.name()));

      placeHeadersInRequest(headers, request);

      ResponseHandler<String> responseHandler = newResponseHandler(charset);

      return httpClient.execute(request, responseHandler);
    } catch (Exception e) {
      throw new SimpleSmartSpacesException(String.format("REST call to %s failed.", sourceUri), e);
    }
  }

  @Override
  public RestWebClientResponse performPutFull(String sourceUri, String putContent, Map<String, String> headers)
      throws SmartSpacesException {
    return performPutFull(sourceUri, putContent, Charsets.UTF_8, headers);
  }

  @Override
  public RestWebClientResponse performPutFull(String sourceUri, String putContent, Charset charset,
                           Map<String, String> headers) throws SmartSpacesException {

    try {
      HttpPut request = new HttpPut(sourceUri);
      request.setEntity(new StringEntity(putContent, charset.name()));

      placeHeadersInRequest(headers, request);

      ResponseHandler<RestWebClientResponse> responseHandler = newResponseHandlerFull(charset);

      return httpClient.execute(request, responseHandler);
    } catch (Exception e) {
      throw new SimpleSmartSpacesException(String.format("REST PUT call to %s failed.", sourceUri), e);
    }
  }

  @Override
  public String performDelete(String sourceUri,
                              Map<String, String> headers) throws SmartSpacesException {
    return performDelete(sourceUri, Charsets.UTF_8,  headers);
  }

  @Override
  public String performDelete(String sourceUri, Charset charset,
                           Map<String, String> headers) throws SmartSpacesException {

    try {
      HttpDelete request = new HttpDelete(sourceUri);

      placeHeadersInRequest(headers, request);

      ResponseHandler<String> responseHandler = newResponseHandler(charset);

      return httpClient.execute(request, responseHandler);
    } catch (Exception e) {
      throw new SimpleSmartSpacesException(String.format("REST call to %s failed.", sourceUri), e);
    }
  }

  @Override
  public RestWebClientResponse performDeleteFull(String sourceUri,
                              Map<String, String> headers) throws SmartSpacesException {
    return performDeleteFull(sourceUri, Charsets.UTF_8,  headers);
  }

  @Override
  public RestWebClientResponse performDeleteFull(String sourceUri, Charset charset,
                              Map<String, String> headers) throws SmartSpacesException {

    try {
      HttpDelete request = new HttpDelete(sourceUri);

      placeHeadersInRequest(headers, request);

      ResponseHandler<RestWebClientResponse> responseHandler = newResponseHandlerFull(charset);

      return httpClient.execute(request, responseHandler);
    } catch (Exception e) {
      throw new SimpleSmartSpacesException(String.format("REST call to %s failed.", sourceUri), e);
    }
  }

  @Override
  public String performPost(String sourceUri, String postContent, Map<String, String> headers)
      throws SmartSpacesException {
    return performPost(sourceUri, postContent, Charsets.UTF_8, headers);
  }

  @Override
  public String performPost(String sourceUri, String postContent, Charset charset,
      Map<String, String> headers) throws SmartSpacesException {

    return performPost(sourceUri, new StringEntity(postContent, charset.name()), charset, headers);
  }

  @Override
  public RestWebClientResponse performPostFull(String sourceUri, String postContent, Map<String, String> headers)
      throws SmartSpacesException {
    return performPostFull(sourceUri, postContent, Charsets.UTF_8, headers);
  }

  @Override
  public RestWebClientResponse performPostFull(String sourceUri, String postContent, Charset charset,
                            Map<String, String> headers) throws SmartSpacesException {

    return performPostFull(sourceUri, new StringEntity(postContent, charset.name()), charset, headers);
  }

  /**
   * Perform a POST operation.
   *
   * @param sourceUri
   *        the URI for the POST
   * @param postContent
   *        the content of the POST, can be {@code null}
   * @param charset
   *        the charset for encoding
   * @param headers
   *        the headers for the call
   *
   * @return the response
   *
   * @throws SmartSpacesException
   */
  private String performPost(String sourceUri, HttpEntity postContent, Charset charset,
      Map<String, String> headers) throws SmartSpacesException {

    try {
      HttpPost request = new HttpPost(sourceUri);
      request.setEntity(postContent);

      placeHeadersInRequest(headers, request);

      ResponseHandler<String> responseHandler = newResponseHandler(charset);

      return httpClient.execute(request, responseHandler);
    } catch (Exception e) {
      throw new SimpleSmartSpacesException(String.format("REST POST call to %s failed.", sourceUri), e);
    }
  }

  /**
   * Perform a POST operation.
   *
   * @param sourceUri
   *        the URI for the POST
   * @param postContent
   *        the content of the POST, can be {@code null}
   * @param charset
   *        the charset for encoding
   * @param headers
   *        the headers for the call
   *
   * @return the response
   *
   * @throws SmartSpacesException
   */
  private RestWebClientResponse performPostFull(String sourceUri, HttpEntity postContent, Charset charset,
                             Map<String, String> headers) throws SmartSpacesException {

    try {
      HttpPost request = new HttpPost(sourceUri);
      request.setEntity(postContent);

      placeHeadersInRequest(headers, request);

      ResponseHandler<RestWebClientResponse> responseHandler = newResponseHandlerFull(charset);

      return httpClient.execute(request, responseHandler);
    } catch (Exception e) {
      throw new SimpleSmartSpacesException(String.format("REST POST call to %s failed.", sourceUri), e);
    }
  }

  @Override
  public int getTotalConnectionsAllowed() {
    return totalConnectionsAllowed;
  }

  /**
   * Place all headers into the request.
   * 
   * @param headers
   *          the headers, can be {@code null}
   * @param request
   *          the request
   */
  private void placeHeadersInRequest(Map<String, String> headers, HttpUriRequest request) {
    if (headers != null) {
      for (Entry<String, String> entry : headers.entrySet()) {
        request.setHeader(entry.getKey(), entry.getValue());
      }
    }
  }

  /**
   * Create a new response handler that just returns content.
   * 
   * @param charset
   *          the charset for the response
   * 
   * @return the response handler
   */
  private ResponseHandler<String> newResponseHandler(Charset charset) {
    ResponseHandler<String> responseHandler = new ResponseHandler<String>() {
      @Override
      public String handleResponse(final HttpResponse response)
          throws ClientProtocolException, IOException {
        int status = response.getStatusLine().getStatusCode();
        if (status >= 200 && status < 300) {
          HttpEntity entity = response.getEntity();
          return entity != null ? EntityUtils.toString(entity, charset) : null;
        } else {
          throw new HttpResponseException(status, "Unexpected response status: " + status);
        }
      }
    };
    return responseHandler;
  }

  /**
   * Create a new response handler that returns a full response.
   *
   * @param charset
   *          the charset for the response
   *
   * @return the response handler
   */
  private ResponseHandler<RestWebClientResponse> newResponseHandlerFull(Charset charset) {
    ResponseHandler<RestWebClientResponse> responseHandler = new ResponseHandler<RestWebClientResponse>() {
      @Override
      public RestWebClientResponse handleResponse(final HttpResponse response)
          throws ClientProtocolException, IOException {
        int status = response.getStatusLine().getStatusCode();
        HttpEntity entity = response.getEntity();
        String content = entity != null ? EntityUtils.toString(entity, charset) : null;

        return new RestWebClientResponse(status, content);
       }
    };
    return responseHandler;
  }
}
