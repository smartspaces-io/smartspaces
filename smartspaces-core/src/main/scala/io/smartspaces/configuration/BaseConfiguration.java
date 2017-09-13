/*
 * Copyright (C) 2016 Keith M. Hughes
 * Copyright (C) 2012 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package io.smartspaces.configuration;

import io.smartspaces.SmartSpacesException;
import io.smartspaces.evaluation.ExpressionEvaluator;
import io.smartspaces.evaluation.SymbolTable;
import io.smartspaces.util.data.json.JsonMapper;
import io.smartspaces.util.data.json.StandardJsonMapper;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Support for implementations of {@link Configuration}.
 *
 * @author Keith M. Hughes
 */
public abstract class BaseConfiguration implements Configuration {

  /**
   * The JSON mapper for JSON parsing.
   */
  private static final JsonMapper MAPPER = StandardJsonMapper.INSTANCE;

  /**
   * Parent configuration to this configuration.
   */
  private Configuration parent;

  /**
   * The expression evaluator for this configuration.
   */
  private ExpressionEvaluator expressionEvaluator;

  /**
   * Construct a new configuration.
   *
   * @param expressionEvaluator
   *          the expression evaluator for this configuration
   */
  public BaseConfiguration(ExpressionEvaluator expressionEvaluator) {
    this(expressionEvaluator, null);
  }

  /**
   * Construct a new configuration.
   *
   * @param expressionEvaluator
   *          the expression evaluator for this configuration
   * @param parent
   *          the parent (can be {@code null}
   */
  public BaseConfiguration(ExpressionEvaluator expressionEvaluator, Configuration parent) {
    this.expressionEvaluator = expressionEvaluator;
    this.parent = parent;
    
    expressionEvaluator.getEvaluationEnvironment().addSymbolTable(asSymbolTable());
  }

  @Override
  public ExpressionEvaluator getExpressionEvaluator() {
    return expressionEvaluator;
  }

  @Override
  public void setParent(Configuration parent) {
    this.parent = parent;
  }

  @Override
  public Configuration getParent() {
    return parent;
  }

  @Override
  public String evaluate(String expression) {
    return expressionEvaluator.evaluateStringExpression(expression);
  }

  @Override
  public String getPropertyString(String property) {
    return getPropertyValue(property);
  }

  @Override
  public String getPropertyString(String property, String defaultValue) {
    String value = getPropertyValue(property);
    if (value != null) {
      return value;
    } else {
      return defaultValue;
    }
  }

  @Override
  public String getRequiredPropertyString(String property) {
    String value = getPropertyValue(property);
    if (value != null) {
      return value;
    } else {
      throw new SmartSpacesException(String.format("Required property %s does not exist", property));
    }
  }

  @Override
  public Integer getPropertyInteger(String property, Integer defaultValue) {
    String value = getPropertyValue(property);
    if (value != null) {
      return Integer.valueOf(value);
    } else {
      return defaultValue;
    }
  }

  @Override
  public Integer getRequiredPropertyInteger(String property) {
    return Integer.valueOf(getRequiredPropertyString(property));
  }

  @Override
  public Long getPropertyLong(String property, Long defaultValue) {
    String value = getPropertyValue(property);
    if (value != null) {
      return Long.valueOf(value);
    } else {
      return defaultValue;
    }
  }

  @Override
  public Long getRequiredPropertyLong(String property) {
    return Long.valueOf(getRequiredPropertyString(property));
  }

  @Override
  public Double getPropertyDouble(String property, Double defaultValue) {
    String value = getPropertyValue(property);
    if (value != null) {
      return Double.valueOf(value);
    } else {
      return defaultValue;
    }
  }

  @Override
  public Double getRequiredPropertyDouble(String property) {
    return Double.valueOf(getRequiredPropertyString(property));
  }

  @Override
  public Boolean getPropertyBoolean(String property, Boolean defaultValue) {
    String value = getPropertyValue(property);
    if (value != null) {
      return getBooleanValue(value);
    } else {
      return defaultValue;
    }
  }

  @Override
  public Boolean getRequiredPropertyBoolean(String property) {
    return getBooleanValue(getRequiredPropertyString(property));
  }

  @Override
  public List<String> getPropertyStringList(String property, String delineator) {
    String value = getPropertyValue(property);
    if (value != null) {
      return Lists.newArrayList(value.split(delineator));
    } else {
      return null;
    }
  }

  @Override
  public Set<String> getPropertyStringSet(String property, String delineator) {
    String value = getPropertyValue(property);
    if (value != null) {
      return Sets.newHashSet(Arrays.asList(value.split(delineator)));
    } else {
      return null;
    }
  }

  @Override
  public <T> T getPropertyJson(String property) {
    String value = getPropertyValue(property);
    if (value != null) {
      @SuppressWarnings("unchecked")
      T result = (T) MAPPER.parse(value);
      return result;
    } else {
      return null;
    }
  }

  /**
   * Get the boolean value for the given string.
   *
   * @param value
   *          the string
   *
   * @return {@code true} if the string represents an Smart Spaces true value
   */
  private boolean getBooleanValue(String value) {
    return "true".equalsIgnoreCase(value);
  }

  @Override
  public boolean containsProperty(String property) {
    Configuration current = this;
    while (current != null) {
      if (current.containsPropertyLocally(property)) {
        return true;
      }

      current = current.getParent();
    }

    return false;
  }

  /**
   * Get the property from the actual implementation, evaluated as a string
   * expression.
   *
   * @param property
   *          name of the property
   *
   * @return the value of the property, or {@code null} if not found
   */
  private String getPropertyValue(String property) {
    String value = findProperty(property);

    if (value != null) {
      return expressionEvaluator.evaluateStringExpression(value);
    } else {
      return null;
    }
  }

  @Override
  public String findProperty(String property) {
    String value = null;

    Configuration current = this;
    while (current != null) {
      value = current.findPropertyLocally(property);
      if (value != null) {
        break;
      }
      current = current.getParent();
    }
    return value;
  }

  @Override
  public void setProperties(Map<String, String> values) {
    for (Entry<String, String> entry : values.entrySet()) {
      setProperty(entry.getKey(), entry.getValue());
    }
  }

  @Override
  public Map<String, String> getCollapsedMap() {
    Map<String, String> map = new HashMap<>();

    addCollapsedEntries(map);

    return map;
  }

  @Override
  public SymbolTable<String> asSymbolTable() {
    return new ConfigurationSymbolTableAdapter(this);
  }
}
