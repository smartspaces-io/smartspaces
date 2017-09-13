/*
 * Copyright (C) 2017 Keith M. Hughes
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

package io.smartspaces.evaluation;

import java.util.HashMap;
import java.util.Map;

/**
 * A simple implementation of a symbol table.
 * 
 * @author Keith M. Hughes
 */
public class SimpleSymbolTable<T> implements SymbolTable<T> {

  /**
   * The map of values for the environment.
   */
  private Map<String, T> values = new HashMap<>();

  @Override
  public T lookupSymbolValue(String symbolName) {
    return values.get(symbolName);
  }

  @Override
  public void setSymbolValue(String symbolName, T value) {
    values.put(symbolName, value);
  }
}
