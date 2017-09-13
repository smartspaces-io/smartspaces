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

/**
 * A table of symbols and their values.
 * 
 * @author Keith M. Hughes
 */
public interface SymbolTable<T> {

  /**
   * Look up the value of a variable.
   *
   * @param symbolName
   *          the name of the symbol to lookup
   *
   * @return the value of the symbol, or {@code null} if not found
   */
  T lookupSymbolValue(String symbolName);
  
  /**
   * Set the value of a variable.
   *
   * @param symbolName
   *          the name of the symbol
   * @param value
   *          the value of the symbol
   */
  void setSymbolValue(String symbolName, T value);
}
