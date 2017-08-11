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

import org.apache.commons.logging.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * A basic {@link DirectoryWatcher}.
 *
 * @author Keith M. Hughes
 */
public class SimpleDirectoryWatcher implements DirectoryWatcher, Runnable {

  /**
   * The directories being watched.
   */
  private final List<File> directoriesWatched = new ArrayList<>();

  /**
   * The files seen on the last round.
   */
  private Map<File, Long> filesSeen = new HashMap<>();

  /**
   * The listeners.
   */
  private final List<DirectoryWatcherListener> listeners = new ArrayList<>();

  /**
   * The future used for scheduling the scanning.
   */
  private ScheduledFuture<?> scanningFuture;

  /**
   * {@code true} if the directories should be cleaned before they are watched.
   */
  private boolean cleanFirst = false;

  /**
   * {@code true} if the watcher should stop when there is an exception.
   */
  private boolean stopOnException = true;

  /**
   * The logger to use.
   */
  private ExtendedLog log;

  /**
   * The file support to use.
   */
  private FileSupport fileSupport = FileSupportImpl.INSTANCE;

  /**
   * Construct a new SimpleWatcher.
   *
   * <p>
   * Directories will not be cleaned before they are watched.
   */
  public SimpleDirectoryWatcher() {
    this(false);
  }

  /**
   * Construct a new SimpleWatcher.
   *
   * @param cleanFirst
   *          {@code true} if added directories are cleaned before they are
   *          watched
   */
  public SimpleDirectoryWatcher(boolean cleanFirst) {
    this(cleanFirst, null);
  }

  /**
   * Construct a new SimpleWatcher.
   *
   * @param cleanFirst
   *          {@code true} if added directories are cleaned before they are
   *          watched
   * @param log
   *          the logger to use
   */
  public SimpleDirectoryWatcher(boolean cleanFirst, ExtendedLog log) {
    this.log = log;
    setCleanFirst(cleanFirst);
  }

  @Override
  public synchronized void startup(SmartSpacesEnvironment environment, long period, TimeUnit unit) {
    // If no log was set, we will use the space environment log
    if (log == null) {
      log = environment.getLog();
    }
    scanningFuture = environment.getExecutorService().scheduleAtFixedRate(this, 0, period, unit);
  }

  @Override
  public Set<File> startupWithScan(SmartSpacesEnvironment environment, long period, TimeUnit unit) {
    Set<File> currentScan = scanAllDirectories();

    for (File file : currentScan) {
      filesSeen.put(file, file.lastModified());
    }

    startup(environment, period, unit);

    return currentScan;
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
      // Assume the directory will be added at some point
      directoriesWatched.add(directory);
    }
  }

  @Override
  public synchronized void addDirectoryWatcherListener(DirectoryWatcherListener listener) {
    listeners.add(listener);
  }

  @Override
  public synchronized void removeDirectoryWatcherListener(DirectoryWatcherListener listener) {
    listeners.remove(listener);
  }

  @Override
  public synchronized void scan() {
    Set<File> currentScan = scanAllDirectories();

    findRemovedFiles(currentScan);
    findAddedFiles(currentScan);
  }

  /**
   * Find all files removed since the last scan.
   *
   * @param currentScan
   *          the files from the current scan
   */
  private void findRemovedFiles(Set<File> currentScan) {
    Set<File> filesRemoved = new HashSet<>();

    for (File fileFromLast : filesSeen.keySet()) {
      if (!currentScan.contains(fileFromLast)) {
        filesRemoved.add(fileFromLast);
      }
    }

    for (File removedFile : filesRemoved) {
      filesSeen.remove(removedFile);
      signalFileRemoved(removedFile);
    }
  }

  /**
   * Find all files added or modified since the last scan.
   * 
   * <p>
   * This modifies the seen map.
   *
   * @param currentScan
   *          the files from the current scan
   */
  private void findAddedFiles(Set<File> currentScan) {
    for (File fileFromCurrent : currentScan) {
      Long modifiedTime = fileFromCurrent.lastModified();
      Long lastModifiedTime = filesSeen.put(fileFromCurrent, modifiedTime);
      if (lastModifiedTime == null) {
        signalFileAdded(fileFromCurrent);
      } else if (!lastModifiedTime.equals(modifiedTime)) {
        signalFileModified(fileFromCurrent);
      }
    }
  }

  /**
   * Scan all directories for the files they contain.
   *
   * @return the set of all files which are currently in the folders
   */
  protected Set<File> scanAllDirectories() {
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
   * @param fileAdded
   *          the file which has been added
   */
  private void signalFileAdded(File fileAdded) {
    for (DirectoryWatcherListener listener : listeners) {
      try {
        listener.onFileAdded(fileAdded);
      } catch (Exception e) {
        log.formatError(e, "Exception while signalling file added %s", fileAdded.getAbsolutePath());
      }
    }
  }

  /**
   * Signal all listeners that a file has been added.
   *
   * @param fileAdded
   *          the file which has been added
   */
  private void signalFileModified(File fileModified) {
    for (DirectoryWatcherListener listener : listeners) {
      try {
        listener.onFileModified(fileModified);
      } catch (Throwable e) {
        log.formatError(e, "Exception while signalling file added %s",
            fileModified.getAbsolutePath());
      }
    }
  }

  /**
   * Signal all listeners that a file has been removed.
   *
   * @param fileRemoved
   *          the file which has been removed
   */
  private void signalFileRemoved(File fileRemoved) {
    for (DirectoryWatcherListener listener : listeners) {
      try {
        listener.onFileRemoved(fileRemoved);
      } catch (Throwable e) {
        log.formatError(e, "Exception while signalling file removed %s",
            fileRemoved.getAbsolutePath());
      }
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
