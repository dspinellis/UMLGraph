#
# $Id$
#

VERSION=4.1
TAGVERSION=$(shell echo $(VERSION) | sed 's/\./_/g')
TARBALL=UMLGraph-$(VERSION).tar.gz
ZIPBALL=UMLGraph-$(VERSION).zip
DISTDIR=UMLGraph-$(VERSION)
WEBDIR=/dds/pubs/web/home/sw/umlgraph
DOCLETSRCPATH=src/gr/spinellis/umlgraph/doclet
DOCLETSRC= \
	$(DOCLETSRCPATH)/ClassGraph.java \
	$(DOCLETSRCPATH)/ClassInfo.java \
	$(DOCLETSRCPATH)/Options.java \
	$(DOCLETSRCPATH)/StringUtil.java \
	$(DOCLETSRCPATH)/UmlGraph.java \
	$(DOCLETSRCPATH)/Version.java
TESTSRC = \
	src/gr/spinellis/umlgraph/test/DotDiff.java \
	src/gr/spinellis/umlgraph/test/BasicTest.java
PICFILE=sequence.pic
README=README.txt
OTHERSRC=index.html build.xml Makefile
# Files to tag
ALLTAG=$(DOCLETSRC) $(TESTSRC) $(PICFILE) $(README) $(OTHERSRC)

JARFILE=lib/UmlGraph.jar

# Remove carriage returns
LF=perl -p -e 'BEGIN {binmode(STDOUT);} s/\r//'

all: $(JARFILE)

tarball: $(TARBALL)

src/gr/spinellis/umlgraph/doclet/Version.java: Makefile
	echo "/* Automatically generated file */" >$@
	echo "package gr.spinellis.umlgraph.doclet;" >>$@
	echo "class Version { public static String VERSION = \"$(VERSION)\";}" >>$@

$(TARBALL): $(JARFILE) docs Makefile
	-cmd /c rd /s/q $(DISTDIR)
	mkdir $(DISTDIR)
	mkdir $(DISTDIR)/doc
	mkdir $(DISTDIR)/lib
	$(LF) $(README) >$(DISTDIR)/$(README)
	$(LF) $(PICFILE) >$(DISTDIR)/lib/$(PICFILE)
	cp $(JARFILE) $(DISTDIR)/lib
	cp $(WEBDIR)/doc/* $(DISTDIR)/doc
	cp build.xml $(DISTDIR)
	tar cf - src testdata/{java,dot-ref} --exclude='*/RCS' | tar -C $(DISTDIR) -xvf -
	$(LF) $(PICFILE) >$(DISTDIR)/src/$(PICFILE)
	tar cvf - $(DISTDIR) | gzip -c >$(TARBALL)
	zip -r $(ZIPBALL) $(DISTDIR)

docs:
	(cd doc && make)

$(JARFILE): $(DOCLETSRC)
	ant compile

test:
	ant test

web: $(TARBALL) CHECKSUM.MD5
	cp $(TARBALL) $(ZIPBALL) CHECKSUM.MD5 $(WEBDIR)
	cp UmlGraph.jar $(WEBDIR)/jars/UmlGraph-$(VERSION).jar
	sed "s/VERSION/$(VERSION)/g" index.html >$(WEBDIR)/index.html

CHECKSUM.MD5: $(TARBALL)
	md5 UMLGraph-2.10.* UMLGraph-$(VERSION).* UmlGraph.jar >CHECKSUM.MD5

tag:
	rcs -nV$(TAGVERSION): $(ALLTAG)
