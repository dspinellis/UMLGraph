#
# $Id$
#

PIC2PLOT=/app/plotutils/bin/pic2plot
NETPBM=/app/netpbm/bin
GS=/app/gs
DESTDIR=/dds/pubs/web/home/sw/umlgraph
NOTETOOLS=/dds/pubs/courses/tools
GIF := $(shell sed -n 's/.*<img *src *= *"\([^.]*\.gif\)".*/\1/p' *.xml)
XML := $(wildcard *.xml)
DOC?=snapshot-doc

.SUFFIXES:.java .dot .ps .gif .pic .eps .png

.java.dot:
	javadoc -docletpath ../lib/UmlGraph.jar -doclet gr.spinellis.umlgraph.doclet.UmlGraph -private $<
	mv graph.dot $@

.dot.eps:
	dot -Teps -o$@ $<

.dot.gif:
	dot -Tps $< | \
	$(GS)/gs -I$(GS) -I$(GS)/fonts -q -r360 -dNOPAUSE -sDEVICE=pnm -sOutputFile=-  - -c quit | \
	$(NETPBM)/pnmcrop | \
	$(NETPBM)/pnmscale 0.25 | \
	$(NETPBM)/ppmtogif >$@

.pic.eps:
	perl -p -e 'BEGIN {binmode(STDOUT);} s/\r//' $< | $(PIC2PLOT) -Teps >$@

# Could also use -Tgif, but going through Ghostscript produces a much better
# anti-aliased image
.pic.gif:
	perl -p -e 'BEGIN {binmode(STDOUT);} s/\r//' $< | \
	$(PIC2PLOT) -Tps | \
	$(GS)/gs -I$(GS) -I$(GS)/fonts -q -r360 -dNOPAUSE -sDEVICE=pnm -sOutputFile=-  - -c quit | \
	$(NETPBM)/pnmcrop | \
	$(NETPBM)/pnmscale 0.25 | \
	$(NETPBM)/ppmtogif >$@


all: sequence.pic $(GIF) $(XML)
	sh $(NOTETOOLS)/coursebib.sh bib
	echo "Drawing UML Diagrams with UMLGraph" | perl $(NOTETOOLS)/thread.pl -n -h html -r -p ../index.html index ${DESTDIR}/${DOC}

sequence.pic: ../sequence.pic
	perl -p -e 'BEGIN {binmode(STDOUT);} s/\r//' $< >$@
