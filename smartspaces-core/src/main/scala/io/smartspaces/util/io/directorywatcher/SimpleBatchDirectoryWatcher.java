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

import io.smartspaces.logging.ExtendedLog;
import io.smartspaces.system.SmartSpacesEnvironment;
import io.smartspaces.util.io.FileSupport;
import io.smartspaces.util.io.FileSupportImpl;

import com.google.common.collect.Sets;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * A basic {@link BatchDirectoryWatcher}.
 *
 * @author Keith M. Hughes
 */
public class SimpleBatchDirectoryWatcher implements BatchDirectoryWatcher, Runnable {

  /**
   * {@code true} if the directories should be cleaned before they are watched.
   */
  private boolean cleanFirst = false;

  /**
   * {@code true} if the watcher should stop when there is an exception.
   */
  private boolean stopOnException = true;

  /**
   * The directories being watched.
   */
  private final List<File> directoriesWatched = new ArrayList<>();

  /**
   * The files seen on the last round.
   */
  private Set<File> filesLastScanned = new HashSet<>();

  /**
   * The listeners.
   */
  private final List<BatchDirectoryWatcherListener> listeners = new ArrayList<>();

  /**
   * The future used for scheduling the scanning.
   */
  private ScheduledFuture<?> scanningFuture;

  /**
   * The logger to use.
   */
  private ExtendedLog log;

  /**
   * The file support to use.
   */
  private FileSupport fileSupport = FileSupportImpl.INSTANCE;

  @Override
  public synchronized void startup(SmartSpacesEnvironment environment, long period, TimeUnit unit) {
    if (log == null) {
      log = environment.getLog();
    }
    
    scanningFuture = environment.getExecutorService().scheduleAtFixedRate(this, 0, period, unit);
  }

  @Override
  public Set<File> startupWithScan(SmartSpacesEnvironment environment, long period, TimeUnit unit) {
    filesLastScanned = scanAllDirectories();

    startup(environment, period, unit);

    return Sets.newHashSet(filesLastScanned);
  }

  @Override
  public synchronized void shutdown() {
    if (scanningFuture != null) {
      scanningFuture.cancel(false);
      scanningFuture = null;
    }
  }

  @Override
  public synchronized void addDirectory(File directory) {
    if (directory.isDirectory()) {
      if (directory.canRead()) {
        if (directory.canWrite()) {
          if (cleanFirst) {
            fileSupport.deleteDirectoryContents(directory);
          }
        }
        directoriesWatched.add(directory);
      } else {
        throw new IllegalArgumentException(String.format("%s is not readable", directory));
      }
    } else if (directory.exists()) {
      // The file exists, but it isn't a directory.
      //
      // This is checked for separately to handle directories added
      // after the watcher starts running.
      throw new IllegalArgumentException(String.format("%s is not a directory", directory));
    } else {
      // It doesn't exist yet. Assume it eventually will.
      directoriesWatched.add(directory);
    }
  }

  @Override
  public synchronized void
      addBatchDirectoryWatcherListener(BatchDirectoryWatcherListener listener) {
    listeners.add(listener);
  }

  @Override
  public synchronized void
      removeBatchDirectoryWatcherListener(BatchDirectoryWatcherListener listener) {
    listeners.remove(listener);
  }

  @Override
  public synchronized void scan() {
    Set<File> currentScan = scanAllDirectories();

    findAddedFiles(currentScan);

    filesLastScanned = currentScan;
  }

  /**
   * Find all files added since the last scan.
   *
   * @param currentScan
   *          the files from the current scan
   */
  private void findAddedFiles(Set<File> currentScan) {
    Set<File> filesAdded = new HashSet<>();
    for (File fileFromCurrent : currentScan) {
      if (!filesLastScanned.contains(fileFromCurrent)) {
        filesAdded.add(fileFromCurrent);
      }
    }
    signalFileAdded(filesAdded);
  }

  /**
   * Scan all directories for the files they contain.
   *
   * @return the set of all files which are currently in the folders
   */
  private Set<File> scanAllDirectories() {
    Set<File> currentScan = new HashSet<>();
    for (File directory : directoriesWatched) {
      if (directory.isDirectory()) {
        File[] files = directory.listFiles();
        if (files != null) {
          for (File file : files) {
            currentScan.add(file);
          }
        }
      }
    }

    return currentScan;
  }

  /**
   * Signal all listeners that a file has been added.
   *
   * @param filesAdded
   *          the files which have been added
   */
  private void signalFileAdded(Set<File> filesAdded) {
    for (BatchDirectoryWatcherListener listener : listeners) {
      listener.onFilesAdded(filesAdded);
    }
  }

  @Override
  public void run() {
    try {
      scan();
    } catch (Throwable e) {
      log.error("Exception happened during directory watcher scan", e);

      if (stopOnException) {
        // TODO(keith): Not entirely happy with this. maybe
        // eventually put the future into this instance and shut it
        // down.
        throw new RuntimeException();
      }
    }
  }

  @Override
  public void setCleanFirst(boolean cleanFirst) {
    this.cleanFirst = cleanFirst;
  }

  @Override
  public void setStopOnException(boolean stopOnException) {
    this.stopOnException = stopOnException;
  }
}
