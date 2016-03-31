#!/bin/bash

#An entry 2014-01-01T00:00:00;450000;46(green%);140(CO2emissions) in
#git/central_system/env/trento/dc4es-service/grid.csv

gawk -F, '{split($1, a, /|"|:| |\//); if(a[5] != "") {d=mktime(a[2]" "a[3]" "a[4]" "a[5]" "a[6]" "a[7]);} else {d=mktime(a[1]" "a[2]" "a[3]" "00" "00" "00);} dd=strftime("%Y-%m-%dT%H:00:00", d);print dd";"$2";"$3";"$4;}' ./output_greenPCO2.csv >> gridt.csv;
