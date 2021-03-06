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

package io.smartspaces.service.script.internal;

import com.google.common.collect.Sets;
import io.smartspaces.SimpleSmartSpacesException;
import io.smartspaces.SmartSpacesException;
import io.smartspaces.activity.Activity;
import io.smartspaces.activity.ActivityFilesystem;
import io.smartspaces.configuration.Configuration;
import io.smartspaces.service.BaseSupportedService;
import io.smartspaces.service.script.*;
import io.smartspaces.service.script.internal.javascript.RhinoJavascriptActivityScriptFactory;
import org.codehaus.groovy.jsr223.GroovyScriptEngineFactory;

import javax.script.*;
import java.util.*;

/**
 * An {@link ScriptService} using {@code javax.script}.
 *
 * @author Keith M. Hughes
 */
public class JavaxScriptScriptService extends BaseSupportedService implements ScriptService {

  /**
   * All engines stored in the script engine.
   */
  private final List<ScriptLanguage> languages = new ArrayList<>();

  /**
   * A mapping from names of languages to the factory.
   */
  private final Map<String, ScriptLanguage> nameToLanguage = new HashMap<>();

  /**
   * A mapping from names of extensions to the factory.
   */
  private final Map<String, ScriptLanguage> extensionToLanguage = new HashMap<>();

  @Override
  public String getName() {
    return ScriptService.SERVICE_NAME;
  }

  @Override
  public void startup() {
    final ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
    Thread.currentThread().setContextClassLoader(null);
    try {
      // TODO(keith): Create a bundle listener which will scan bundles for
      // registered factories.
      GroovyScriptEngineFactory groovyScriptEngineFactory = new GroovyScriptEngineFactory();
      registerLanguage("groovy", groovyScriptEngineFactory, null);
      //registerLanguage("python", new PyScriptEngineFactory(), new PythonActivityScriptFactory(
      //    getSpaceEnvironment()));
      // registerLanguage("javascript",
      // new RhinoJavascriptScriptEngineFactory(),
      // new RhinoJavascriptActivityScriptFactory());

      // Get any languages, other than Javascript, that
      // might be available.
      ScriptEngineManager mgr = new ScriptEngineManager();
      for (ScriptEngineFactory factory : mgr.getEngineFactories()) {
        if ("ECMAScript".equals(factory.getLanguageName())) {
          registerLanguage("javascript", factory, new RhinoJavascriptActivityScriptFactory(
              getSpaceEnvironment()));
        } else {
          registerLanguage(factory.getLanguageName(), factory, null);
        }
      }
    } finally {
      Thread.currentThread().setContextClassLoader(oldClassLoader);
    }
  }

  /**
   * Register a new scripting engine factory with the factory.
   *
   * @param languageName
   *          the name of the language
   * @param scriptEngineFactory
   *          the JSR 223 scripting factory
   * @param activityScriptFactory
   *          the factory for making scripted activities
   */
  private void registerLanguage(String languageName, ScriptEngineFactory scriptEngineFactory,
      ActivityScriptFactory activityScriptFactory) {
    ScriptLanguage language = new ScriptLanguage(scriptEngineFactory, activityScriptFactory);
    languages.add(language);

    nameToLanguage.put(scriptEngineFactory.getLanguageName(), language);
    for (String name : scriptEngineFactory.getNames()) {
      nameToLanguage.put(name, language);
    }
    for (String extension : scriptEngineFactory.getExtensions()) {
      extensionToLanguage.put(extension, language);
    }

    if (activityScriptFactory != null) {
      activityScriptFactory.initialize();
    }
  }

  @Override
  public Set<String> getLanguageNames() {
    Set<String> languages = Sets.newHashSet(nameToLanguage.keySet());

    return languages;
  }

  @Override
  public void executeSimpleScript(String languageName, String script) {
    executeScript(languageName, script, EMPTY_BINDINGS);
  }

  @Override
  public void executeScript(String languageName, String script, Map<String, Object> bindings) {
    executeScriptByName(languageName, new StringScriptSource(script), bindings);
  }

  @Override
  public void executeScriptByName(String languageName, ScriptSource scriptSource,
      Map<String, Object> bindings) {
    ScriptLanguage scriptLanguage = nameToLanguage.get(languageName);
    if (scriptLanguage == null) {
      throw new SimpleSmartSpacesException(String.format(
          "Unable to find a script engine for language %s", languageName));
    }

    executeScript(scriptLanguage, scriptSource, bindings, "language", languageName);
  }

  @Override
  public void executeScriptByExtension(String extension, ScriptSource scriptSource,
      Map<String, Object> bindings) {
    ScriptLanguage scriptLanguage = extensionToLanguage.get(extension);
    if (scriptLanguage == null) {
      throw new SimpleSmartSpacesException(String.format(
          "Unable to find a script engine for language extension %s", extension));
    }

    executeScript(scriptLanguage, scriptSource, bindings, "extension", extension);
  }

  /**
   * Execute a script.
   *
   * @param scriptLanguage
   *          the scripting language that will run the script
   * @param scriptSource
   *          the source of the script
   * @param bindings
   *          the extra bindings for the script
   * @param idType
   *          the type of language ID which was used
   * @param languageId
   *          the ID used to get the language
   *
   */
  private void executeScript(ScriptLanguage scriptLanguage, ScriptSource scriptSource,
      Map<String, Object> bindings, String idType, String languageId) {

    ScriptEngineFactory factory = scriptLanguage.getScriptEngineFactory();
    if (factory == null) {
      throw new SmartSpacesException(String.format("Unable to find a script engine for %s %s",
          idType, languageId));
    }

    final ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
    Thread.currentThread().setContextClassLoader(JavaxScriptScriptService.class.getClassLoader());
    try {
      ScriptEngine engine = factory.getScriptEngine();
      if (engine != null) {
        engine.setBindings(new SimpleBindings(bindings), ScriptContext.GLOBAL_SCOPE);

        engine.eval(scriptSource.getScriptContents());
      }
    } catch (ScriptException ex) {
      ex.printStackTrace();
    } finally {
      Thread.currentThread().setContextClassLoader(oldClassLoader);
    }
  }

  @Override
  public Script newSimpleScript(String languageName, String script) {
    return newScriptByName(languageName, new StringScriptSource(script));
  }

  @Override
  public Script newScriptByName(String languageName, ScriptSource scriptSource) {
    ScriptLanguage scriptLanguage = nameToLanguage.get(languageName);
    if (scriptLanguage == null) {
      throw new SimpleSmartSpacesException(String.format(
          "Unable to find a script engine for language %s", languageName));
    }

    return newScript(scriptLanguage, scriptSource);
  }

  @Override
  public Script newScriptByExtension(String extension, ScriptSource scriptSource) {
    ScriptLanguage scriptLanguage = extensionToLanguage.get(extension);
    if (scriptLanguage == null) {
      throw new SimpleSmartSpacesException(String.format(
          "Unable to find a script engine for language extension %s", extension));
    }

    return newScript(scriptLanguage, scriptSource);
  }

  /**
   * Create the new script object.
   *
   * @param scriptLanguage
   *          the script language
   * @param scriptSource
   *          source of the script
   *
   * @return the script
   */
  private Script newScript(ScriptLanguage scriptLanguage, ScriptSource scriptSource) {
    ScriptEngine engine = scriptLanguage.getScriptEngineFactory().getScriptEngine();
    if (engine instanceof Compilable) {
      return new CompiledScriptScript((Compilable) engine, scriptSource);
    } else {
      return new ScriptEngineScript(engine, scriptSource);
    }
  }

  @Override
  public ActivityScriptWrapper getActivityByName(String languageName, String objectName,
      ScriptSource script, ActivityFilesystem activityFilesystem, Configuration configuration) {
    ScriptLanguage language = nameToLanguage.get(languageName);
    if (language == null) {
      throw new SimpleSmartSpacesException(String.format(
          "Unable to find a script engine for language %s", languageName));
    }

    return getActivity(languageName, objectName, script, activityFilesystem, configuration,
        language, "name");
  }

  @Override
  public ActivityScriptWrapper getActivityByExtension(String extension, String objectName,
      ScriptSource script, ActivityFilesystem activityFilesystem, Configuration configuration) {
    ScriptLanguage language = extensionToLanguage.get(extension);
    if (language == null) {
      throw new SmartSpacesException(String.format(
          "Unable to find a script engine for language %s", extension));
    }

    return getActivity(extension, objectName, script, activityFilesystem, configuration, language,
        "extension");
  }

  /**
   * Get the activity, if possible, from the {@link ActivityScriptFactory}.
   *
   * @param languageId
   *          ID of the language
   * @param objectName
   *          name of the object to be created
   * @param script
   *          the script
   * @param activityFilesystem
   *          the file system for the activity
   * @param configuration
   *          the configuration for the activity
   * @param language
   *          the language of the script
   * @param nameType
   *          how the language type is coming in
   *
   * @return the activity
   */
  private ActivityScriptWrapper getActivity(String languageId, String objectName,
      ScriptSource script, ActivityFilesystem activityFilesystem, Configuration configuration,
      ScriptLanguage language, String nameType) {
    ActivityScriptFactory factory = language.getActivityScriptFactory();
    if (factory == null) {
      throw new SimpleSmartSpacesException(String.format(
          "Unable to find an activity script factory for %s %s", nameType, languageId));
    }

    return factory.getActivity(objectName, script, activityFilesystem, configuration);
  }

  /**
   * Everything that can be done with a scripting language.
   *
   * @author Keith M. Hughes
   */
  private static class ScriptLanguage {

    /**
     * A script language factory, if any, for the language.
     *
     * <p>
     * Can be {@code null}.
     */
    private final ScriptEngineFactory scriptEngineFactory;

    /**
     * The factory for creating {@link Activity} instances.
     *
     * <p>
     * Can be {@code null}.
     */
    private final ActivityScriptFactory activityScriptFactory;

    /**
     * Construct a script language.
     *
     * @param scriptEngineFactory
     *          the script engine factory for the language
     * @param activityScriptFactory
     *          the activity script factory for the language
     */
    public ScriptLanguage(ScriptEngineFactory scriptEngineFactory,
        ActivityScriptFactory activityScriptFactory) {
      this.scriptEngineFactory = scriptEngineFactory;
      this.activityScriptFactory = activityScriptFactory;
    }

    /**
     * Get the script engine factory.
     *
     * @return the scriptEngineFactory, {@code null} if none
     */
    public ScriptEngineFactory getScriptEngineFactory() {
      return scriptEngineFactory;
    }

    /**
     * Get the activity script factory.
     *
     * @return the activityScriptFactory, {@code null} if none
     */
    public ActivityScriptFactory getActivityScriptFactory() {
      return activityScriptFactory;
    }
  }
}
