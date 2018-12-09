#!/bin/bash

echo "Removing files..."

for (( i = 1; i < 91; i++ )); do
	#statements
	rm -f ./glpk/datFiles/Dynamic/experience${i}/*
	rm -f ./glpk/datFiles/Heuristic/experience${i}/*
	rm -f ./glpk/outputFiles/Dynamic/experience${i}/*
	rm -f ./glpk/outputFiles/Heuristic/experience${i}/*
	rm -f ./statistics/Dynamic/experience${i}/*
	rm -f ./statistics/Heuristic/experience${i}/*
	rm -f ./utils/experienceData/*
done