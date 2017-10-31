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

package io.smartspaces.service.web.server;

import io.smartspaces.SmartSpacesException;
import io.smartspaces.util.data.resource.CopyableResource;

import java.util.Map;

/**
 * The body of an HTTP POST.
 *
 * @author Keith M. Hughes
 */
public interface HttpPostBody extends CopyableResource {
  
  /**
   * Get the content type of the post.
   * 
   * @return the content type of the post
   */
  String getContentType();
  
  /**
   * Is the POST a multipart post?
   * 
   * @return {@code true} if multipart
   */
  boolean isMultipart();
  
  /**
   * Get the form name of the file upload.
   *
   * @return the upload name.
   */
  String getFormName();

  /**
   * Is there actually a file in the upload?
   *
   * @return {@code true} if a file was loaded.
   */
  boolean hasFile();

  /**
   * Get the file name the file had in a multipart upload.
   *
   * @return the file name, or {@code null} if there was no file
   */
  String getFilename();

  /**
   * Get the parameters which were part of the multipart upload.
   *
   * <p>
   * These are any text parameters in the HTTP form.
   *
   * @return the map
   */
  Map<String, String> getParameters();
  
  /**
   * Get the content of the POST.
   * 
   * <p>
   * This only returns a value if the POST was not a multipart .
   * 
   * @return the content of the POST
   * 
   * @throws SmartSpacesException this was a multipart POST.
   */
  byte[] getContent() throws SmartSpacesException;
}
