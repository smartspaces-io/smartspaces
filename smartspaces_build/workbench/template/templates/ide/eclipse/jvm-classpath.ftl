<?xml version="1.0" encoding="UTF-8"?>
<classpath>

<#list srcs as src>
	<classpathentry kind="src" path="${src}"/>
</#list>
<#list launchers as launcher>
	<classpathentry kind="con" path="${launcher}" />
</#list>

<#list dynamicProjects as dynamicProject>
    <classpathentry kind="src" path="/${dynamicProject.identifyingName}" exported="true"/>
</#list>

<#list libs as lib>
	<classpathentry kind="lib" path="${lib}"/>
</#list>

	<classpathentry kind="output" path="bin"/>
</classpath>
