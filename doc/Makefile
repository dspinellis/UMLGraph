#
#

ROOT=/cygdrive/c
PIC2PLOT=pic2plot
GS=gs
DESTDIR=/dds/pubs/web/home/umlgraph
NOTETOOLS=/dds/pubs/Courses/tools
GIF := $(shell sed -n 's/.*<img *src *= *"\([^.]*\.gif\)".*/\1/p' *.xml)
XML := $(wildcard *.xml)
DOC?=snapshot-doc

.SUFFIXES:.java .dot .ps .gif .pic .eps .png

.java.dot:
	javadoc -docletpath ../lib/UmlGraph.jar -doclet org.umlgraph.doclet.UmlGraph -private $<
	mv graph.dot $@

.dot.eps:
	dot -Teps -o$@ $<

.dot.gif:
	dot -Tgif $< -o$@

.pic.eps:
	perl -p -e 'BEGIN {binmode(STDOUT);} s/\r//' $< | $(PIC2PLOT) -Teps >$@

# Could also use -Tgif, but going through Ghostscript produces a much better
# anti-aliased image
.pic.gif:
	perl -p -e 'BEGIN {binmode(STDOUT);} s/\r//' $< | \
	$(PIC2PLOT) -Tps | \
	$(GS) -q -r360 -dNOPAUSE -sDEVICE=pnm -sOutputFile=-  - -c quit | \
	pnmcrop | \
	pnmscale 0.25 | \
	ppmtogif >$@


all: sequence.pic $(GIF) $(XML)
	sh $(ROOT)/$(NOTETOOLS)/coursebib.sh bib
	echo "Drawing UML Diagrams with UMLGraph" | /c/Perl/bin/perl $(NOTETOOLS)/thread.pl -n -h html -r -p ../index.html index ${DESTDIR}/${DOC}

sequence.pic: ../sequence.pic
	perl -p -e 'BEGIN {binmode(STDOUT);} s/\r//' $< >$@
