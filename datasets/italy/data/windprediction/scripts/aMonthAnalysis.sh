#!/bin/bash

SAVEIFS=$IFS;
IFS=$(echo -en "\n\b");
#IFS=$SAVEIFS

year="analysis";
#mkdir $year;
rm -fr $year/*;
rm -f ./windactual.csv
rm -f ./windprediction.csv

for i in `ls ./csv/prediction/*.csv`; do
	echo "Reading $i day: electricity prediction from Wind.";
	./scripts/predictedHourAggregation.sh "$i" >> windprediction.csv;
done;

for i in `ls ./csv/actual/*.csv`; do
	echo "Reading $i day: actual electricity generation from Wind.";
	./scripts/hourEnergyTypeAggregation.sh "$i" >> windactual.csv;
done;

sort windactual.csv > $year/windactual.csv;
sort windprediction.csv > $year/windprediction.csv;

#cp $year/grid.csv ~/git/central_system/env/trento/dc4es-service/
