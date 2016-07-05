/*
 * Copyright (C) 2016 Keith M. Hughes
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

package io.smartspaces.workbench.project;

/**
 * The default file layout for projects.
 * 
 * @author Keith M. Hughes
 */
public class ProjectFileLayout {

  /**
   * Source location prefix for the language source files. The language must be
   * applied to the end.
   */
  public static final String SOURCE_MAIN_PREFIX = "src/main";

  /**
   * Source location prefix for the language test files. The language must be
   * applied to the end.
   */
  public static final String SOURCE_TEST_PREFIX = "src/test";

  /**
   * Root location for generated source.
   */
  public static final String GENERATED_SOURCE_ROOT = "generated-src";

  /**
   * Source location prefix for the generated source files. The language must be
   * applied to the end.
   */
  public static final String SOURCE_GENERATED_MAIN_PREFIX = GENERATED_SOURCE_ROOT + "/main";

  /**
   * Source location prefix for generated tests. The language must be applied to
   * the end.
   */
  public static final String SOURCE_GENERATED_TEST_PREFIX = GENERATED_SOURCE_ROOT + "/test";

  /**
   * Source location for the resource source files.
   */
  public static final String SOURCE_MAIN_RESOURCES = "src/main/resources";

  /**
   * Source location for test resources.
   */
  public static final String SOURCE_TEST_RESOURCES = "src/test/resources";

  /**
   * Source location for generated resource source files.
   */
  public static final String SOURCE_GENERATED_MAIN_RESOURCES =
      GENERATED_SOURCE_ROOT + "/main/resources";

  /**
   * Folder where the main classes will be built.
   */
  public static final String BUILD_DIRECTORY_CLASSES_MAIN = "classes/main";

  /**
   * Folder where the main classes will be built.
   */
      public static final String BUILD_DIRECTORY_CLASSES_TESTS = "classes/test";

}
