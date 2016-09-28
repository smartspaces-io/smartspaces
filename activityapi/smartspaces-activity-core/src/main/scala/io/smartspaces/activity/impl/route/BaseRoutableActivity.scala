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

package io.smartspaces.activity.impl.route;

import io.smartspaces.activity.behavior.routing.StandardActivityRouting
import io.smartspaces.activity.impl.BaseActivity

/**
 * An {@link Activity} that simplifies the use of SmartSpaces routes.
 *
 * @author Keith M. Hughes
 */
class BaseRoutableActivity extends BaseActivity with StandardActivityRouting {
}
