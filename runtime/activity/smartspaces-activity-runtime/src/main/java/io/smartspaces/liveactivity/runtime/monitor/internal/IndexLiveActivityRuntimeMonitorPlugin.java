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

package io.smartspaces.liveactivity.runtime.monitor.internal;

import io.smartspaces.liveactivity.runtime.monitor.LiveActivityRuntimeMonitorPlugin;
import io.smartspaces.liveactivity.runtime.monitor.PluginFunctionalityDescriptor;
import io.smartspaces.service.web.server.HttpRequest;
import io.smartspaces.service.web.server.HttpResponse;

import java.io.OutputStream;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.google.common.collect.Lists;

/**
 * The live activity monitor plugin that provides a web index for all the
 * monitoring plugins.
 *
 * @author Keith M. Hughes
 */
public class IndexLiveActivityRuntimeMonitorPlugin extends BaseLiveActivityRuntimeMonitorPlugin {

  /**
   * The URL prefix for this plugin.
   */
  public static final String URL_PREFIX = "/";

  /**
   * A comparator for sorting plugins in alphabetically increasing order.
   */
  public static final Comparator<PluginFunctionalityDescriptor> FUNCTIONALITY_DESCRIPTOR_COMPARATOR =
      new Comparator<PluginFunctionalityDescriptor>() {

        @Override
        public int compare(PluginFunctionalityDescriptor o1, PluginFunctionalityDescriptor o2) {
          return o1.getDisplayName().compareToIgnoreCase(o2.getDisplayName());
        }
      };

  /**
   * The functionality descriptors for this plugin.
   */
  private List<PluginFunctionalityDescriptor> functionalityDescriptors = Collections
      .unmodifiableList(Lists.newArrayList(new PluginFunctionalityDescriptor(URL_PREFIX, "Index")));

  @Override
  public String getUrlPrefix() {
    return URL_PREFIX;
  }

  @Override
  public List<PluginFunctionalityDescriptor> getFunctionalityDescriptors() {
    return functionalityDescriptors;
  }

  @Override
  protected void
      onHandleRequest(HttpRequest request, HttpResponse response, String fullRequestPath)
          throws Throwable {
    OutputStream outputStream = startWebResponse(response, false);
    addCommonPageHeader(outputStream, "Debugging");

    List<PluginFunctionalityDescriptor> descriptors = Lists.newArrayList();
    for (LiveActivityRuntimeMonitorPlugin plugin : getMonitorService().getPlugins()) {
      descriptors.addAll(plugin.getFunctionalityDescriptors());
    }
    Collections.sort(descriptors, FUNCTIONALITY_DESCRIPTOR_COMPARATOR);

    StringBuilder links = new StringBuilder();
    links.append("<ul>");
    for (PluginFunctionalityDescriptor descriptor : descriptors) {
      links.append("<li>");
      addLink(links, descriptor.getUrl(), descriptor.getDisplayName());
      links.append("</li>");
    }
    links.append("</ul>");
    outputStream.write(links.toString().getBytes());

    endWebResponse(outputStream, false);
  }

}
