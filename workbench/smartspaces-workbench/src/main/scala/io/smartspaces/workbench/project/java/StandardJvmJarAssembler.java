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

package io.smartspaces.workbench.project.java;

import io.smartspaces.SimpleSmartSpacesException;
import io.smartspaces.SmartSpacesException;
import io.smartspaces.resource.Version;
import io.smartspaces.util.io.FileSupport;
import io.smartspaces.util.io.FileSupportImpl;
import io.smartspaces.workbench.language.ProgrammingLanguageCompiler;
import io.smartspaces.workbench.language.ProgrammingLanguageSupport;
import io.smartspaces.workbench.project.Project;
import io.smartspaces.workbench.project.ProjectDependency;
import io.smartspaces.workbench.project.ProjectDependency.ProjectDependencyLinking;
import io.smartspaces.workbench.project.ProjectDependencyProvider;
import io.smartspaces.workbench.project.ProjectTaskContext;
import io.smartspaces.workbench.project.constituent.ContentProjectConstituent;
import io.smartspaces.workbench.project.java.ContainerInfo.ImportPackage;

import aQute.lib.osgi.Analyzer;
import aQute.lib.osgi.Constants;
import aQute.lib.osgi.Jar;
import com.google.common.base.Joiner;
import com.google.common.io.Closeables;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipOutputStream;

/**
 * A {@link JvmJarAssembler} using the system Java compiler.
 *
 * @author Keith M. Hughes
 */
public class StandardJvmJarAssembler implements JvmJarAssembler {

  /**
   * The name of the manifest file in a Java jar.
   */
  public static final String JAVA_JAR_MANIFEST_MF = "MANIFEST.MF";

  /**
   * A file filter that accepts anything but a Java jar manifest file.
   */
  public static final FileFilter JAVA_JAR_MANIFEST_IGNORE_FILTER = new FileFilter() {
    @Override
    public boolean accept(File pathname) {
      return !pathname.getName().endsWith(JAVA_JAR_MANIFEST_MF);
    }
  };

  /**
   * The name given to the temporary folder that helps with static linking.
   */
  public static final String TEMP_FOLDER_STATIC_LINKING = "staticlinking";

  /**
   * The separator between a bundle's name and version in the bundle filename.
   */
  public static final String BUNDLE_NAME_VERSION_SEPARATOR = "-";

  /**
   * Size of IO buffers in bytes.
   */
  public static final int IO_BUFFER_SIZE = 1024;

  /**
   * The file support to use.
   */
  private final FileSupport fileSupport = FileSupportImpl.INSTANCE;

  /**
   * The project support for this item.
   */
  private JvmProjectSupport projectSupport = new StandardJvmProjectSupport();

  @Override
  public void buildJar(File jarDestinationFile, File compilationBuildFolder,
      JvmProjectExtension extensions, ContainerInfo containerInfo, ProjectTaskContext context,
      ProgrammingLanguageSupport languageSupport) {

    List<File> classpath = new ArrayList<>();
    projectSupport.getRuntimeClasspath(true, context, classpath, extensions,
        context.getWorkbenchTaskContext());

    Project project = context.getProject();
    File mainSourceDirectory =
        fileSupport.newFile(project.getBaseDirectory(), languageSupport.getMainSourceDirectory());
    File generatedSourceDirectory = fileSupport.newFile(context.getBuildDirectory(),
        languageSupport.getMainGeneratedSourceDirectory());

    context.addSourceDirectories(mainSourceDirectory, generatedSourceDirectory);

    for (ContentProjectConstituent constituent : project.getSources()) {
      String sourceDirectory = constituent.getSourceDirectory();
      if (sourceDirectory == null) {
        File buildTempDirectory = context.getTempBuildDirectory();
        constituent.processConstituent(project, buildTempDirectory, context);
        sourceDirectory = buildTempDirectory.toString();
      }
      File addedSource = context.getProjectTargetFile(project.getBaseDirectory(), sourceDirectory);

      context.addSourceDirectories(addedSource);
    }

    List<File> compilationFiles = getCompilationFiles(context, languageSupport);

    if (compilationFiles.isEmpty()) {
      throw new SimpleSmartSpacesException("No Scala or Java source files for Scala project");
    }

    ProgrammingLanguageCompiler compiler = languageSupport.newCompiler();
    compiler.compile(context, compilationBuildFolder, classpath, compilationFiles,
        languageSupport.getCompilerOptions(context));

    addStaticLinkDependencies(compilationBuildFolder, context);

    createJarFile(project, jarDestinationFile, compilationBuildFolder, classpath, containerInfo,
        context);

    if (extensions != null) {
      extensions.postProcessJar(context, jarDestinationFile);
    }

    context.addArtifactToInclude(jarDestinationFile);
  }

  /**
   * Get the compilation files for the given compiler.
   * 
   * @param context
   *          the project context
   * @param languageSupport
   *          the language support
   */
  private List<File> getCompilationFiles(ProjectTaskContext context,
      ProgrammingLanguageSupport languageSupport) {
    List<File> compilationFiles = new ArrayList<>();
    int currentCount = 0;
    for (File sourceDirectory : context.getSourceDirectories()) {
      languageSupport.getCompilationFiles(sourceDirectory, compilationFiles);

      int newCount = compilationFiles.size();
      context.getLog().info(String.format("Found %d files for source directory %s",
          newCount - currentCount, sourceDirectory.getAbsolutePath()));
      currentCount = newCount;
    }
    return compilationFiles;
  }

  /**
   * Link in any dependencies which are static into the jar.
   *
   * @param compilationFolder
   *          the folder where the activity class files are
   * @param context
   *          the context for the project task
   */
  private void addStaticLinkDependencies(File compilationFolder, ProjectTaskContext context) {
    // This folder is created lazily.
    File expansionFolder = null;

    try {
      for (ProjectDependency dependency : context.getProject().getDependencies()) {
        if (ProjectDependencyLinking.STATIC != dependency.getLinking()) {
          continue;
        }

        ProjectDependencyProvider provider = dependency.getProvider();
        if (provider == null) {
          continue;
        }

        if (expansionFolder == null) {
          expansionFolder =
              fileSupport.newFile(context.getBuildDirectory(), TEMP_FOLDER_STATIC_LINKING);
          if (!fileSupport.mkdirs(expansionFolder)) {
            // Folder already exists. Clean it out.
            fileSupport.deleteDirectoryContents(expansionFolder);
          }
        }
        provider.placeContents(expansionFolder);
      }

      if (expansionFolder != null) {
        fileSupport.copyDirectory(expansionFolder, JAVA_JAR_MANIFEST_IGNORE_FILTER,
            compilationFolder, true, null);
      }
    } finally {
      if (expansionFolder != null) {
        fileSupport.delete(expansionFolder);
      }
    }
  }

  /**
   * Create the JAR file for the artifact.
   *
   * @param project
   *          the project being built
   * @param jarDestinationFile
   *          the file where the jar file is going
   * @param compilationFolder
   *          folder that the Java class files were compiled into
   * @param classpath
   *          class-path to use for jar manifest file
   * @param containerInfo
   *          the container info for the build
   * @param context
   *          the build context
   */
  private void createJarFile(Project project, File jarDestinationFile, File compilationFolder,
      List<File> classpath, ContainerInfo containerInfo, ProjectTaskContext context) {
    // Create a buffer for reading the files
    byte[] buf = new byte[IO_BUFFER_SIZE];

    Manifest manifest =
        createManifest(project, compilationFolder, classpath, containerInfo, context);
    JarOutputStream out = null;
    try {
      // Create the ZIP file
      out = new JarOutputStream(new FileOutputStream(jarDestinationFile), manifest);

      writeJarFile(compilationFolder, buf, out, "", context);
    } catch (IOException e) {
      throw SmartSpacesException.newFormattedException(e, "Failed creating jar file %s",
          jarDestinationFile.getAbsolutePath());
    } finally {
      fileSupport.close(out, true);
    }
  }

  /**
   * Create a manifest for the object.
   *
   * @param project
   *          the project being created
   * @param compilationFolder
   *          folder where the Java files were compiled to
   * @param classpath
   *          class-path to use for jar manifest file
   * @param containerInfo
   *          any container info for the build
   * @param context
   *          the project build context
   *
   * @return the manifest
   */
  private Manifest createManifest(Project project, File compilationFolder, List<File> classpath,
      ContainerInfo containerInfo, ProjectTaskContext context) {
    // Map Java package names which are supplied by project dependencies to
    // those dependencies.
    Map<String, ProjectDependency> dependencyInfo = new HashMap<>();
    analyzeClasspathForDependencies(classpath, project, dependencyInfo, context);

    Analyzer analyzer = new Analyzer();
    try {
      Jar bin = new Jar(compilationFolder);
      analyzer.setJar(bin);

      analyzer.setClasspath(classpath.toArray(new File[classpath.size()]));

      analyzer.setProperty(Constants.BUNDLE_SYMBOLICNAME, project.getIdentifyingName());

      Version version = project.getVersion();
      analyzer.setProperty(Constants.BUNDLE_VERSION, version.toString());

      String activatorClassname = containerInfo.getActivatorClassname();
      if (activatorClassname != null && !activatorClassname.trim().isEmpty()) {
        analyzer.setProperty(Constants.BUNDLE_ACTIVATOR, activatorClassname);
      }

      List<String> exportPackages = new ArrayList<>();

      List<String> privatePackages = containerInfo.getPrivatePackages();
      if (!privatePackages.isEmpty()) {
        // All private packages should be added as not being in the public
        // packages. For some reason BND wants the NOT for private packages
        // added to the export.
        for (String privatePackage : privatePackages) {
          exportPackages.add("!" + privatePackage);
        }
        analyzer.setProperty(Constants.PRIVATE_PACKAGE, Joiner.on(",").join(privatePackages));
      }

      // At this point exportPackages will have entries to avoid exporting
      // private packages. This addition will make sure all packages from the
      // activity will export and will all be given the bundle version.
      String versionSpecifier = ";version=\"" + version + "\"";
      List<String> containerInfoExportPackages = containerInfo.getExportPackages();
      if (!containerInfoExportPackages.isEmpty()) {
        for (String containerInfoExportPackage : containerInfoExportPackages) {
          exportPackages.add(containerInfoExportPackage + versionSpecifier);
        }
      } else {
        // No exports explicitly specified, so export all.
        exportPackages.add("*" + versionSpecifier);
      }
      analyzer.setProperty(Constants.EXPORT_PACKAGE, Joiner.on(",").join(exportPackages));

      // This will make sure any imports we miss will be added to the imports.
      List<String> importPackages = new ArrayList<>();
      for (ImportPackage importPackage : containerInfo.getImportPackages()) {
        importPackages.add(importPackage.getOsgiHeader());
      }
      importPackages.add("*");
      analyzer.setProperty(Constants.IMPORT_PACKAGE, Joiner.on(",").join(importPackages));

      analyzer.analyze();

      correctDependencyVersions(dependencyInfo, analyzer);

      Manifest manifest = analyzer.calcManifest();

      return manifest;
    } catch (Exception e) {
      throw new SmartSpacesException("Could not create JAR manifest for project", e);
    } finally {
      fileSupport.close(analyzer, false);
    }
  }

  /**
   * Make sure that all project dependencies are given the version range stated
   * in the project dependencies.
   *
   * @param dependencyInfo
   *          the collection of packages to the project dependency
   * @param analyzer
   *          the analyzer for the bundle being created
   */
  private void correctDependencyVersions(Map<String, ProjectDependency> dependencyInfo,
      Analyzer analyzer) {
    Map<String, Map<String, String>> importsInfo = analyzer.getImports();
    for (Entry<String, Map<String, String>> importInfo : importsInfo.entrySet()) {
      String importedPackageName = importInfo.getKey();
      ProjectDependency projectDependency = dependencyInfo.get(importedPackageName);
      if (projectDependency != null) {
        String osgiVersionString = projectDependency.getOsgiVersionString();
        importInfo.getValue().put(Constants.VERSION_ATTRIBUTE, osgiVersionString);
      }
    }
  }

  /**
   * Analyze the classpath and get all bundle information that will affect
   * project dependencies. The {@code dependencyInfo} map will be keyed by Java
   * package names coming from project dependencies and will have a value of the
   * project dependency itself.
   *
   * @param classpath
   *          the classpath to be analyzed
   * @param project
   *          the project being built
   * @param dependencyInfo
   *          the dependency map
   * @param context
   *          the build context
   */
  private void analyzeClasspathForDependencies(List<File> classpath, Project project,
      Map<String, ProjectDependency> dependencyInfo, ProjectTaskContext context) {
    ContainerBundleAnalyzer bundleAnalyzer = new BndOsgiContainerBundleAnalyzer();

    Map<String, ProjectDependency> dependencies = new HashMap<>();
    for (ProjectDependency dependency : project.getDependencies()) {
      dependencies.put(dependency.getIdentifyingName(), dependency);

    }
    for (File bundle : classpath) {
      String initialName = bundle.getName();
      int posInitialname = initialName.lastIndexOf(BUNDLE_NAME_VERSION_SEPARATOR);
      if (posInitialname != -1) {
        initialName = initialName.substring(0, posInitialname);
      }
      ProjectDependency associatedDependency = dependencies.get(initialName);
      if (associatedDependency != null) {
        Set<String> packageExports = bundleAnalyzer.analyze(bundle).getPackageExports();
        if (packageExports != null) {
          for (String packageExport : packageExports) {
            if (dependencyInfo.put(packageExport, associatedDependency) != null) {
              context.getLog().warn(String
                  .format("Package export %s found in multiple classpath entities", packageExport));
            }
          }
        }
      }
    }
  }

  /**
   * Write out the contents of the folder to the distribution file.
   *
   * @param directory
   *          folder being written to the build
   * @param buf
   *          a buffer for caching info
   * @param jarOutputStream
   *          the stream where the jar is being written
   * @param parentPath
   *          path up to this point
   * @param context
   *          the project build context
   *
   * @throws IOException
   *           for io access errors
   */
  private void writeJarFile(File directory, byte[] buf, ZipOutputStream jarOutputStream,
      String parentPath, ProjectTaskContext context) throws IOException {
    File[] files = directory.listFiles();
    if (files == null || files.length == 0) {
      context.getLog().warn("No source files found in " + directory.getAbsolutePath());
      return;
    }
    for (File file : files) {
      if (file.isDirectory()) {
        writeJarFile(file, buf, jarOutputStream, parentPath + file.getName() + "/", context);
      } else {
        FileInputStream in = null;
        try {
          in = new FileInputStream(file);

          // Add ZIP entry to output stream.
          jarOutputStream.putNextEntry(new JarEntry(parentPath + file.getName()));

          // Transfer bytes from the file to the ZIP file
          int len;
          while ((len = in.read(buf)) > 0) {
            jarOutputStream.write(buf, 0, len);
          }

          // Complete the entry
          jarOutputStream.closeEntry();
        } finally {
          Closeables.close(in, true);
        }
      }
    }
  }
}
