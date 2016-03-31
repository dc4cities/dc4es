#!/bin/bash

#This script reads all csv files from ./csv directory, and pass them one by one to dateTypeAggregation.sh script to cacluate green percentage and CO2 emissions from energy mix generation data of Italian grid.

#If you would like to do calculations for only one csv file, you can simply pass that file to dateTypeAggregation.sh, and then run dc4citiesGridFormat.sh

SAVEIFS=$IFS;
IFS=$(echo -en "\n\b");

year="grid2013";
mkdir $year;
rm -fr $year/*;
rm -f ./gridt.csv

for i in `ls ./csv2013/*.csv`; do
	echo "Reading $i day: electricity generation mix.";
	j=`basename $i`;
	./hourEnergyTypeAggregation.sh "$i" >> gridt.csv;
	#./dc4citiesGridFormat.sh;
done;

sort gridt.csv > $year/grid.csv;

#cp $year/grid.csv ~/git/central_system/env/trento/dc4es-service/

IFS=$SAVEIFS
