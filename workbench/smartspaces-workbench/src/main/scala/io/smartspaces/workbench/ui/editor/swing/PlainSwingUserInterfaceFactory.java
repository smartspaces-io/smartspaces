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

package io.smartspaces.workbench.ui.editor.swing;

import io.smartspaces.workbench.project.source.Source;
import io.smartspaces.workbench.ui.SourceEditor;
import io.smartspaces.workbench.ui.UserInterfaceFactory;

/**
 * A {@link UserInterfaceFactory} using plain Swing widgets.
 *
 * @author Keith M. Hughes
 */
public class PlainSwingUserInterfaceFactory implements UserInterfaceFactory {

  @Override
  public SourceEditor newSourceEditor(Source source) {
    return new JTextAreaSourceEditor(source);
  }
}
