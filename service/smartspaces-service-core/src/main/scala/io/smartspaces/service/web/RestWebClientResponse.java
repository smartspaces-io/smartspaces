/*
 * Copyright (C) 2020 Keith M. Hughes
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

import java.util.Objects;

/**
 * A response to a { @link RestWebClient } call.
 *
 * @authoe Keith M. Hughes
 */
public class RestWebClientResponse {

  /**
   * The HTTP response code.
   */
  private final int responseCode;

  /**
   * The content of the response.
   */
  private final String responseContent;

  /**
   * Construct a new response.
   *
   * @param responseCode
   *        the HTTP response code
   * @param responseContent
   *        the content of the response, can be null
   */
  public RestWebClientResponse(int responseCode, String responseContent) {
    this.responseCode = responseCode;
    this.responseContent = responseContent;
  }

  public int getResponseCode() {
    return responseCode;
  }

  public String getResponseContent() {
    return responseContent;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    RestWebClientResponse that = (RestWebClientResponse) o;
    return responseCode == that.responseCode &&
        Objects.equals(responseContent, that.responseContent);
  }

  @Override
  public int hashCode() {
    return Objects.hash(responseCode, responseContent);
  }
}
