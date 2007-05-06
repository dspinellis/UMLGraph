@echo off

rem Run UMLGraph on the specified base file name

rem $Id$

if not "%2" == "" goto ok
echo usage: umlgraph base_file_name filetype [umlgraph arguments]
goto end

:ok
javadoc -docletpath lib/UmlGraph.jar -package -doclet gr.spinellis.umlgraph.doclet.UmlGraph %3 %4 %5 %6 %7 %8 %9 -output %1.dot %1.java
dot -T%2 -o%1.%2 %1.dot

:end
