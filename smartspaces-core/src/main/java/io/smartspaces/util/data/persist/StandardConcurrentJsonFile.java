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

package io.smartspaces.util.data.persist;

import io.smartspaces.SmartSpacesException;
import io.smartspaces.util.data.json.JsonMapper;
import io.smartspaces.util.data.json.StandardJsonMapper;
import io.smartspaces.util.io.FileSupport;
import io.smartspaces.util.io.FileSupportImpl;

import java.io.File;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.google.common.collect.Maps;

/**
 * The standard implementation of a concurrent JSON file.
 *
 * @author Keith M. Hughes
 */
public class StandardConcurrentJsonFile implements ConcurrentJsonFile {

  /**
   * The JSON mapper.
   */
  private static final JsonMapper MAPPER = StandardJsonMapper.INSTANCE;

  /**
   * The file which stores the JSON.
   */
  private File file;

  /**
   * The read/write lock.
   *
   * <p>
   * This lock will be fair between reader and writer threads.
   */
  private ReadWriteLock rwlock = new ReentrantReadWriteLock(true);

  /**
   * The map.
   */
  private Map<String, Object> map = Maps.newHashMap();

  /**
   * The file support for file operations.
   */
  private FileSupport fileSupport = FileSupportImpl.INSTANCE;

  /**
   * Construct a new concurrent file.
   *
   * @param file
   *          the file system file
   */
  public StandardConcurrentJsonFile(File file) {
    this.file = file;
  }

  @Override
  public boolean load() throws SmartSpacesException {
    rwlock.readLock().lock();
    try {
      if (fileSupport.exists(file)) {
        String value = fileSupport.readFile(file);
        map = MAPPER.parseObject(value);
        return true;
      } else {
        return false;
      }
    } catch (Exception e) {
      throw new SmartSpacesException(String.format("Could not read %s", file), e);
    } finally {
      rwlock.readLock().unlock();
    }
  }

  @Override
  public Object get(String key) {
    rwlock.readLock().lock();
    try {
      return map.get(key);
    } finally {
      rwlock.readLock().unlock();
    }
  }

  @Override
  public Map<String, Object> getAll() {
    rwlock.readLock().lock();
    try {
      return Maps.newHashMap(map);
    } finally {
      rwlock.readLock().unlock();
    }
  }

  @Override
  public void save() throws SmartSpacesException {
    rwlock.writeLock().lock();
    try {
      fileSupport.writeFile(file, MAPPER.toString(map));
    } catch (Exception e) {
      throw new SmartSpacesException(String.format("Could not read %s", file), e);
    } finally {
      rwlock.writeLock().unlock();
    }
  }

  @Override
  public void replaceAll(Map<String, Object> newData) {
    rwlock.writeLock().lock();
    try {
      map.clear();
      map.putAll(newData);
    } finally {
      rwlock.writeLock().unlock();
    }
  }

  @Override
  public void put(String key, Object value) {
    rwlock.writeLock().lock();
    try {
      map.put(key, value);
    } finally {
      rwlock.writeLock().unlock();
    }
  }

  @Override
  public void putAll(Map<String, Object> values) {
    rwlock.writeLock().lock();
    try {
      for (Entry<String, Object> entry : values.entrySet()) {
        map.put(entry.getKey(), entry.getValue());
      }
    } finally {
      rwlock.writeLock().unlock();
    }
  }
}
