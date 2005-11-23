@echo off

rem Run UMLGraph on the specified base file name

rem $Id$

if not "%2" == "" goto ok
echo usage: umlgraph base_file_name filetype
goto end

:ok
javadoc -docletpath lib/UmlGraph.jar -doclet gr.spinellis.umlgraph.doclet.UmlGraph -private -output %1.dot %1.java
dot -T%2 -o%1.%2 %1.dot

:end
