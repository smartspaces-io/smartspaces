/*
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

package io.smartspaces.workbench.project.test;

import io.smartspaces.SimpleSmartSpacesException;
import io.smartspaces.SmartSpacesException;
import io.smartspaces.util.io.FileSupport;
import io.smartspaces.util.io.FileSupportImpl;
import io.smartspaces.workbench.project.Project;
import io.smartspaces.workbench.project.ProjectTaskContext;
import io.smartspaces.workbench.project.java.EclipseProgrammingLanguageCompiler;
import io.smartspaces.workbench.project.java.JvmProjectExtension;
import io.smartspaces.workbench.project.java.JvmProjectType;
import io.smartspaces.workbench.project.java.ProgrammingLanguageCompiler;

import org.apache.commons.logging.Log;

import java.io.File;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

/**
 * A test runner which finds a bunch of classes which are JUnit test classes and
 * runs them.
 *
 * <p>
 * Tests are run in a test runner which is isolated from the classloader which
 * loads the Smart Spaces workbench. This is to prevent mixing jar files which
 * are found in both the Smart Spaces Workbench and controller.
 *
 * @author Keith M. Hughes
 */
public class IsolatedClassloaderJavaTestRunner implements JavaTestRunner {

  /**
   * The classname for the isolated test runner.
   */
  public static final String ISOLATED_TESTRUNNER_CLASSNAME =
      "io.smartspaces.workbench.project.test.IsolatedJavaTestRunner";

  /**
   * The method name for running tests on the isolated test runner.
   */
  public static final String ISOLATED_TESTRUNNER_METHODNAME = "runTests";

  /**
   * The project compiler.
   */
  private final ProgrammingLanguageCompiler projectCompiler = new EclipseProgrammingLanguageCompiler();

  /**
   * File support for class.
   */
  private final FileSupport fileSupport = FileSupportImpl.INSTANCE;

  /**
   * Build a class path structure based on the given jar destination file and
   * project.
   *
   * @param context
   *          the project task context
   * @param extension
   *          the java project extension
   * @param additionalFiles
   *          any number of additional Files to add to the class path
   * @return a List<File> of all required class path entries
   */
  private List<File> getClasspath(ProjectTaskContext context, JvmProjectExtension extension,
      File... additionalFiles) {
    List<File> classpath = new ArrayList<>();
    for (File additionalFile : additionalFiles) {
      classpath.add(additionalFile);
    }
    classpath.add(fileSupport.newFile(context.getProject().getBaseDirectory(),
        JvmProjectType.SOURCE_MAIN_TEST_RESOURCES));

    JvmProjectType projectType = context.getProjectType();
    projectType.getProjectClasspath(true, context, classpath, extension,
        context.getWorkbenchTaskContext());

    return classpath;
  }

  @Override
  public void runTests(File jarDestinationFile, JvmProjectExtension extension,
      ProjectTaskContext context) throws SmartSpacesException {
    List<File> compilationFiles = new ArrayList<>();

    Project project = context.getProject();
    projectCompiler.getCompilationFiles(
        fileSupport.newFile(project.getBaseDirectory(), JvmProjectType.SOURCE_MAIN_TESTS),
        compilationFiles);
    projectCompiler.getCompilationFiles(fileSupport.newFile(context.getBuildDirectory(),
        JvmProjectType.SOURCE_GENERATED_MAIN_TESTS), compilationFiles);

    if (compilationFiles.isEmpty()) {
      // No tests mean they all succeeded in some weird philosophical sense.
      return;
    }

    context.getLog().info(String.format("Running Java tests for project %s",
        context.getProject().getBaseDirectory().getAbsolutePath()));

    List<File> classpath = getClasspath(context, extension, jarDestinationFile);

    File compilationFolder = fileSupport.newFile(context.getBuildDirectory(),
        ProgrammingLanguageCompiler.BUILD_DIRECTORY_CLASSES_TESTS);
    fileSupport.directoryExists(compilationFolder);

    List<String> compilerOptions = projectCompiler.getCompilerOptions(context);

    projectCompiler.compile(context, compilationFolder, classpath, compilationFiles,
        compilerOptions);

    runJavaUnitTests(compilationFolder, jarDestinationFile, extension, context);
  }

  /**
   * Detect and run any JUnit test classes.
   *
   * @param testCompilationFolder
   *          folder where the test classes were compiled
   * @param jarDestinationFile
   *          the jar that was built
   * @param extension
   *          the Java project extension for the project (can be {@code null})
   * @param context
   *          the build context
   *
   * @throws SmartSpacesException
   *           the tests failed
   */
  private void runJavaUnitTests(File testCompilationFolder, File jarDestinationFile,
      JvmProjectExtension extension, ProjectTaskContext context) throws SmartSpacesException {
    List<File> classpath =
        getClasspath(context, extension, jarDestinationFile, testCompilationFolder);

    List<URL> urls = new ArrayList<>();
    for (File classpathElement : classpath) {
      try {
        urls.add(classpathElement.toURL());
      } catch (MalformedURLException e) {
        context.getWorkbenchTaskContext().getWorkbench().getLog()
            .error(String.format("Error while adding %s to the unit test classpath",
                classpathElement.getAbsolutePath()), e);
      }
    }

    URLClassLoader classLoader = new URLClassLoader(urls.toArray(new URL[urls.size()]),
        context.getWorkbenchTaskContext().getWorkbench().getBaseClassLoader());

    runTestsInIsolation(testCompilationFolder, classLoader, context);
  }

  /**
   * Run the given tests in the given class loader. This method is somewhat
   * complicated, since it needs to use reflection to isolate the test runner in
   * a separate class loader that does not derive from the current class.
   *
   * @param testCompilationFolder
   *          the folder containing the test classes
   * @param classLoader
   *          classLoader to use for running tests
   * @param context
   *          the build context
   *
   * @throws SmartSpacesException
   *           the tests failed
   */
  private void runTestsInIsolation(File testCompilationFolder, URLClassLoader classLoader,
      ProjectTaskContext context) throws SmartSpacesException {
    boolean result = false;
    try {
      // This code is equivalent to TestRunnerBridge.runTests(testClassNames,
      // classLoader), except
      // that it is sanitized through the test class loader.
      Class<?> testRunnerClass = classLoader.loadClass(ISOLATED_TESTRUNNER_CLASSNAME);
      Method runner = testRunnerClass.getMethod(ISOLATED_TESTRUNNER_METHODNAME, File.class,
          URLClassLoader.class, Log.class);

      Object testRunner = testRunnerClass.newInstance();

      result = (Boolean) runner.invoke(testRunner, testCompilationFolder, classLoader,
          context.getWorkbenchTaskContext().getWorkbench().getLog());
    } catch (Exception e) {
      // This catch here for the reflection errors
      context.getWorkbenchTaskContext().getWorkbench().getLog().error("Error running tests", e);
      throw new SmartSpacesException("Error running tests", e);
    }

    if (!result) {
      throw new SimpleSmartSpacesException("Unit tests failed");
    }
  }
}
