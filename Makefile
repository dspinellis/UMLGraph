#
# $Id$
#

VERSION?=4.9
TAGVERSION=$(shell echo $(VERSION) | sed 's/\./_/g')
BALL_TAR_GZ=UMLGraph-$(VERSION).tar.gz
ZIPBALL=UMLGraph-$(VERSION).zip
DISTDIR=UMLGraph-$(VERSION)
WEBDIR=$(UH)/dds/pubs/web/home/sw/umlgraph
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
LICENSE=LICENSE
OTHERSRC=index.html build.xml Makefile
# Files to tag
ALLTAG=$(DOCLETSRC) $(TESTSRC) $(PICFILE) $(README) $(LICENSE) $(OTHERSRC)
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

tarball: $(BALL_TAR_GZ)

src/gr/spinellis/umlgraph/doclet/Version.java: Makefile
	ant -DVERSION="$(VERSION)" version

$(BALL_TAR_GZ): $(JARFILE) docs Makefile
	-cmd /c rd /s/q $(DISTDIR)
	mkdir $(DISTDIR)
	mkdir $(DISTDIR)/doc
	mkdir $(DISTDIR)/lib
	mkdir $(DISTDIR)/bin
	$(LF) $(README) >$(DISTDIR)/$(README)
	$(LF) $(LICENSE) >$(DISTDIR)/$(LICENSE)
	$(LF) $(PICFILE) >$(DISTDIR)/lib/$(PICFILE)
	cp $(JARFILE) $(DISTDIR)/lib
	cp $(WEBDIR)/$(DOC)/* $(DISTDIR)/doc
	cp build.xml $(DISTDIR)
	cp umlgraph.bat $(DISTDIR)/bin
	$(LF) umlgraph >$(DISTDIR)/bin/umlgraph
	tar cf - src testdata/{java,dot-ref} javadoc --exclude='*/RCS' | tar -C $(DISTDIR) -xvf -
	$(LF) $(PICFILE) >$(DISTDIR)/src/$(PICFILE)
	tar czf $(BALL_TAR_GZ) $(DISTDIR)
	zip -r $(ZIPBALL) $(DISTDIR)

docs:
	(cd doc && make DOC=$(DOC))
	ant javadocs

$(JARFILE): $(DOCLETSRC)
	ant compile

test:
	ant test

web: $(BALL_TAR_GZ) CHECKSUM.MD5
	cp $(BALL_TAR_GZ) $(ZIPBALL) CHECKSUM.MD5 $(WEBDIR)
	cp $(JARFILE) $(WEBDIR)/jars/UmlGraph-$(VERSION).jar
	tar cf - javadoc | tar -C $(WEBDIR) -xvf -
	sed "s/VERSION/$(VERSION)/g" index.html >$(WEBDIR)/index.html

CHECKSUM.MD5: $(BALL_TAR_GZ) $(JARFILE)
	md5 UMLGraph-2.10.* UMLGraph-$(VERSION).* >CHECKSUM.MD5
	(cd lib ; md5 UmlGraph.jar) >>CHECKSUM.MD5

tag:
	rcs -nV$(TAGVERSION): $(ALLTAG)
