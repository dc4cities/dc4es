#!/bin/bash

SAVEIFS=$IFS
IFS=$(echo -en "\n\b")
#sudo apt-get install gnumeric
mkdir csv$year;
rm -fr csv$year/*;

for i in `ls ./xls/*.xls`; do
	echo "Converting $i excell file to csv type.";
	j=`basename $i`;
	ssconvert --recalc --export-type=Gnumeric_stf:stf_csv "$i" "./csv$year/$j.csv";
	sed -i 1,2d "./csv$year/$j.csv";
done;

IFS=$SAVEIFS

#find . -name  \*.xls -type f -print0 | xargs -0 ssconvert --recalc --export-type=Gnumeric_stf:stf_csv './csv/aa.csv';
