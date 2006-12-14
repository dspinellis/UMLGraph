#
# $Id$
#

VERSION?=4.5
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
# Documentation location (release)
ifeq ($(VERSION),snapshot)
	DOC=snapshot-doc
else
	DOC=doc
endif

JARFILE=lib/UmlGraph.jar

# Remove carriage returns
LF=perl -p -e 'BEGIN {binmode(STDOUT);} s/\r//'

all: $(JARFILE)

tarball: $(TARBALL)

src/gr/spinellis/umlgraph/doclet/Version.java: Makefile
	ant -DVERSION="$(VERSION)" version

$(TARBALL): $(JARFILE) docs Makefile
	-cmd /c rd /s/q $(DISTDIR)
	mkdir $(DISTDIR)
	mkdir $(DISTDIR)/doc
	mkdir $(DISTDIR)/lib
	$(LF) $(README) >$(DISTDIR)/$(README)
	$(LF) $(PICFILE) >$(DISTDIR)/lib/$(PICFILE)
	cp $(JARFILE) $(DISTDIR)/lib
	cp $(WEBDIR)/$(DOC)/* $(DISTDIR)/doc
	cp build.xml $(DISTDIR)
	tar cf - src testdata/{java,dot-ref} javadoc --exclude='*/RCS' | tar -C $(DISTDIR) -xvf -
	$(LF) $(PICFILE) >$(DISTDIR)/src/$(PICFILE)
	tar cvf - $(DISTDIR) | gzip -c >$(TARBALL)
	zip -r $(ZIPBALL) $(DISTDIR)

docs:
	(cd doc && make DOC=$(DOC))
	ant javadocs

$(JARFILE): $(DOCLETSRC)
	ant compile

test:
	ant test

web: $(TARBALL) CHECKSUM.MD5
	cp $(TARBALL) $(ZIPBALL) CHECKSUM.MD5 $(WEBDIR)
	cp $(JARFILE) $(WEBDIR)/jars/UmlGraph-$(VERSION).jar
	tar cf - javadoc | tar -C $(WEBDIR) -xvf -
	sed "s/VERSION/$(VERSION)/g" index.html >$(WEBDIR)/index.html

CHECKSUM.MD5: $(TARBALL) $(JARFILE)
	md5 UMLGraph-2.10.* UMLGraph-$(VERSION).* >CHECKSUM.MD5
	(cd lib ; md5 UmlGraph.jar) >>CHECKSUM.MD5

tag:
	rcs -nV$(TAGVERSION): $(ALLTAG)
