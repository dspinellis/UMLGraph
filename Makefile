VERSION:=$(shell git describe --abbrev=6 HEAD | sed 's/R//;s/_/./;s/-/./;s/-.*$$/-SNAPSHOT/')
BALL_TAR_GZ=UMLGraph-$(VERSION).tar.gz
ZIPBALL=UMLGraph-$(VERSION).zip
DISTDIR=UMLGraph-$(VERSION)
WEBDIR=$(UH)/dds/pubs/web/home/umlgraph
DOCLETSRCPATH=src/main/java/org/umlgraph/doclet
DOCLETSRC= \
	$(DOCLETSRCPATH)/ClassGraph.java \
	$(DOCLETSRCPATH)/ClassInfo.java \
	$(DOCLETSRCPATH)/Options.java \
	$(DOCLETSRCPATH)/StringUtil.java \
	$(DOCLETSRCPATH)/UmlGraph.java \
	$(DOCLETSRCPATH)/Version.java
TESTSRC = \
	src/main/java/org/umlgraph/test/DotDiff.java \
	src/main/java/org/umlgraph/test/BasicTest.java
PICFILE=sequence.pic
README=README.txt
LICENSE=LICENSE
OTHERSRC=index.html build.xml Makefile oldversion.html
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
	tar cf - src testdata/{java,dot-ref} javadoc --exclude='*/CVS' | tar -C $(DISTDIR) -xvf -
	$(LF) $(PICFILE) >$(DISTDIR)/src/$(PICFILE)
	tar czf $(BALL_TAR_GZ) $(DISTDIR)
	zip -r $(ZIPBALL) $(DISTDIR)

docs:
	(cd $(UH)/dds/pubs/Courses/tools && make)
	(cd doc && make DOC=$(DOC))
	ant javadocs

$(JARFILE): $(DOCLETSRC)
	ant compile

test:
	ant test

testupdate:
	bash tools/testupdate.sh

# Create only the static HTML pages
static-web:
	(cd web && sh build.sh)
	cp web/build/* $(WEBDIR)
	sed "s/VERSION/$(VERSION)/g" web/build/download.html >$(WEBDIR)/download.html

web: $(BALL_TAR_GZ) CHECKSUM.MD5
	cp $(BALL_TAR_GZ) $(ZIPBALL) CHECKSUM.MD5 $(WEBDIR)
	(cd web && sh build.sh)
	cp web/build/* $(WEBDIR)
	sed "s/VERSION/$(VERSION)/g" web/build/download.html >$(WEBDIR)/download.html
	cp $(JARFILE) $(WEBDIR)/jars/UmlGraph-$(VERSION).jar
	tar cf - javadoc | tar -C $(WEBDIR) -xvf -

CHECKSUM.MD5: $(BALL_TAR_GZ) $(JARFILE)
	openssl md5 legacy/UMLGraph-2.10.* legacy/UMLGraph-4.8.* UMLGraph-$(VERSION).* >CHECKSUM.MD5
	(cd lib ; openssl md5 UmlGraph.jar) >>CHECKSUM.MD5

tags:
	etags **/*.java
