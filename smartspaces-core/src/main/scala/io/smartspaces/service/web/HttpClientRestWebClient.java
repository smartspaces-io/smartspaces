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
import com.google.common.io.Closeables;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.util.EntityUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * An {@link RestWebClient} which uses Apache HttpClient.
 *
 * @author Keith M. Hughes
 */
public class HttpClientRestWebClient implements RestWebClient {

  public static void main(String[] args) {
    HttpClientRestWebClient client = new HttpClientRestWebClient();
    client.startup();

    Map<String, String> authHeaders = new HashMap<>();
    authHeaders.put("Content-Type", "application/json");

    String authContent =
        "{\"grant_type\":\"password\", \"client_id\":\"bbfa4b1f12894473b5f24025e2d55290\", \"email\":\"keith@inhabitech.com\",\"password\":\"mu113#!\"}";
    String result = client.performPost("https://api.stacklighting.com/v0/oauth2/token", authContent,
        authHeaders);
    System.out.println(result);
  }

  /**
   * The default number of total connections.
   */
  public static final int TOTAL_CONNECTIONS_ALLOWED_DEFAULT = 20;

  /**
   * Number of bytes in the copy buffer.
   */
  private static final int BUFFER_SIZE = 4096;

  /**
   * The HTTPClient instance which does the actual transfer.
   */
  private HttpClient httpClient;

  /**
   * Connection manager for the client.
   */
  private ThreadSafeClientConnManager httpConnectionManager;

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
    httpConnectionManager = new ThreadSafeClientConnManager();
    httpConnectionManager.setDefaultMaxPerRoute(totalConnectionsAllowed);
    httpConnectionManager.setMaxTotal(totalConnectionsAllowed);

    httpClient = new DefaultHttpClient(httpConnectionManager);
  }

  @Override
  public void shutdown() {
    if (httpConnectionManager != null) {
      httpConnectionManager.shutdown();

      httpConnectionManager = null;
      httpClient = null;
    }
  }

  @Override
  public String performGet(String sourceUri) throws SmartSpacesException {
    return performGet(sourceUri, Charsets.UTF_8);
  }

  @Override
  public String performGet(String sourceUri, Charset charset) throws SmartSpacesException {
    StringRestPerformer performer = new StringRestPerformer(charset);

    HttpUriRequest request = new HttpGet(sourceUri);

    performer.performRequest(request);

    return performer.getResponse();
  }

  @Override
  public String performPut(String sourceUri, String putContent) throws SmartSpacesException {
    return performPut(sourceUri, putContent, Charsets.UTF_8);
  }

  @Override
  public String performPut(String sourceUri, String putContent, Charset charset)
      throws SmartSpacesException {
    StringRestPerformer performer = new StringRestPerformer(charset);

    try {
      HttpPut request = new HttpPut(sourceUri);
      request.setEntity(new StringEntity(putContent, charset.name()));

      performer.performRequest(request);

      return performer.getResponse();
    } catch (UnsupportedEncodingException e) {
      throw new SimpleSmartSpacesException(String.format(
          "REST call to %s failed. Character set %s not supported", sourceUri, charset.name()));
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
    StringRestPerformer performer = new StringRestPerformer(charset);

    try {
      HttpPost request = new HttpPost(sourceUri);
      request.setEntity(new StringEntity(postContent, charset.name()));

      if (headers != null) {
        for (Entry<String, String> entry : headers.entrySet()) {
          request.setHeader(entry.getKey(), entry.getValue());
        }
      }

      performer.performRequest(request);

      return performer.getResponse();
    } catch (UnsupportedEncodingException e) {
      throw new SimpleSmartSpacesException(String.format(
          "REST call to %s failed. Character set %s not supported", sourceUri, charset.name()));
    }
  }

  @Override
  public int getTotalConnectionsAllowed() {
    return totalConnectionsAllowed;
  }

  /**
   * A REST performer that gets String output.
   *
   * @author Keith M. Hughes
   */
  private class StringRestPerformer extends RestPerformer {

    /**
     * Output stream for the performer.
     */
    private ByteArrayOutputStream outputStream;

    /**
     * The charset for the bytes being read.
     */
    private Charset charset;

    /**
     * Construct a string performer.
     *
     * @param charset
     *          the charset the string is expected in
     */
    public StringRestPerformer(Charset charset) {
      this.charset = charset;
    }

    /**
     * Get the string from the result in the required charset.
     *
     * @return the content of the result
     */
    public String getResponse() {
      return new String(outputStream.toByteArray(), charset);
    }

    @Override
    protected OutputStream getOutputStream() throws IOException {
      outputStream = new ByteArrayOutputStream();

      return outputStream;
    }

    @Override
    protected String getDestinationDescription() {
      return "string result";
    }
  }

  /**
   * The performer for HTTP responses. Subclasses decide the ultimate
   * destination.
   *
   * @author Keith M. Hughes
   */
  private abstract class RestPerformer {

    /**
     * Create the output stream needed for the performer.
     *
     * @return the output steam to write to
     *
     * @throws IOException
     *           an exception happened when obtaining the stream
     */
    protected abstract OutputStream getOutputStream() throws IOException;

    /**
     * Get a description of the destination for error reporting.
     *
     * @return a description of the destination for error reporting
     */
    protected abstract String getDestinationDescription();

    /**
     * Get the remote content from the source URI using an HTTP GET.
     *
     * @param sourceUri
     *          the URI for the source content
     */
    public void performRequest(HttpUriRequest request) {
      HttpEntity entity = null;
      try {
        HttpResponse response = httpClient.execute(request);

        entity = response.getEntity();

        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode == HttpStatus.SC_OK) {
          if (entity != null) {
            InputStream in = entity.getContent();
            try {
              transferFile(in);

              in.close();
              in = null;
            } catch (IOException e) {
              throw new SmartSpacesException(
                  String.format("Exception during REST call %s", request.getURI()), e);
            } finally {
              Closeables.closeQuietly(in);
            }
          }
        } else {
          throw new SimpleSmartSpacesException(
              String.format("Server returned bad status code %d for REST call to URI %s",
                  statusCode, request.getURI()));
        }
      } catch (SmartSpacesException e) {
        throw e;
      } catch (Exception e) {
        throw new SmartSpacesException(
            String.format("Could not read REST URI %s", request.getURI()), e);
      } finally {
        if (entity != null) {
          try {
            EntityUtils.consume(entity);
          } catch (IOException e) {
            throw new SmartSpacesException(String
                .format("Could not consume entity content for REST call %s", request.getURI()), e);
          }
        }
      }
    }

    /**
     * Transfer the content from the HTTP input stream to the destination file.
     *
     * @param in
     *          the HTTP result
     *
     * @throws IOException
     *           something bad happened during IO operations
     */
    private void transferFile(InputStream in) throws IOException {
      OutputStream out = null;
      try {
        out = getOutputStream();

        byte[] buffer = new byte[BUFFER_SIZE];

        int len;
        while ((len = in.read(buffer)) > 0) {
          out.write(buffer, 0, len);
        }

        out.flush();

        out.close();
        out = null;
      } finally {
        if (out != null) {
          out.close();
        }
      }
    }

  }
}
