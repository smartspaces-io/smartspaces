<#--
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
 -->
<!DOCTYPE html>
<html>
<head>
<title>Smart Spaces Admin: Support</title>

<#include "/allpages_head.ftl">

<script type="text/javascript">
function doAjaxCommand(command) {
  $.ajax({
      url: '/smartspaces/admin/support/' + command + '.json',
      success: function(data) {
        $('#commandResult').html(data.result);
      }
  });
}

function importMasterDomainModel() {
    if (confirm("Are you sure you want to import the domain model?")) {
        doAjaxCommand("importMasterDomainModel");
    }
}

function hardRestartMaster() {
    if (confirm("Are you sure you want to hard restart the master?")) {
        doAjaxCommand("hardRestartMaster");
    }
}

function softRestartMaster() {
    if (confirm("Are you sure you want to soft restart the master?")) {
        doAjaxCommand("softRestartMaster");
    }
}
</script>

</head>

<body class="admin-content">

<h2>Support</h2>

<table class="commandBar">
  <tr>
    <td><button type="button" onclick="doAjaxCommand('exportMasterDomainModel')" title="Export the Master Domain Model">Export Master Model</button></td>
    <td><button type="button" onclick="importMasterDomainModel()" title="Import the Master Domain Model">Import Master Model</button></td>
    <td><button type="button" onclick="hardRestartMaster()" title="Hard restart the master">Hard Restart Master</button></td>
    <td><button type="button" onclick="softRestartMaster()" title="Soft restart the master">Soft Restart Master</button></td>
  </tr>
</table>

<div id="commandResult">
</div>

<div id="content">

<table>

<tr>
  <td>Smart Spaces Version</td>
  <td>${smartSpacesVersion}</td>
</tr>

</table>

</div>

</body>
<html>