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

package io.smartspaces.configuration;

import io.smartspaces.evaluation.ExpressionEvaluator;
import io.smartspaces.evaluation.SimpleExpressionEvaluator;
import io.smartspaces.expression.language.ssel.SselExpressionEvaluatorFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * A simple {@link Configuration}.
 *
 * @author Keith M. Hughes
 */
public class SimpleConfiguration extends BaseConfiguration {

  /**
   * Map of locally defined values.
   */
  private Map<String, String> values = new HashMap<String, String>();

  /**
   * Create a {@link SimpleConfiguration} using a
   * {@link SimpleExpressionEvaluator} pointing at the configuration.
   *
   * @return the newly created configuration
   */
  public static SimpleConfiguration newConfiguration() {
    ExpressionEvaluator expressionEvaluator = new SselExpressionEvaluatorFactory().newEvaluator();
 
    SimpleConfiguration configuration = new SimpleConfiguration(expressionEvaluator);

    return configuration;
  }

  /**
   * Construct a new configuration.
   *
   * @param expressionEvaluator
   *          the expression evaluator for this configuration
   */
  public SimpleConfiguration(ExpressionEvaluator expressionEvaluator) {
    this(expressionEvaluator, null);
  }

  /**
   * Construct a new configuration.
   *
   * @param expressionEvaluator
   *          the expression evaluator for this configuration
   * @param parent
   *          the parent configuration
   */
  public SimpleConfiguration(ExpressionEvaluator expressionEvaluator, Configuration parent) {
    super(expressionEvaluator, parent);
  }

  @Override
  public boolean containsPropertyLocally(String property) {
    return values.containsKey(property);
  }

  @Override
  public String findPropertyLocally(String property) {
    return values.get(property);
  }

  @Override
  public void setProperty(String property, String value) {
    values.put(property, value);
  }

  @Override
  public void clear() {
    values.clear();
  }

  @Override
  public void addCollapsedEntries(Map<String, String> map) {
    Configuration parent = getParent();
    if (parent != null) {
      parent.addCollapsedEntries(map);
    }

    map.putAll(values);
  }
}
