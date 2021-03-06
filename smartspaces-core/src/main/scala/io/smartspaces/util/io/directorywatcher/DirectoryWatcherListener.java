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

package io.smartspaces.util.io.directorywatcher;

import java.io.File;

/**
 * A listener for {@link DirectoryWatcher} events.
 *
 * @author Keith M. Hughes
 */
public interface DirectoryWatcherListener {

  /**
   * A file has been added since the last scan.
   *
   * @param file
   *          the file which was added
   */
  void onFileAdded(File file);

  /**
   * A file has been changed since the last scan.
   *
   * @param file
   *          the file which was added
   */
  void onFileModified(File file);

  /**
   * A file has been removed since the last scan.
   *
   * @param file
   *          the file which was removed
   */
  void onFileRemoved(File file);
}
