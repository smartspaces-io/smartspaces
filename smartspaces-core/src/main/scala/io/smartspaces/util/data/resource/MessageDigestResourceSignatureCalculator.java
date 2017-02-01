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

package io.smartspaces.util.data.resource;

import io.smartspaces.SimpleSmartSpacesException;
import io.smartspaces.SmartSpacesException;
import io.smartspaces.util.ByteUtils;
import io.smartspaces.util.io.FileSupport;
import io.smartspaces.util.io.FileSupportImpl;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.security.MessageDigest;

import com.google.common.io.Closeables;

/**
 * A resource signature calculator using message digests.
 *
 * @author Keith M. Hughes
 */
public class MessageDigestResourceSignatureCalculator implements ResourceSignatureCalculator {

  /**
   * Buffer size for digesting stream.
   */
  private static final int COPY_BUFFER_SIZE = 4096;

  /**
   * Digest signature algorithm.
   */
  public static final String SIGNATURE_ALGORITHM = "SHA-512";

  /**
   * The file support to use.
   */
  private FileSupport fileSupport = FileSupportImpl.INSTANCE;

  @Override
  public String getResourceSignature(URI resourceUri) {
    try {
      if (FileSupport.URI_SCHEME_FILE.equals(resourceUri.getScheme())) {
        return getResourceSignature(fileSupport.newFile(resourceUri.toURL().getFile()));
      }

      return null;
    } catch (MalformedURLException e) {
      throw SmartSpacesException.newFormattedException(e, "Could not obtain URL for resource %s",
          resourceUri.toString());
    }
  }

  @Override
  public String getResourceSignature(File resourceFile) {
    FileInputStream fis = null;

    try {
      fis = new FileInputStream(resourceFile);
      return getResourceSignature(fis);
    } catch (Exception e) {
      throw new SimpleSmartSpacesException(String.format("Could not create signature for file %s",
          resourceFile.getAbsolutePath()), e);
    } finally {
      Closeables.closeQuietly(fis);
    }
  }

  @Override
  public String getResourceSignature(InputStream inputStream) {
    try {
      byte[] buffer = new byte[COPY_BUFFER_SIZE];
      MessageDigest digest = MessageDigest.getInstance(SIGNATURE_ALGORITHM);
      int len;
      while ((len = inputStream.read(buffer)) > 0) {
        digest.update(buffer, 0, len);
      }
      return ByteUtils.toHexString(digest.digest());
    } catch (Throwable e) {
      throw new SimpleSmartSpacesException("Could not calculate stream signature", e);
    }
  }
}
