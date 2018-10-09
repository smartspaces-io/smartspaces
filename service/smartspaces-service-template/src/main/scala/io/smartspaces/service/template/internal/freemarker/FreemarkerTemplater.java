/**
 * Copyright (C) 2016 Keith M. Hughes
 * Copyright (C) 2013 Google Inc.
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

package io.smartspaces.service.template.internal.freemarker;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Locale;
import java.util.Map;

import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapperBuilder;
import freemarker.template.Template;
import freemarker.template.Version;
import io.smartspaces.SimpleSmartSpacesException;
import io.smartspaces.SmartSpacesException;
import io.smartspaces.service.template.Templater;
import io.smartspaces.util.io.FileSupport;
import io.smartspaces.util.io.FileSupportImpl;

/**
 * A templater using Freemarker.
 *
 * @author Keith M. Hughes
 */
public class FreemarkerTemplater implements Templater {

  /**
   * The version of freemarker to use.
   */
  private static final Version FREEMARKER_VERSION = Configuration.VERSION_2_3_28;

  /**
   * The directory containing the templates.
   */
  private final File templateDirectory;

  /**
   * The configuration used by Freemarker.
   */
  private Configuration freemarkerConfig;

  /**
   * The file support to use.
   */
  private final FileSupport fileSupport = FileSupportImpl.INSTANCE;

  /**
   * Construct a new Freemarker templater.
   *
   * @param templateDirectory
   *        the directory containing the templates
   */
  public FreemarkerTemplater(File templateDirectory) {
    this.templateDirectory = templateDirectory;
  }

  @Override
  public synchronized void startup() {
    try {
      DefaultObjectWrapperBuilder objectWrapperBuilder =
          new DefaultObjectWrapperBuilder(FREEMARKER_VERSION);
      freemarkerConfig = new Configuration(FREEMARKER_VERSION);
      freemarkerConfig.setDirectoryForTemplateLoading(templateDirectory);
      // Specify how templates will see the data-model. This is an
      // advanced topic... but just use this:
      freemarkerConfig.setObjectWrapper(objectWrapperBuilder.build());
    } catch (Exception e) {
      freemarkerConfig = null;
      throw new SmartSpacesException("Cannot initialize Freemarker templater", e);
    }
  }

  @Override
  public synchronized void shutdown() {
    freemarkerConfig = null;
  }

  /**
   * Get the configuration to use, and also check that the system has been
   * started.
   *
   * @return freemarker configuration
   */
  private synchronized Configuration getConfiguration() {
    checkInitialized();
    return freemarkerConfig;
  }

  /**
   * Check to see that the template system has been properly initialized.
   */
  private void checkInitialized() {
    if (freemarkerConfig == null) {
      throw new SimpleSmartSpacesException("Templater has not been started");
    }
  }

  @Override
  public String instantiateTemplate(String templateName, Map<String, Object> data) {
    return instantiateTemplate(templateName, data, null);
  }

  @Override
  public String instantiateTemplate(String templateName, Map<String, Object> data, Locale locale) {
    checkInitialized();
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    Writer out = new OutputStreamWriter(baos);
    boolean noException = true;
    try {
      Template template = getConfiguration().getTemplate(templateName, locale);

      template.process(data, out);
    } catch (Exception e) {
      noException = false;
      throw new SmartSpacesException(
          String.format("Could not instantiate template %s", templateName), e);
    } finally {
      fileSupport.close(out, noException);
    }

    return new String(baos.toString());
  }

  @Override
  public void writeTemplate(String templateName, Map<String, Object> data, File outputFile) {
    writeTemplate(templateName, data, null, outputFile);
  }

  @Override
  public void writeTemplate(String templateName, Map<String, Object> data, Locale locale, File outputFile) {
    checkInitialized();
    Writer out = null;
    boolean noException = true;
    try {
      Template template = getConfiguration().getTemplate(templateName, locale);
      out = new FileWriter(outputFile);
      template.process(data, out);
    } catch (Exception e) {
      noException = false;
      throw new SmartSpacesException(
          String.format("Could not instantiate template %s", templateName), e);
    } finally {
      fileSupport.close(out, noException);
    }
  }
}
