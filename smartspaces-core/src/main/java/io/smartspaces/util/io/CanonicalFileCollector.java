/*
 * Copyright (C) 2016 Keith M. Hughes
 * Copyright (C) 2015 Google Inc.
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

package io.smartspaces.util.io;

import io.smartspaces.SimpleSmartSpacesException;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;

/**
 * Simple version of a file map that converts everything to a cannonical file as
 * a key, so that duplicate files are not present in the key map. This can
 * happen, for example, when there is a simple '.' in the file path somewhere,
 * which causes two files to look different when they're really the same.
 */
public class CanonicalFileCollector implements FileCollector {

  /**
   * Internal map for keeping a set of source files.
   */
  private Map<File, File> fileMap = Maps.newHashMap();

  @Override
  public void put(File destination, File source) {
    try {
      fileMap.put(destination.getCanonicalFile(), source);
    } catch (IOException e) {
      throw SimpleSmartSpacesException.newFormattedException(
          "Error getting canonical version of %s", destination, e);
    }
  }

  @Override
  public Set<Map.Entry<File, File>> entrySet() {
    return fileMap.entrySet();
  }
}
