#!/bin/sh
#
# Update the reference test data to match what is actually produced
# This should only be executed after manually verifying that the
# differences are indeed intentional.
#
# Diomidis Spinellis. August 2008.
#
#

for i in dot umldoc
do
	git ls-files testdata/*-ref |
	sed "s/\(testdata\/$i-\)ref\(.*\)/\1ref\2 \1out\2/" |
	while read refname outname
	do
		if [ -r "$outname" -a -r "$outname" ] && \
			! cmp -s $refname $outname
		then
			cp $outname $refname
		fi
	done
done
