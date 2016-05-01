/*
 * Copyright (C) 2016 Keith M. Hughes
 * Copyright (C) 2015 Google Inc.
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

package io.smartspaces.master.server.services;

import io.smartspaces.SimpleSmartSpacesException;
import io.smartspaces.SmartSpacesException;
import io.smartspaces.domain.basic.SpaceController;
import io.smartspaces.resource.TypedId;

/**
 * The base implementation of the {@link SpaceControllerRepository}. It provides
 * common implementation.
 *
 * @author Keith M. Hughes
 */
public abstract class BaseSpaceControllerRepository implements SpaceControllerRepository {

  @Override
  public SpaceController getSpaceControllerByTypedId(String typedIdString)
      throws SmartSpacesException {
    TypedId typedId =
        TypedId.newTypedID(TYPED_ID_TYPE_COMPONENT_SEPARATOR, TYPED_ID_TYPE_DEFAULT, typedIdString);
    switch (typedId.getType()) {
      case TYPED_ID_TYPE_ID:
        return getSpaceControllerById(typedId.getId());
      case TYPED_ID_TYPE_UUID:
        return getSpaceControllerByUuid(typedId.getId());
      default:
        throw SimpleSmartSpacesException.newFormattedException(
            "Unknown typed ID type %s while getting space controller", typedId.getType());
    }
  }
}
