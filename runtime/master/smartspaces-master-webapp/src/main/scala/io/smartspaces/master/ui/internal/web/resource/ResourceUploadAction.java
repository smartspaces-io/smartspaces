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

package io.smartspaces.master.ui.internal.web.resource;

import io.smartspaces.SimpleSmartSpacesException;
import io.smartspaces.SmartSpacesException;
import io.smartspaces.master.api.master.MasterApiActivityManager;
import io.smartspaces.master.api.master.MasterApiResourceManager;
import io.smartspaces.master.api.messages.MasterApiMessageSupport;
import io.smartspaces.master.ui.internal.web.BaseSpaceMasterController;

import org.springframework.web.multipart.MultipartFile;
import org.springframework.webflow.core.collection.MutableAttributeMap;
import org.springframework.webflow.execution.RequestContext;

import java.io.Serializable;
import java.util.Map;

/**
 * The webflow action for resource upload.
 *
 * @author Keith M. Hughes
 */
public class ResourceUploadAction extends BaseSpaceMasterController {

  /**
   * Manager for UI operations on resources.
   */
  private MasterApiResourceManager masterApiResourceManager;

  /**
   * Get a new activity model.
   *
   * @return new activity form
   */
  public ResourceForm newResource() {
    return new ResourceForm();
  }

  /**
   * Add entities to the flow context needed by the new entity page.
   *
   * @param context
   *          The Webflow context.
   */
  public void addNeededEntities(RequestContext context) {
    MutableAttributeMap viewScope = context.getViewScope();
    addGlobalModelItems(viewScope);
  }

  /**
   * Save the new resource.
   *
   * @param form
   *          resource form context
   *
   * @return status result
   */
  public String saveResource(ResourceForm form) {
    try {
      Map<String, Object> resourceResponse =
          masterApiResourceManager.saveResource(form.getResourceFile().getOriginalFilename(), form.getResourceFile().getInputStream());

      // So the ID gets copied out of the flow.
      if (MasterApiMessageSupport.isSuccessResponse(resourceResponse)) {

        return "success";
      } else {
        return handleError(form, MasterApiMessageSupport.getResponseDetail(resourceResponse));
      }
    } catch (Throwable e) {
      String message = (e instanceof SimpleSmartSpacesException)
          ? ((SimpleSmartSpacesException) e).getCompoundMessage()
          : SmartSpacesException.getStackTrace(e);

      spaceEnvironment.getLog().error("Could not get uploaded resource file\n" + message);

      return handleError(form, message);
    }
  }

  /**
   * handle an error from an activity upload attempt.
   *
   * @param form
   *          the submission form
   * @param responseDetail
   *          the detail of the error response
   *
   * @return the key for webflow for the error handling
   */
  private String handleError(ResourceForm form, String responseDetail) {
    // On an error, need to clear the activity file else flow serialization
    // fails.
    form.setResourceFile(null);
    form.setResourceError(responseDetail);

    return "error";
  }

  /**
   * @param masterApiActivityManager
   *          the masterApiActivityManager to set
   */
  public void setMasterApiResourceManager(MasterApiResourceManager masterApiResourceManager) {
    this.masterApiResourceManager = masterApiResourceManager;
  }

  /**
   * Form bean for activity objects.
   *
   * @author Keith M. Hughes
   */
  public static class ResourceForm implements Serializable {

    /**
     * The activity file.
     */
    private MultipartFile activityFile;

    /**
     * The activity error description.
     */
    private String resourceError;

    /**
     * Get the uploaded activity file.
     *
     * @return the uploaded file
     */
    public MultipartFile getResourceFile() {
      return activityFile;
    }

    /**
     * Set the uploaded activity file.
     *
     * @param activityFile
     *          the uploaded file
     */
    public void setResourceFile(MultipartFile activityFile) {
      this.activityFile = activityFile;
    }

    /**
     * Get the resource error.
     * 
     * @return the resource error
     */
    public String getResourceError() {
      return resourceError;
    }

    /**
     * Set an resource error message.
     *
     * @param resourceError
     *          resource error message
     */
    public void setResourceError(String resourceError) {
      this.resourceError = resourceError;
    }
  }
}
