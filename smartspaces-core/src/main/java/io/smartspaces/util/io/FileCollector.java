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

import java.io.File;
import java.util.Map;
import java.util.Set;

/**
 * Interface used to collect file operations for creating output maps.
 */
public interface FileCollector {

  /**
   * Put an entry into the collection. Designed to be compatible with
   * {@link Map.put}
   *
   * @param dest
   *          destination file
   * @param src
   *          source file
   */
  void put(File dest, File src);

  /**
   * Get the entry set of collected files.
   *
   * @return collected files
   */
  Set<Map.Entry<File, File>> entrySet();
}
