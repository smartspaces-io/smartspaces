/*
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

package interactivespaces.workbench.project;

import interactivespaces.InteractiveSpacesException;
import interactivespaces.SimpleInteractiveSpacesException;
import interactivespaces.configuration.Configuration;
import interactivespaces.resource.Version;
import interactivespaces.resource.VersionRange;
import interactivespaces.workbench.project.constituent.ProjectAssemblyConstituent;
import interactivespaces.workbench.project.constituent.ProjectBundleConstituent;
import interactivespaces.workbench.project.constituent.ProjectConstituent;
import interactivespaces.workbench.project.constituent.ProjectResourceConstituent;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Closeables;

import org.apache.commons.logging.Log;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import java.util.Map;

/**
 * A {@link ProjectReader} based on JDOM.
 *
 * @author Keith M. Hughes
 */
public class JdomProjectReader implements ProjectReader {

  /**
   * The default version range for projects which do not specify a version
   * range.
   */
  public static final VersionRange INTERACTIVESPACES_VERSION_RANGE_DEFAULT = new VersionRange(new Version(1, 0, 0),
      new Version(2, 0, 0), false);

  /**
   * Map of resource types to resource builders.
   */
  private static final Map<String, ProjectConstituent.ProjectConstituentFactory> PROJECT_CONSTITUENT_FACTORY_MAP =
      ImmutableMap.of(ProjectResourceConstituent.PROJECT_TYPE,
          new ProjectResourceConstituent.ProjectResourceBuilderFactory(),
          ProjectResourceConstituent.PROJECT_TYPE_ALTERNATE,
          new ProjectResourceConstituent.ProjectResourceBuilderFactory(), ProjectAssemblyConstituent.PROJECT_TYPE,
          new ProjectAssemblyConstituent.ProjectAssemblyConstituentFactory(), ProjectBundleConstituent.PROJECT_TYPE,
          new ProjectBundleConstituent.ProjectBundleConstituentFactory());

  /**
   * The name of the project.
   */
  private static final String PROJECT_ELEMENT_NAME_NAME = "name";

  /**
   * The description of the project.
   */
  private static final String PROJECT_ELEMENT_NAME_DESCRIPTION = "description";

  /**
   * The identifying name of the project.
   */
  private static final String PROJECT_ELEMENT_NAME_IDENTIFYING_NAME = "identifyingName";

  /**
   * The project attribute for the project type.
   */
  private static final String PROJECT_ATTRIBUTE_NAME_PROJECT_TYPE = "type";

  /**
   * The project attribute for the project builder.
   */
  private static final String PROJECT_ATTRIBUTE_NAME_PROJECT_BUILDER = "builder";

  /**
   * The project attribute for the version of Interactive Spaces which is
   * required.
   */
  private static final String PROJECT_ATTRIBUTE_NAME_PROJECT_INTERACTIVE_SPACES_VERSION = "interactiveSpacesVersion";

  /**
   * The version of the project.
   */
  private static final String PROJECT_ELEMENT_NAME_VERSION = "version";

  /**
   * Project definition file element name for resources.
   */
  private static final String PROJECT_ELEMENT_RESOURCES_NAME = "resources";

  /**
   * Project definition file element name for sources.
   */
  private static final String PROJECT_ELEMENT_NAME_SOURCES = "sources";

  /**
   * Project definition file element name for metadata.
   */
  private static final String PROJECT_ELEMENT_NAME_METADATA = "metadata";

  /**
   * Project definition file element name for metadata.
   */
  private static final String PROJECT_ELEMENT_NAME_METADATA_ITEM = "item";

  /**
   * Project definition file element name for metadata.
   */
  private static final String PROJECT_ATTRIBUTE_NAME_METADATA_ITEM_NAME = "name";

  /**
   * Project definition file element name for dependencies.
   */
  private static final String PROJECT_ELEMENT_NAME_DEPENDENCIES = "dependencies";

  /**
   * Project definition file element name for a dependency item.
   */
  private static final String PROJECT_ELEMENT_NAME_DEPENDENCY_ITEM = "dependency";

  /**
   * Project definition file attribute name for the name of a dependency.
   */
  private static final String PROJECT_ATTRIBUTE_NAME_DEPENDENCY_ITEM_NAME = "name";

  /**
   * Project definition file attribute name for the minimum version of a
   * dependency.
   */
  private static final String PROJECT_ATTRIBUTE_NAME_DEPENDENCY_ITEM_VERSION_MINIMUM = "minimumVersion";

  /**
   * Project definition file attribute name for the maximum version of a
   * dependency.
   */
  private static final String PROJECT_ATTRIBUTE_NAME_DEPENDENCY_ITEM_VERSION_MAXIMUM = "maximumVersion";

  /**
   * Project definition file attribute name for whether a dependency is
   * required.
   */
  private static final String PROJECT_ATTRIBUTE_NAME_DEPENDENCY_ITEM_REQUIRED = "required";

  /**
   * Project definition file element name for deployments.
   */
  private static final String PROJECT_ELEMENT_NAME_DEPLOYMENTS = "deployments";

  /**
   * Project definition file element name for a deployment item.
   */
  private static final String PROJECT_ELEMENT_NAME_DEPLOYMENT_ITEM = "deployment";

  /**
   * Project definition file attribute name for the type of a deployment.
   */
  private static final String PROJECT_ATTRIBUTE_NAME_DEPLOYMENT_ITEM_TYPE = "type";

  /**
   * Project definition file attribute name for the method of a deployment.
   */
  private static final String PROJECT_ATTRIBUTE_NAME_DEPLOYMENT_ITEM_METHOD = "method";

  /**
   * Project definition file attribute name for the location for a deployment.
   */
  private static final String PROJECT_ATTRIBUTE_NAME_DEPLOYMENT_ITEM_LOCATION = "location";

  /**
   * Project definition file element name for configurations.
   */
  private static final String PROJECT_ELEMENT_NAME_CONFIGURATION = "configuration";

  /**
   * Project definition file element name for a configuration item.
   */
  private static final String PROJECT_ELEMENT_NAME_CONFIGURATION_ITEM = "property";

  /**
   * Project definition file attribute name for the name of a configuration
   * item.
   */
  private static final String PROJECT_ATTRIBUTE_NAME_CONFIGURATION_ITEM_NAME = "name";

  /**
   * Log for errors.
   */
  private final Log log;

  /**
   * {@code true} if read was successful.
   */
  private boolean failure;

  /**
   * Construct a project reader.
   *
   * @param log
   *          the logger to use
   */
  public JdomProjectReader(Log log) {
    this.log = log;
  }

  @Override
  public Project readProject(File projectFile) {
    Document doc = null;
    FileInputStream inputStream = null;
    try {
      inputStream = new FileInputStream(projectFile);
      SAXBuilder builder = new SAXBuilder();
      doc = builder.build(inputStream);
    } catch (Exception e) {
      throw new InteractiveSpacesException(String.format("Exception while processing project file %s",
          projectFile.getAbsolutePath()), e);
    } finally {
      Closeables.closeQuietly(inputStream);
    }

    Element rootElement = doc.getRootElement();

    Project project = new Project();
    project.setType(getProjectType(rootElement));
    project.setBaseDirectory(projectFile.getParentFile());

    getProjectAttributes(project, rootElement);
    getMainData(project, rootElement);
    getMetadata(project, rootElement);
    getDependencies(project, rootElement);
    getConfiguration(project, rootElement);
    project.addResources(getConstituents(rootElement.getChild(PROJECT_ELEMENT_RESOURCES_NAME)));
    project.addSources(getConstituents(rootElement.getChild(PROJECT_ELEMENT_NAME_SOURCES)));
    getDeployments(project, rootElement);

    if (failure) {
      throw new SimpleInteractiveSpacesException(String.format("Project file %s had errors",
          projectFile.getAbsolutePath()));
    }

    return project;
  }

  /**
   * Get the project type from the root element.
   *
   * @param rootElement
   *          the root element of the XML doc
   *
   * @return the project type
   */
  private String getProjectType(Element rootElement) {
    return getRequiredAttributeValue(rootElement, PROJECT_ATTRIBUTE_NAME_PROJECT_TYPE);
  }

  /**
   * Get the main data from the document.
   *
   * @param project
   *          the project description whose data is being read
   * @param rootElement
   *          root element of the XML DOM containing the project data
   */
  private void getMainData(Project project, Element rootElement) {
    String name = getChildTextTrimmed(rootElement, PROJECT_ELEMENT_NAME_NAME);
    project.setName(name);

    String description = getChildTextTrimmed(rootElement, PROJECT_ELEMENT_NAME_DESCRIPTION);
    project.setDescription(description);

    String identifyingName = getChildTextTrimmed(rootElement, PROJECT_ELEMENT_NAME_IDENTIFYING_NAME);
    project.setIdentifyingName(identifyingName);

    String version = getChildTextTrimmed(rootElement, PROJECT_ELEMENT_NAME_VERSION);
    project.setVersion(Version.parseVersion(version));

    String builder = getAttributeValue(rootElement, PROJECT_ATTRIBUTE_NAME_PROJECT_BUILDER, null);
    project.setBuilderType(builder);

    String interactiveSpacesVersionRangeAttribute =
        getAttributeValue(rootElement, PROJECT_ATTRIBUTE_NAME_PROJECT_INTERACTIVE_SPACES_VERSION, null);

    VersionRange interactiveSpacesVersionRange = null;
    if (interactiveSpacesVersionRangeAttribute != null) {
      interactiveSpacesVersionRange = VersionRange.parseVersionRange(interactiveSpacesVersionRangeAttribute);
    } else {
      log.warn("Did not specify a range of needed Interactive Spaces versions. Setting default to "
          + INTERACTIVESPACES_VERSION_RANGE_DEFAULT);
      interactiveSpacesVersionRange = INTERACTIVESPACES_VERSION_RANGE_DEFAULT;
    }
    project.setInteractiveSpacesVersionRange(interactiveSpacesVersionRange);
  }

  /**
   * Get any attributes attached to the project.
   *
   * @param project
   *          the project description whose data is being read
   * @param rootElement
   *          root element of the XML DOM containing the project data
   */
  private void getProjectAttributes(Project project, Element rootElement) {
    @SuppressWarnings("unchecked")
    List<Attribute> attributes = rootElement.getAttributes();
    for (Attribute attribute : attributes) {
      project.addAttribute(attribute.getName(), attribute.getValue());
    }
  }

  /**
   * Return the trimmed text of a child element.
   *
   * @param element
   *          container element
   * @param key
   *          variable key
   *
   * @return trimmed element text
   *
   * @throws SimpleInteractiveSpacesException
   *           if the child element is not provided
   */
  private String getChildTextTrimmed(Element element, String key) throws SimpleInteractiveSpacesException {
    try {
      return element.getChildText(key).trim();
    } catch (Exception e) {
      throw new SimpleInteractiveSpacesException("Looking for text of child: " + key, e);
    }
  }

  /**
   * Return a required element attribute.
   *
   * @param element
   *          container element
   * @param key
   *          attribute key
   *
   * @return attribute value or {@code null} if none found
   */
  private String getRequiredAttributeValue(Element element, String key) {
    String value = getAttributeValue(element, key);
    if (value == null) {
      log.error("Missing required attribute " + key);
    }
    return value;
  }

  /**
   * Return a given element attribute, using the default value if not found.
   *
   * @param element
   *          container element
   * @param key
   *          attribute key
   * @param fallback
   *          default attribute value
   *
   * @return attribute value
   */
  private String getAttributeValue(Element element, String key, String fallback) {
    return element.getAttributeValue(key, fallback);
  }

  /**
   * Return a given element attribute.
   *
   * @param element
   *          container element
   * @param key
   *          attribute key
   *
   * @return attribute value, or {@code null} if not found.
   */
  private String getAttributeValue(Element element, String key) {
    return getAttributeValue(element, key, null);
  }

  /**
   * Get the metadata from the document.
   *
   * @param project
   *          the project description whose data is being read
   * @param rootElement
   *          root element of the XML DOM containing the project data
   */
  private void getMetadata(Project project, Element rootElement) {
    Element metadataElement = rootElement.getChild(PROJECT_ELEMENT_NAME_METADATA);

    if (metadataElement != null) {
      @SuppressWarnings("unchecked")
      List<Element> itemElements = metadataElement.getChildren(PROJECT_ELEMENT_NAME_METADATA_ITEM);

      Map<String, Object> metadata = Maps.newHashMap();
      for (Element itemElement : itemElements) {
        String name = getRequiredAttributeValue(itemElement, PROJECT_ATTRIBUTE_NAME_METADATA_ITEM_NAME);
        String value = itemElement.getTextNormalize();
        metadata.put(name, value);
      }

      project.setMetadata(metadata);
    }
  }

  /**
   * Get the configuration from the document.
   *
   * @param project
   *          the project description whose data is being read
   * @param rootElement
   *          root element of the XML DOM containing the project data
   */
  private void getConfiguration(Project project, Element rootElement) {
    Element configurationElement = rootElement.getChild(PROJECT_ELEMENT_NAME_CONFIGURATION);

    if (configurationElement != null) {
      @SuppressWarnings("unchecked")
      List<Element> propertyElements = configurationElement.getChildren(PROJECT_ELEMENT_NAME_CONFIGURATION_ITEM);

      Configuration configuration = project.getConfiguration();
      for (Element propertyElement : propertyElements) {
        String name = getRequiredAttributeValue(propertyElement, PROJECT_ATTRIBUTE_NAME_CONFIGURATION_ITEM_NAME);
        if (name == null) {
          continue;
        }
        String value = propertyElement.getTextNormalize();
        configuration.setValue(name, value);
      }
    }
  }

  /**
   * Get the resources from the document.
   *
   * @param project
   *          the project description whose data is being read
   * @param rootElement
   *          root element of the XML DOM containing the project data
   */
  private void getDependencies(Project project, Element rootElement) {
    Element dependenciesElement = rootElement.getChild(PROJECT_ELEMENT_NAME_DEPENDENCIES);

    if (dependenciesElement != null) {
      @SuppressWarnings("unchecked")
      List<Element> dependencyElements = dependenciesElement.getChildren(PROJECT_ELEMENT_NAME_DEPENDENCY_ITEM);

      for (Element dependencyElement : dependencyElements) {
        ProjectDependency dependency = getDependency(dependencyElement);
        project.addDependency(dependency);
      }
    }
  }

  /**
   * Get an project dependency from the dependency element.
   *
   * @param dependencyElement
   *          the element containing the data
   *
   * @return the dependency found in the element
   */
  private ProjectDependency getDependency(Element dependencyElement) {
    String name = getAttributeValue(dependencyElement, PROJECT_ATTRIBUTE_NAME_DEPENDENCY_ITEM_NAME);
    if (name == null) {
      addError("Dependency has no name");
    }

    String minimumVersion =
        getAttributeValue(dependencyElement, PROJECT_ATTRIBUTE_NAME_DEPENDENCY_ITEM_VERSION_MINIMUM);
    String maximumVersion =
        getAttributeValue(dependencyElement, PROJECT_ATTRIBUTE_NAME_DEPENDENCY_ITEM_VERSION_MAXIMUM);

    if (minimumVersion != null) {
      if (maximumVersion == null) {
        maximumVersion = minimumVersion;
      }
    } else if (maximumVersion != null) {
      // If here was no minimum version
      minimumVersion = maximumVersion;
    } else {
      addError("Dependency has no version constraints");
    }

    String requiredString =
        getAttributeValue(dependencyElement, PROJECT_ATTRIBUTE_NAME_DEPENDENCY_ITEM_REQUIRED, "true");

    ProjectDependency dependency = new ProjectDependency();

    dependency.setName(name);
    dependency.setMinimumVersion(Version.parseVersion(minimumVersion));
    dependency.setMaximumVersion(Version.parseVersion(maximumVersion));
    dependency.setRequired("true".equals(requiredString));

    return dependency;
  }

  /**
   * Add the constituents from the document.
   *
   * @param containerElement
   *          root element of the XML DOM containing the project data
   *
   * @return the constituents for the project
   */
  private List<ProjectConstituent> getConstituents(Element containerElement) {
    if (containerElement == null) {
      return null;
    }

    List<ProjectConstituent> constituents = Lists.newArrayList();
    @SuppressWarnings("unchecked")
    List<Element> childElements = containerElement.getChildren();

    for (Element childElement : childElements) {
      String type = childElement.getName();
      ProjectConstituent.ProjectConstituentFactory factory = PROJECT_CONSTITUENT_FACTORY_MAP.get(type);
      if (factory == null) {
        addError(String.format("Unknown resource type '%s'", type));
      } else {
        ProjectConstituent constituent = factory.newBuilder(log).buildConstituentFromElement(childElement);
        if (constituent != null) {
          constituents.add(constituent);
        }
      }
    }

    return constituents;
  }

  /**
   * Get the deployments from the document.
   *
   * @param project
   *          the project description whose data is being read
   * @param rootElement
   *          root element of the XML DOM containing the project data
   */
  private void getDeployments(Project project, Element rootElement) {
    Element deploymentsElement = rootElement.getChild(PROJECT_ELEMENT_NAME_DEPLOYMENTS);

    if (deploymentsElement != null) {
      @SuppressWarnings("unchecked")
      List<Element> deploymentElements = deploymentsElement.getChildren(PROJECT_ELEMENT_NAME_DEPLOYMENT_ITEM);

      for (Element deploymentElement : deploymentElements) {
        ProjectDeployment deployment = getDeployment(deploymentElement);
        if (deployment != null) {
          project.addDeployment(deployment);
        }
      }
    }
  }

  /**
   * Get an project deployment from the deployment element.
   *
   * @param deploymentElement
   *          the element containing the data
   *
   * @return the deployment found in the element
   */
  private ProjectDeployment getDeployment(Element deploymentElement) {
    String type = getRequiredAttributeValue(deploymentElement, PROJECT_ATTRIBUTE_NAME_DEPLOYMENT_ITEM_TYPE);
    String method = getAttributeValue(deploymentElement, PROJECT_ATTRIBUTE_NAME_DEPLOYMENT_ITEM_METHOD);
    String location = getAttributeValue(deploymentElement, PROJECT_ATTRIBUTE_NAME_DEPLOYMENT_ITEM_LOCATION);

    // TODO(keith): Enumerate all possible errors

    return new ProjectDeployment(type, method, location);
  }

  /**
   * An error has occurred.
   *
   * @param error
   *          text of the error message
   */
  private void addError(String error) {
    log.error(error);
    failure = true;
  }
}
