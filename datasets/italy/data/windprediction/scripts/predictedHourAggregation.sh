#!/bin/bash

#ZONA,ORA01,ORA02,ORA03,ORA04,ORA05,ORA06,ORA07,ORA08,ORA09,ORA10,ORA11,ORA12,ORA13,ORA14,ORA15,ORA16,ORA17,ORA18,ORA19,ORA20,ORA21,ORA22,ORA23,ORA24

SAVEIFS=$IFS;
IFS=$(echo -en "\n\b");

if [ A$1 == "A" ]; then
	echo "Need an input file"
	exit 0;
fi

MIX_ENERGY_DATA_FILE=$1
filename=`basename $1`;

index=`expr index "$filename" '20'`;
dat=${filename:index-1:8};
y=${dat:0:4}
m=${dat:4:2}
d=${dat:6:2}

#Filtering regions
awk -F, '{if(match($1,/CNOR|CSUD|NORD|SARD|SICI|SUD/)) print;}' $MIX_ENERGY_DATA_FILE > filtered_regions_to_match_actual_regions.csv;

#then Aggregate all regions electricity prediction by hour
awk -v y=$y -v m=$m -v d=$d -F, 'BEGIN{for(i=1;i<=24;i++) data_hours[i] = 0;} {for(i=1;i<=24;i++) data_hours[i] +=$(i+1);}END{for(i in data_hours) {ddate=mktime(y" "m" "d" "i" "00" "00); dd=strftime("%Y-%m-%dT%H:00:00", ddate); print dd, ",", data_hours[i];}}' filtered_regions_to_match_actual_regions.csv; # > output_hourly_prediction.csv;

exit 0
#$1:date, $2: generation
#dattt=`expr match $filename '20[:digit:]+'`;
#echo "date  $dattt";
