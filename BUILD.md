# Requirements

This file will describe how to build Smart Spaces.

This process is not for the faint of heart and perhaps it is best to
use the installers. But if you want to try the cutting edge, or perhaps
have a desire to torture yourself, then this file is for you.

The overall steps are:

- Clone the Smart Spaces git repo and the the Smart Spaces Dependency git repo.
- Set up the other dependencies.
- Create a file in the root folder of the Smart Spaces project called gradle.properties.
- Build

Some folks have figured out how to build Smart Spaces on operating systems
other than Linux in the past, though these folks are no longer interested
in helping with this. Non-Linux builds are not officially supported.
If you want to build
on a Mac or Windows, it is suggested you use a virtual machine through Vagrant
or some other package to build Smart Spaces.

# The git repositories.

It is assumed you know how to use git. If not, there are many good websites
with excellent documentation.

First clone the smartspaces repository. I typicall do this in a
folder `software/repos`.

```
git clone https://github.com/smartspaces-io/smartspaces.git
```

Next you need the dependencies repository. There are a collection of
dependencies that Smart Spaces has that cannot be
easily pulled from repositories, particularly in the format that the
Smart Spaces build system needs them in. These dependencies have been
placed in a GitHub repository.

You can place them in any folder you like, but I usually place them in the same
folder as the SmartSpaces git repo.

```
git clone https://github.com/smartspaces-io/smartspaces-dependencies.git
```

The file system might then look something like this:

```
keith@inhabitecheng1:~/software/repos$ ls
smartspaces               smartspaces-sandbox
smartspaces-dependencies
```

# Other dependencies

Smart Spaces has a collection of other dependencies needed to build it.

## ROS

Smart Spaces makes use of ROS, the Robot Operating System.

Install ROS on your computer. Install from www.ros.org. I usually install the
desktop full version, though `ros-core` is also sufficient.

It doesn't particularly matter which version of ROS you use, though Smart Spaces
has only been tested with versions Indigo and forward.

Make sure you follow all of the directions needed for the ROS install,
particularly the changes documented for your `.bashrc` file.

```
sudo apt-get install ros-indigo-desktop-full
```

You now need to make sure that the ROS packages can find the Smart Spaces
code. To do this, you need to add Smart Spaces to the ROS package path.

Your `.bashrc` should now contain a line like

```
source /opt/ros/indigo/setup.bash
```
Underneath this line add

```
export ROS_PACKAGE_PATH=smartspacesrepo:${ROS_PACKAGE_PATH}
```

where `smartspacesrepo` is the file path to the folder that is your clone of
the smartspaces repository is. In the example install above, this would be

```
export ROS_PACKAGE_PATH=/home/you/software/repos/smartspaces:${ROS_PACKAGE_PATH}
```

Another way to do this is to place the Smart Spaces repository in the ROS
share directory, though this ties you to a ROS version and is not recommended.

```
sudo mv smartspaces /opt/ros/indigo/share/
```

## Smart Spaces documentation generation

The Smart Spaces documentation is built with the Python documentation 
system Sphinx. It uses Latex for building the PDF documentation. On Linux, make
sure you install texlive, textlive-latex-extra, and texlive-fonts-*. You also
need pygments.

```
sudo apt-get install python-sphinx texlive texlive-latex-extra texlive-fonts-* 
sudo pip install pygments
```

You may also want to install pthread library:

```
sudo apt-get install libevent-pthreads-2.0-5
```

# The gradle.properties file

Smart Spaces uses the most excellent Gradle build system.

There are some things that Gradle needs to know about your setup.
These things go into the `gradle.properties` file at the root of your
clone of the Smart Spaces repository.

You can copy, and are advised to copy, the `gradle.properties.default` file
to `gradle.properties` as a start.

```
cp gradle.properties.default gradle.properties
```

This file should contain the following properties. Each of the properties
ending in .home say where you have a package installed. The examples
below give a fake place where each software package is installed, you will
change these to match where you installed the particular packages.

`smartspaces.dependencies.home` should point at where you have installed
the git repo clone for the Smart Spaces Dependencies repo.


```
smartspaces.dependencies.home = /home/you/software/repos/smartspaces-dependencies
```

It has not been easy to have the Smart Spaces build work properly with the
Gradle build daemon, so it is best to tell Gradle not to use a daemon.

```
org.gradle.daemon=false
```

Smart Spaces does have a Maven repository where it stores some of its
dependencies. The `nexusUrl` property specifies which Maven repository
to look at for SmartSpaces information.

The official repository is hosted at inhabitech.com.

```
nexusUrl=https://eng.inhabitech.com:8084
```

Some of the other properties are described later in the **Building** section.

# Building

Smart Spaces uses the Gradle build tool. There is no need to download
Gradle,it will be downloaded automatically by the build process.

The simplest way to start a build is to use the command

```
./gradlew clean install
```

This will clean everything out and place the Smart Spaces jar files in your
local Maven repository.

## Controlling Test Sizes

Automatic tests that take place during a build come in a variety of sizes.
Small tests are run every time a build is done, while large tests are only done
if explicitly asked for. The following shows how to run the large tests.

```
./gradlew -PtestSize=large clean install
```

## Building a dev instance

A dev instance of Smart Spaces is an installation of Smart Spaces that you are
using for development work, either for Smart Spaces itself or for any
Smart Spaces projects you are working on.

A dev build of Smart Spaces will install the build into your development
instance and not touch any databases, configuration files, or anything
in the `startup` folder.

Add the following to your `gradle.properties` file.

```
smartspaces.dev.home=/home/you/smartspaces
smartspaces.dev.home.subdir.default=latest
```

To build Smart Spaces and install updated files into your development
instance, use

```
./gradlew installDev
```

which will copy everything into `/home/you/smartspaces/latest`.

If you are working with a couple of different containers, say one called
`mytest`
in addition to using `latest`, you can use the command

```
./gradlew -PdevHomeSubdir=mytest installDev
```

which will copy everything into `/home/you/smartspaces/mytest`.

## Building installers

If you want to build the installers, use the following command

```
./gradlew createInstallers
```

This will create installers for the master, controller, and workbench. The
installers will be found in

```
smartspaces_build/*/build/distributions
```

where * can be master, controller, or workbench.

## Building an Image:

This is only useful if using SpaceOperations.

```
./gradlew -PimageHome=path createImage
```

where path is the root folder which will receive the image.

The image will contain a master, controller, and workbench.

## Using IntelliJ

If you want to work with Smartspaces using a Gradle-based IntelliJ project,
you should add the following Gradle VM option.

```
-DINTELLIJ=true
```
