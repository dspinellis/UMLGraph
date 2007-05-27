@echo off

rem Windows batch file to run UMLGraph on the specified base file name
rem For this to work the JDK tools.jar file must be located in the
rem same directory as UmlGraph.jar.  You also need to adjust the path
rem of UmlGraph.jar to the actual path where the jar file is installed.

rem $Id$

if not "%2" == "" goto ok
echo usage: umlgraph base_file_name filetype [umlgraph arguments] 1>&2
echo example: umlgraph MyClass png 1>&2
echo (The above will convert MyClass.java into MyClass.png) 1>&2
goto end

:ok
java -jar lib/UmlGraph.jar -package %3 %4 %5 %6 %7 %8 %9 -output - %1.java | dot -T%2 -o%1.%2

:end
