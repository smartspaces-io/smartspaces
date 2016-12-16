Running Smart Spaces
*****************************

Smart Spaces comes with a set of tools for managing your Smart Space environment. This
section will cover both the beginning and advanced usage. Those who are not familiar with
the details of operating systems, processes, and installing system services might want to
wait on the Advanced section, it isn't necessary to start playing with Smart Spaces.


Basic Running of Smart Spaces
=============================

Smart Spaces has a few basic commands for starting and stopping the Master and Space Controllers.

Manually Starting a Smart Spaces Container
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Manually starting a Smart Spaces container is very easy. 

The shell command for starting a container is


::

  bin/smartspaces

Manually Shutting Down a Smart Spaces Container
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Shutting down a Smart Spaces container is very easy. You have several choices.

You can do a shutdown of a Space Controller from the Master Web Admin, from a shell
command on the machine the Space Controller is running on, or through the OSGi console. 
Masters can only be shut down from a shell command on the machine the Master or from the OSGi console.

The shell command for shutting down a container is


::

  bin/smartspaces shutdown

If you wish to shutdown the container from the container's OSGi console, you can type the command


::

  shutdown

or you can type ``^D``, where ``^`` is the Control key on your keyboard.

Advanced Running of Smart Spaces
================================

Smart Spaces has several advanced usages.

Restarting a Smart Spaces Container
-----------------------------------

It is possible to restart a running Smart Spaces container and have it immediately start running
again without needing to manually restart it. This can be useful if you are controlling
the container remotely or have screwed up the container fairly majorly during development.

You can do a hard or soft restart.

Hard Restart
~~~~~~~~~~~~

A hard restart totally exits the operating system process that the container is running in.
All external files are re-read that set up the Smart Spaces runtime environment, such as the
JVM boot classpath and the JVM process is started from scratch.

A hard restart is useful when you either want to have those environment files reread or when
memory has gotten so screwed up that your best solution is to restart the process.

You can do a hard restart on a Space Controller from the Master Web Admin, or from a shell
command on the machine the Space Controller is running on. Masters can only be restarted 
from a shell command on the machine the Master.

The shell command for hard restarting a container is


::

  bin/smartspaces hardrestart


Soft Restart
~~~~~~~~~~~~

A soft restart leaves the operating system container process running
and merely restarts the OSGi framework. All extensions files are reread and the framework
bootstrap classloader is rebuilt. The JVM bootstrap classloader and all JVM environment
variables and command line arguments are not changed, this would require a process restart.
If you need this, then do a hard restart.

A hard restart is useful when you either want to have those environment files reread or when
memory has gotten so screwed up that your best solution is to restart the process.

You can do a soft restart on a Space Controller from the Master Web Admin, or from a shell
command on the machine the Space Controller is running on. Masters can only be restarted 
from a shell command on the machine the Master.

The shell command for soft restarting a container is

::

  bin/smartspaces softrestart


Running as Server or Daemon
---------------------------

Smart Spaces can be run in a server mode or a daemon. These disable the console shell.

::

  bin/smartspaces server

or


::

  bin/smartspaces daemon

These commands are used by some of the other scripts in the ``bin`` folder.

