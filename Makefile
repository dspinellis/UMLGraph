#
# $Id$
#

.SUFFIXES:.class .java
VERSION=2.6
TARBALL=UMLGraph-$(VERSION).tar.gz
ZIPBALL=UMLGraph-$(VERSION).zip
DISTDIR=UMLGraph-$(VERSION)
WEBDIR=/dds/pubs/web/home/sw/umlgraph
SRCFILE=UmlGraph.java sequence.pic README

.java.class:
	javac -classpath d:/jdk/lib/tools.jar $<

all: UmlGraph.jar

$(TARBALL): UmlGraph.jar docs Makefile
	-cmd /c rd /s/q $(DISTDIR)
	mkdir $(DISTDIR)
	mkdir $(DISTDIR)/doc
	cp $(WEBDIR)/doc/* $(DISTDIR)/doc
	cp UmlGraph.jar $(DISTDIR)
	for i in $(SRCFILE) ;\
	do\
	perl -p -e 'BEGIN {binmode(STDOUT);} s/\r//' $$i >$(DISTDIR)/$$i;\
	done
	tar cvf - $(DISTDIR) | gzip -c >$(TARBALL)
	zip -r $(ZIPBALL) $(DISTDIR)

docs:
	(cd doc && make)

UmlGraph.jar: UmlGraph.class
	jar cvf UmlGraph.jar ClassGraph.class ClassInfo.class Options.class \
	StringUtil.class UmlGraph.class
	jar i UmlGraph.jar

UmlGraph.class: UmlGraph.java

web: $(TARBALL)
	cp $(TARBALL) $(ZIPBALL) $(WEBDIR)
	cp UmlGraph.jar $(WEBDIR)/UmlGraph-$(VERSION).jar
	sed "s/VERSION/$(VERSION)/g" index.html >$(WEBDIR)/index.html
