UMLGraph - Declarative Drawing of UML Diagrams

UMLGraph allows the declarative specification and drawing of
UML class and sequence diagrams.  You can browse the system's
documentation from the doc/index.html page, or print it from
doc/indexw.html.

To compile the Java doclet from the source code simply execute:
	javac -classpath YOUR_JDK_DIRECTORY/lib/tools.jar UmlGraph.java
	jar cvf UmlGraph.jar ClassGraph.class ClassInfo.class Options.class StringFuns.class UmlGraph.class
	jar i UmlGraph.jar

Project home page: http://www.spinellis.gr/sw/umlgraph

Diomidis Spinellis - May 2004
