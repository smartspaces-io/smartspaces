##
# Copyright (C) 2016 Keith M. Hughes
# Copyright (C) 2012 Google Inc.
#  
#  Licensed under the Apache License, Version 2.0 (the "License"); you may not
#  use this file except in compliance with the License. You may obtain a copy of
#  the License at
#  
#  http://www.apache.org/licenses/LICENSE-2.0
#  
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
#  WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
#  License for the specific language governing permissions and limitations under
#  the License.
##

from io.smartspaces.activity.impl.ros import BaseRoutableRosActivity

class ExampleOutputRoutablePythonActivity(BaseRoutableRosActivity):

    def onActivityActivate(self):
        self.log.info("Activated")
        message = {}
        message["message"] = "yipee! activated!"
        self.sendOutputMessage("output1", message)

    def onActivityDeactivate(self):
        self.log.info("Deactivated")
        message = {}
        message["message"] = "bummer! deactivated!"
        self.sendOutputMessage("output1", message)
