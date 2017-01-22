@echo off
rem
rem
rem This file adapted for SmartSpaces from the similar file in the
rem Apache Karaf project.
rem
rem    Licensed to the Apache Software Foundation (ASF) under one or more
rem    contributor license agreements.  See the NOTICE file distributed with
rem    this work for additional information regarding copyright ownership.
rem    The ASF licenses this file to You under the Apache License, Version 2.0
rem    (the "License"); you may not use this file except in compliance with
rem    the License.  You may obtain a copy of the License at
rem
rem       http://www.apache.org/licenses/LICENSE-2.0
rem
rem    Unless required by applicable law or agreed to in writing, software
rem    distributed under the License is distributed on an "AS IS" BASIS,
rem    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
rem    See the License for the specific language governing permissions and
rem    limitations under the License.
rem

if not "%ECHO%" == "" echo %ECHO%

setlocal
set DIRNAME=%~dp0%
set PROGNAME=%~nx0%
set ARGS=%*

rem Sourcing environment settings for smartspaces similar to tomcats setenv
SET SMARTSPACES_SCRIPT="start.bat"
if exist "%DIRNAME%setenv.bat" (
  call "%DIRNAME%setenv.bat"
)

goto BEGIN

:warn
    echo %PROGNAME%: %*
goto :EOF

:BEGIN

rem # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # #

if not "%SMARTSPACES_HOME%" == "" (
    call :warn Ignoring predefined value for SMARTSPACES_HOME
)
set SMARTSPACES_HOME=%DIRNAME%..
if not exist "%SMARTSPACES_HOME%" (
    call :warn SMARTSPACES_HOME is not valid: "%SMARTSPACES_HOME%"
    goto END
)

if not "%SMARTSPACES_BASE%" == "" (
    if not exist "%SMARTSPACES_BASE%" (
       call :warn SMARTSPACES_BASE is not valid: "%SMARTSPACES_BASE%"
       goto END
    )
)
if "%SMARTSPACES_BASE%" == "" (
  set "SMARTSPACES_BASE=%SMARTSPACES_HOME%"
)

if not "%SMARTSPACES_DATA%" == "" (
    if not exist "%SMARTSPACES_DATA%" (
        call :warn SMARTSPACES_DATA is not valid: "%SMARTSPACES_DATA%"
        goto END
    )
)
if "%SMARTSPACES_DATA%" == "" (
    set "SMARTSPACES_DATA=%SMARTSPACES_BASE%\data"
)

if not "%SMARTSPACES_ETC%" == "" (
    if not exist "%SMARTSPACES_ETC%" (
        call :warn SMARTSPACES_ETC is not valid: "%SMARTSPACES_ETC%"
        goto END
    )
)
if "%SMARTSPACES_ETC%" == "" (
    set "SMARTSPACES_ETC=%SMARTSPACES_BASE%\etc"
)

if "%SMARTSPACES_TITLE%" == "" (
    set "SMARTSPACES_TITLE=Smartspaces"
)

:EXECUTE
    start "%SMARTSPACES_TITLE%" /MIN "%SMARTSPACES_HOME%\bin\smartspaces.bat" server %*

rem # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # #

:END

endlocal

if not "%PAUSE%" == "" pause

:END_NO_PAUSE

