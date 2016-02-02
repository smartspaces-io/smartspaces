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

package io.smartspaces.service.template.internal.freemarker;

import io.smartspaces.service.BaseSupportedService;
import io.smartspaces.service.template.Templater;
import io.smartspaces.service.template.TemplaterService;

import java.io.File;

/**
 * A factory for templaters using Freemarker.
 *
 * @author Keith M. Hughes
 */
public class FreemarkerTemplaterService extends BaseSupportedService implements TemplaterService {

  @Override
  public String getName() {
    return TemplaterService.SERVICE_NAME;
  }

  @Override
  public Templater newTemplater(File templateDirectory) {
    return new FreemarkerTemplater(templateDirectory);
  }
}
