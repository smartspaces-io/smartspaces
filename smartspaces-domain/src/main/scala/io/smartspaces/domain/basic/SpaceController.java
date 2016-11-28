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

package io.smartspaces.domain.basic;

import io.smartspaces.domain.PersistedObject;

import java.io.Serializable;
import java.util.Map;

/**
 * A controller node in the space.
 *
 * @author Keith M. Hughes
 */
public interface SpaceController extends PersistedObject, Serializable {

	/**
	 * Get the host ID.
	 * 
	 * @return the hostId
	 */
	String getHostId();

	/**
	 * Set the host ID.
	 * 
	 * @param hostId
	 *            the hostId to set
	 */
	void setHostId(String hostId);

	/**
	 * Get the host name.
	 * 
	 * @return the host name
	 */
	String getHostName();

	/**
	 * Set the host name.
	 * 
	 * @param hostName
	 *            the host name to set
	 */
	void setHostName(String hostName);

	/**
	 * Get the port on the host for control.
	 * 
	 * @return the host control port
	 */
	int getHostControlPort();

	/**
	 * Set the port on the host for control.
	 * 
	 * @param hostControlPort
	 *            the host control port
	 */
	void setHostControlPort(int hostControlPort);

	/**
	 * Get the UUID.
	 * 
	 * @return the uuid
	 */
	String getUuid();

	/**
	 * Set the UUID.
	 * 
	 * @param uuid
	 *            the uuid to set
	 */
	void setUuid(String uuid);

	/**
	 * Get the name.
	 * 
	 * @return the name
	 */
	String getName();

	/**
	 * Set the name.
	 * 
	 * @param name
	 *            the name to set
	 */
	void setName(String name);

	/**
	 * Get the description.
	 * 
	 * @return the description
	 */
	String getDescription();

	/**
	 * Set the description.
	 * 
	 * @param description
	 *            the description to set
	 */
	void setDescription(String description);

	/**
	 * Set the metadata for the space controller.
	 *
	 * <p>
	 * This removes the old metadata completely.
	 *
	 * @param metadata
	 *            the metadata for the space controller (can be {@link null}
	 */
	void setMetadata(Map<String, Object> metadata);

	/**
	 * Get the metadata for the space controller.
	 *
	 * @return the space controller's meta data
	 */
	Map<String, Object> getMetadata();

	/**
	 * Get the controller configuration.
	 *
	 * @return the configuration, can be {@code null}
	 */
	SpaceControllerConfiguration getConfiguration();

	/**
	 * Set the controller configuration.
	 *
	 * @param configuration
	 *            the configuration, can be {@code null}
	 */
	void setConfiguration(SpaceControllerConfiguration configuration);

	/**
	 * Set the mode for this controller.
	 *
	 * @param mode
	 *            mode to set
	 */
	void setMode(SpaceControllerMode mode);

	/**
	 * Get the mode for this controller.
	 *
	 * @return mode of controller
	 */
	SpaceControllerMode getMode();
}
