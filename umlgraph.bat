@echo off

rem Windows batch file to run UMLGraph on the specified base file name
rem For this to work you must adjust the following defintion of
rem UMLGRAPH_HOME to point to the directory where UmlGraph.jar is installed.

set UMLGRAPH_HOME=lib

if not "%2" == "" goto ok
echo usage: umlgraph base_file_name filetype [umlgraph arguments] 1>&2
echo example: umlgraph MyClass png 1>&2
echo (The above will convert MyClass.java into MyClass.png) 1>&2
goto end

:ok

java -classpath "%UMLGRAPH_HOME%\UmlGraph.jar;%JAVA_HOME%\lib\Tools.jar" org.umlgraph.doclet.UmlGraph -package %3 %4 %5 %6 %7 %8 %9 -output - %1.java | dot -T%2 -o%1.%2

:end
