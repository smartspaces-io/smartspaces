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
<#import "/spring.ftl" as spring />
<html>
  <head>
    <title>Smart Spaces Admin: Resource Upload Error</title>

  <#include "/allpages_head.ftl">
  </head>

  <body class="admin-content">
    <h2>Error encountered during resource upload:</h2>
    <pre>${resourceForm.resourceError}</pre>
  </body>
</html>