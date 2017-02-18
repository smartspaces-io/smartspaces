<#--
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
 -->
<!DOCTYPE html>
<html>
<head>
<title>Smart Spaces Admin: Resources</title>

<#include "/allpages_head.ftl">

</head>

<body class="admin-content">

<h2>Resources</h2>

<table class="commandBar">
  <tr>
    <td><button type="button" id="uploadButton" onclick="ugly.changePage('/smartspaces/resource/upload.html?mode=embedded')" title="Upload a resource">Upload</button></td>
  </tr>
</table>

<div id="commandResult">
</div>

<table class="activity-list">
  <tr>
    <th></th>
    <th>Identifying Name</th>
    <th>Version</th>
    <th>Last Uploaded</th>
    <th>Bundle Content Hash</th>
  </tr>
<#list resources as resource>
    <#assign trCss = (resource_index % 2 == 0)?string("even","odd")>
    <tr class="${trCss}">
      <td><a class="uglylink" onclick="return ugly.changePage('/smartspaces/resource/${resource.id}/view.html', event);">View</a></td>
      <td>${resource.identifyingName?html}</td>
      <td>${resource.version}</td>
      <td>${resource.lastUploadDate?datetime}</td>
      <td class="resource-bundle-content-hash"><#if resource.bundleContentHash??>${resource.bundleContentHash}</#if></td>
    </tr>
</#list>
</table>
</body>
<html>