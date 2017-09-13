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

package io.smartspaces.configuration;

import io.smartspaces.evaluation.EvaluationSmartSpacesException;
import io.smartspaces.evaluation.SymbolTable;

/**
 * Adapt a configuration to a string symbol table.
 * 
 * @author Keith M. Hughes
 */
public class ConfigurationSymbolTableAdapter implements SymbolTable<String> {

  /**
   * The configuration being adapted.
   */
  private Configuration configuration;

  /**
   * Construct an adapter.
   * 
   * @param configuration
   *          the configuration to be adapted
   */
  public ConfigurationSymbolTableAdapter(Configuration configuration) {
    this.configuration = configuration;
  }

  @Override
  public String lookupSymbolValue(String variable) throws EvaluationSmartSpacesException {
    return configuration.findProperty(variable);
  }

  @Override
  public void setSymbolValue(String symbolName, String value) {
    configuration.setProperty(symbolName, value);
  }

}
