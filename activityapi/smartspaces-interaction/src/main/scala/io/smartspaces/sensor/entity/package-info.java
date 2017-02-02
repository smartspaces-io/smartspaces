/*
 * Copyright (C) 2015 Keith M. Hughes
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

/**
 * An API for talking about entities, such as sensors, physical locations, etc.
 * 
 * <p>
 * A sensor is a small hardware or platform that can contain multiple subsensor
 * values. An example may be a small board with a WIFI chip and multiple
 * invidual sensors, such as temperature and humidity.
 * 
 * <p>
 * A sensor channel gives the value that a particular subsensor is reading from
 * a sensor platform. For example, one channel may give the temperature of the
 * room while another gives the humidity of the room. Multiple channels could
 * give the same sort of measurement, though for something else, for example the
 * sensor platform could have two temperature sensors, one to read room
 * temperature and another to read the fish tank temperature.
 * 
 * <p>
 * Markers are things that allow something to be identified.
 * 
 * @author Keith M. Hughes
 */
package io.smartspaces.sensor.entity;