#!/bin/sh
#
# Visually compare the dot files reported as different when running
# the test.
#
# Specify as an argument the name of the file reported as different.
# Example: viscompare.sh Category.dot
# Example2: for i in `cd dot-ref ; ls *.dot`; do ../viscompare.sh $i; done
#
# $Id$
#

filea=`find . -name $1 | sed -n 1p`
fileb=`find . -name $1 | sed -n 2p`
diff $filea $fileb
echo left $filea - right $fileb
dot -Tpng $filea | pngtopnm >a.pnm
dot -Tpng $fileb | pngtopnm >b.pnm
pnmcat -lr a.pnm b.pnm | pnmtopng >comp.png

start comp.png		# Windows-specific

echo -n Press enter to finish
read dummy
rm -f a.pnm b.pnm comp.png
