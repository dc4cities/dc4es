#!/bin/bash

SAVEIFS=$IFS;
IFS=$(echo -en "\n\b");

if [ A$1 == "A" ]; then
	echo "Need an input file"
	exit 0;
fi
MIX_ENERGY_DATA_FILE=$1

#Filter Wind
awk -F, '{if(match($3,/Wind/)) print;}' $MIX_ENERGY_DATA_FILE > filter_wind.csv;

#then Aggregate all regions electricity generation by hour
awk -F, '{data[$1]+=$4;}END{for(i in data) { split(i, a, /|"|:| |\//); if(a[5] != "") {d=mktime(a[2]" "a[3]" "a[4]" "a[5]" "a[6]" "a[7]);} else {d=mktime(a[1]" "a[2]" "a[3]" "00" "00" "00);} dd=strftime("%Y-%m-%dT%H:00:00", d);print dd,",",data[i];}}' ./filter.csv # > output_hourEnergyType.csv;

#$1:date, $2: generation
IFS=$SAVEIFS
