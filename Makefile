#
# $Id$
#

.SUFFIXES:.class .java
VERSION=2.0
TARBALL=UMLGraph-$(VERSION).tar.gz
DISTDIR=UMLGraph-$(VERSION)
WEBDIR=/dds/pubs/web/home/sw/umlgraph
SRCFILE=UmlGraph.java sequence.pic README

.java.class:
	javac -classpath d:/jdk/lib/tools.jar $<

$(TARBALL): UmlGraph.jar docs Makefile
	cmd /c rd /s/q $(DISTDIR)
	mkdir $(DISTDIR)
	mkdir $(DISTDIR)/doc
	cp $(WEBDIR)/doc/* $(DISTDIR)/doc
	cp UmlGraph.jar $(DISTDIR)
	for i in $(SRCFILE) ;\
	do\
	perl -p -e 'BEGIN {binmode(STDOUT);} s/\r//' $$i >$(DISTDIR)/$$i;\
	done
	tar cvf - $(DISTDIR) | gzip -c >$(TARBALL)

docs:
	(cd doc && make)

UmlGraph.jar: UmlGraph.class
	jar cvf UmlGraph.jar ClassGraph.class ClassInfo.class Options.class \
	StringFuns.class UmlGraph.class
	jar i UmlGraph.jar

UmlGraph.class: UmlGraph.java

web: $(TARBALL)
	cp $(TARBALL) $(WEBDIR)
	sed "s/VERSION/$(VERSION)/g" index.html >$(WEBDIR)/index.html
