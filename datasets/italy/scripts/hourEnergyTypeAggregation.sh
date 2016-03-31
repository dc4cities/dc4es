#!/bin/bash

SAVEIFS=$IFS;
IFS=$(echo -en "\n\b");

if [ A$1 == "A" ]; then
	echo "Need an input file"
	exit 0;
fi
MIX_ENERGY_DATA_FILE=$1

#Filter Pumps Consumption and Photovoltaic Estimated, 
#awk -F, '{if(match($3,/Hydro|Wind|"Photovoltaic Measured"|Geotermal|"Hydro (River)"|Thermal/)) print;}' $MIX_ENERGY_DATA_FILE >  filter.csv
awk -F, '{if(match($3,/"Pumps Consumption"|"Photovoltaic Estimated"|Hydro|Wind|"Photovoltaic Measured"|Geotermal|"Hydro (River)"|Thermal/)) print;}' $MIX_ENERGY_DATA_FILE >  filter.csv;
#then Aggregate all regions electricity generation by date, and type
awk -F, '{a[$1","$3]+=$4;}END{for(i in a)print i,",",a[i];}' ./filter.csv > output_hourEnergyType.csv;
#awk -F, '{a[$1","$3]+=$4;}END{for (i in a)print i,",",a[i];}'  > output_dateType.csv
#$1:date, $2:type, $3: generation

#gawk -vOFMT=%15.7f -F, 'BEGIN{CO2COF=534.513;co2coefficient=0.410898038}{if(match($2,/Hydro|Wind|"Photovoltaic Measured"|Geotermal|"Hydro (River)"/)) el[$1",green"]+=$3; else el[$1",brown"]+=$3;}END{for(i in el) {split(i, e, ","); if(e[2] == "green") {totalP[e[1]]=el[i] + el[e[1]",brown"]; greenP[e[1]]=(100 * el[i])/(totalP[e[1]]);}} for(i in totalP) printf("%s,%d,%d,%d\n", i, totalP[i] * 1000 * 1000, greenP[i], el[i",brown"] * 1000 * co2coefficient);}' ./output_hourEnergyType.csv > output_greenPCO2.csv
#gawk -vOFMT=%15.7f -F, 'BEGIN{CO2COF=534.513;co2coefficient=0.410898038}{if(match($2,/Hydro|Wind|"Photovoltaic Measured"|Geotermal|"Hydro (River)"/)) el[$1",green"]+=$3; else el[$1",brown"]+=$3;}END{for(i in el) {split(i, e, ","); if(e[2] == "green") {totalP[e[1]]=el[i] + el[e[1]",brown"]; greenP[e[1]]=(100 * el[i])/(totalP[e[1]]);}} for(i in totalP) printf("%s,%d,%d,%d\n", i, totalP[i] * 1000 * 1000, greenP[i], el[i",brown"] * 1000 * co2coefficient);}' ./output_hourEnergyType.csv > output_greenPCO2.csv
#if(a[5] != "") {y=a[2];m=a[3];d=a[4];h=a[5];} else {y=a[1];m=a[2];d=a[3];h="00";} 

gawk -F, 'BEGIN{CO2COF=534.513;}{split($1, a, /|"|:| |\//); if(a[5] != "") {d=mktime(a[2]" "a[3]" "a[4]" "a[5]" "a[6]" "a[7]);} else {d=mktime(a[1]" "a[2]" "a[3]" "00" "00" "00);} dd=strftime("%Y-%m-%dT%H:00:00", d); if(match($2,/"Pumps Consumption"|"Photovoltaic Estimated"|Hydro|Wind|"Photovoltaic Measured"|Geotermal|"Hydro (River)"/)) {green[dd]+=$3;} else {brown[dd]+=$3;}}END{for(i in green){ totalP=green[i]+brown[i]; greenPP=(100* green[i]) / (totalP); printf("%s,%d,%d,%d\n", i, totalP * 1000 * 1000, greenPP, brown[i] * CO2COF / totalP);}}' ./output_hourEnergyType.csv;
# > output_greenPCO2.csv

#awk -F, '{if(match($2,/Hydro|Wind|"Photovoltaic Measured"|Geotermal|"Hydro (River)"/)) a[$1",green"]+=$3; else a[$1",brown"]+=$3;if(a[$1",green"] + a[$1",brown"] > 0) {b[$1]=(100 * a[$1",green"])/(a[$1",green"] + a[$1",brown"]);}}END{for (i in b) if(match(i, /green%/)) print i,",",b[i];}' output_dateType.csv > output_greenPerecentage.csv

#To test calculations: awk -F, '{if(match($2,/Hydro|Wind|"Photovoltaic Measured"|Geotermal|"Hydro (River)"/)) b[$1",green"]+=$3; else b[$1",brown"]+=$3;if(b[$1",green"] + b[$1",brown"] > 0) {b[$1",green%"]=b[$1",green"]/(b[$1",green"] + b[$1",brown"]);}}END{for (i in b) print i,",",b[i];}' output_dateType.csv > output_Verification.csv

#awk -F, '{a[$1",total"]+=$3;}END{for (i in a)print i,",",a[i];}' output_dateType.csv > output_total.csv
#"Photovoltaic Estimate
IFS=$SAVEIFS

#http://www.carbontrust.com/resources/guides/carbon-footprinting-and-reporting/conversion-factors
#In order to convert 'energy consumed in kWh' to 'kg of carbon dioxide equivalent', the energy use should be multiplied by a conversion factor.
#Carbon emissions are usually quoted in kgCO2/kWh. If you wish to convert the carbon dioxide factors into carbon (ie kgC/kWh), multiply the figure by 12 and divide by 44.
#litres petrol = 200 x 2.331 = 466.2 kgCO2e

#Electricity-specific factors (kgCO 2 /kWh): 0.410898038
#Emissions per kWh of electricity generated: 0.410898038
#Emissions associated with T&D losses per kWh of electricity consumed:  0.024367877

#Our data files from terna for Italian grid info:
#Ex-post data of actual generation 
#Generation unit:MWh,
#Electricity sources: Hydro|Wind|"Photovoltaic Measured"|Geotermal|"Hydro (River)"|Thermal, we excluded Pumps Consumption and Photovoltaic Estimated data

#x MWh = x * 1000 KWh 
