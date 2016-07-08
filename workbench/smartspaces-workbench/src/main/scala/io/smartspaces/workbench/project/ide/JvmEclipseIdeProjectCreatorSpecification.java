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

package io.smartspaces.workbench.project.ide;

import io.smartspaces.util.io.FileSupport;
import io.smartspaces.util.io.FileSupportImpl;
import io.smartspaces.workbench.FreemarkerTemplater;
import io.smartspaces.workbench.language.java.JavaProgrammingLanguageSupport;
import io.smartspaces.workbench.language.scala.ScalaProgrammingLanguageSupport;
import io.smartspaces.workbench.project.Project;
import io.smartspaces.workbench.project.ProjectDependency;
import io.smartspaces.workbench.project.ProjectTaskContext;
import io.smartspaces.workbench.project.java.JvmProjectExtension;
import io.smartspaces.workbench.project.java.JvmProjectSupport;
import io.smartspaces.workbench.project.java.StandardJvmProjectSupport;

import com.google.common.collect.Lists;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Specification for projects that run in a JVM with a classpath.
 *
 * @author Keith M. Hughes
 */
public class JvmEclipseIdeProjectCreatorSpecification
    implements EclipseIdeProjectCreatorSpecification {

  /**
   * Freemarker context variable for libraries which are part of the project.
   */
  public static final String FREEMARKER_CONTEXT_LIBS = "libs";

  /**
   * Freemarker context variable for sources which are part of the project.
   */
  public static final String FREEMARKER_CONTEXT_SOURCES = "srcs";

  /**
   * Freemarker context variable for launcher containers which are part of the project.
   */
  public static final String FREEMARKER_CONTEXT_LAUNCHERS = "launchers";

  /**
   * Freemarker context variable for dynamic projects which are part of the
   * project.
   */
  public static final String FREEMARKER_CONTEXT_DYNAMIC_PROJECTS = "dynamicProjects";

  /**
   * The location of the eclipse classpath template file.
   */
  public static final String TEMPLATE_FILEPATH_ECLIPSE_CLASSPATH = "ide/eclipse/jvm-classpath.ftl";

  /**
   * The name of the Eclipse classpath file.
   */
  public static final String FILENAME_CLASSPATH_FILE = ".classpath";

  /**
   * The value for the Java builder in Eclipse.
   */
  public static final String ECLIPSE_BUILDER_JAVA = "org.eclipse.jdt.core.javabuilder";

  /**
   * The value for the Java nature for an Eclipse project.
   */
  public static final String ECLIPSE_NATURE_JAVA = "org.eclipse.jdt.core.javanature";

  /**
   * The value for the Scala builder in Eclipse.
   */
  public static final String ECLIPSE_BUILDER_SCALA = "org.scala-ide.sdt.core.scalabuilder";

  /**
   * The value for the Scalaa nature for an Eclipse project.
   */
  public static final String ECLIPSE_NATURE_SCALA = "org.scala-ide.sdt.core.scalanature";

  /**
   * The launcher container for Java projects.
   */
  public static final String ECLIPSE_LAUNCHER_CONTAINER_JAVA ="org.eclipse.jdt.launching.JRE_CONTAINER";

  /**
   * The launcher container for Java projects.
   */
  public static final String ECLIPSE_LAUNCHER_CONTAINER_SCALA ="org.scala-ide.sdt.launching.SCALA_CONTAINER";

  /**
   * List of required sources for the project.
   */
  private final List<String> sourcesRequired;

  /**
   * List of optional sources for the project.
   */
  private final List<String> sourcesOptional;

  /**
   * The Java activity extensions.
   *
   * <p>
   * Can be {@code null}.
   */
  private final JvmProjectExtension extensions;

  /**
   * The project support for this item.
   */
  private JvmProjectSupport projectSupport = new StandardJvmProjectSupport();

  /**
   * The file support to use.
   */
  private final FileSupport fileSupport = FileSupportImpl.INSTANCE;

  /**
   * Construct a specification with {@code null} extensions.
   *
   * @param sourcesRequired
   *          the required source directories
   * @param sourcesOptional
   *          optional resources for the project
   */
  public JvmEclipseIdeProjectCreatorSpecification(List<String> sourcesRequired,
      List<String> sourcesOptional) {
    this(sourcesRequired, sourcesOptional, null);
  }

  /**
   * Construct a specification with extensions.
   *
   * @param sourcesRequired
   *          list of source directories for the project
   * @param sourcesOptional
   *          optional resources for the project
   * @param extensions
   *          the extensions to use, can be {@code null}
   */
  public JvmEclipseIdeProjectCreatorSpecification(List<String> sourcesRequired,
      List<String> sourcesOptional, JvmProjectExtension extensions) {
    this.sourcesRequired = sourcesRequired;
    this.sourcesOptional = sourcesOptional;
    this.extensions = extensions;
  }

  @Override
  public void addSpecificationData(Project project, ProjectTaskContext context,
      Map<String, Object> freemarkerContext) {
    String language = context.getProject().getLanguage();
    String builderField = null;
    List<String> naturesField = new ArrayList<>();
    if (JavaProgrammingLanguageSupport.LANGUAGE_NAME.equals(language)) {
      builderField = ECLIPSE_BUILDER_JAVA;
      
      naturesField.add(ECLIPSE_NATURE_JAVA);
    } else if (ScalaProgrammingLanguageSupport.LANGUAGE_NAME().equals(language)) {
      builderField = ECLIPSE_BUILDER_SCALA;
      
      naturesField.add(ECLIPSE_NATURE_SCALA);
      naturesField.add(ECLIPSE_NATURE_JAVA);
    }

    freemarkerContext.put(ECLIPSE_PROJECT_FIELD_NATURES, naturesField);
    freemarkerContext.put(ECLIPSE_PROJECT_FIELD_BUILDER, builderField);
  }

  @Override
  public void writeAdditionalFiles(Project project, ProjectTaskContext context,
      Map<String, Object> freemarkerContext, FreemarkerTemplater templater) throws Exception {
    List<Project> dynamicProjects = new ArrayList<>();
    for (ProjectDependency dependency : project.getDependencies()) {
      if (dependency.isDynamic()) {
        Project dependencyProject =
            context.getWorkbenchTaskContext().getDynamicProjectFromProjectPath(dependency);
        if (dependencyProject != null) {
          dynamicProjects.add(dependencyProject);
        }
      }
    }

    List<File> projectLibs = new ArrayList<>();
    projectSupport.getProjectClasspath(false, context, projectLibs, extensions,
        context.getWorkbenchTaskContext());

    List<String> sources = Lists.newArrayList(sourcesRequired);
    addNecessaryOptionalSources(project, sources);
    
    String language = context.getProject().getLanguage();
    List<String> launchersField = new ArrayList<>();
    if (JavaProgrammingLanguageSupport.LANGUAGE_NAME.equals(language)) {
      launchersField.add(ECLIPSE_LAUNCHER_CONTAINER_JAVA);
    } else if (ScalaProgrammingLanguageSupport.LANGUAGE_NAME().equals(language)) {
      launchersField.add(ECLIPSE_LAUNCHER_CONTAINER_JAVA);
      launchersField.add(ECLIPSE_LAUNCHER_CONTAINER_SCALA);
    }

    
    freemarkerContext.put(FREEMARKER_CONTEXT_SOURCES, sources);
    freemarkerContext.put(FREEMARKER_CONTEXT_LIBS, projectLibs);
    freemarkerContext.put(FREEMARKER_CONTEXT_DYNAMIC_PROJECTS, dynamicProjects);
    freemarkerContext.put(FREEMARKER_CONTEXT_LAUNCHERS, launchersField);

    templater.writeTemplate(freemarkerContext,
        fileSupport.newFile(project.getBaseDirectory(), FILENAME_CLASSPATH_FILE),
        TEMPLATE_FILEPATH_ECLIPSE_CLASSPATH);
  }

  /**
   * Add any needed optional sources.
   *
   * <p>
   * These are added by checking to see if the source folder exists.
   *
   * @param project
   *          the project being checked
   * @param sources
   *          the sources list which will be added
   */
  private void addNecessaryOptionalSources(Project project, List<String> sources) {
    for (String sourceOptional : sourcesOptional) {
      File location = fileSupport.newFile(project.getBaseDirectory(), sourceOptional);
      if (location.exists()) {
        sources.add(sourceOptional);
      }
    }
  }
}
