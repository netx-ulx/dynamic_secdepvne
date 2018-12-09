#!/bin/bash

./cleanUpDVNE.sh

echo "Compiling..."

javac -d bin/ ./src/pt/SecDepVNE/Main/*.java ./src/pt/SecDepVNE/Charts/*.java ./src/pt/SecDepVNE/Glpk/*.java ./src/pt/SecDepVNE/Substrate/*.java ./src/pt/SecDepVNE/Common/*.java ./src/pt/SecDepVNE/Heuristic/*.java ./src/pt/SecDepVNE/Revenue/*.java ./src/pt/SecDepVNE/Virtual/*.java ./src/pt/SecDepVNE/Common/KShortestPath/model/abstracts/*.java ./src/pt/SecDepVNE/Common/ShortestPath/*.java

echo "Executing..."

#java -cp bin/ pt.SecDepVNE.Main.Main random 50 4000 3
#java -cp bin/ pt.SecDepVNE.Charts.DatCreator 50
java -cp bin/ pt.SecDepVNE.Main.Main random 50 4000 3