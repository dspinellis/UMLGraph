# This works for me; your mileage may vary - dds
#
# $Id$
#

.SUFFIXES:.class .java .dot .ps .gif

.java.dot:
	javadoc -docletpath UmlGraph.jar -doclet UmlGraph -private $<
	mv graph.dot $@

.java.class:
	javac -classpath /jdk/lib/tools.jar $<

.dot.ps:
	dot -Tps -o$@ $<

.dot.gif:
	dot -Tgif -o$@ $<

#all: UmlGraph.class vis.gif schema.gif general.gif catalina.gif advrel.gif assoc.gif classadd.gif
#all: UmlGraph.class vis.ps schema.ps general.ps catalina.ps advrel.ps assoc.ps classadd.ps
all: UmlGraph.jar vis.dot schema.dot general.dot catalina.dot advrel.dot assoc.dot classadd.dot

UmlGraph.jar: UmlGraph.class
	jar cvf UmlGraph.jar ClassGraph.class ClassInfo.class Options.class \
	StringFuns.class UmlGraph.class
	jar i UmlGraph.jar

UmlGraph.class: UmlGraph.java

web: UmlGraph.jar
	cmd /c del /f/q \dds\pubs\web\home\sw\umlgraph
	cp *.java *.jar *.gif *.dot index.html makefile /dds/pubs/web/home/sw/umlgraph
