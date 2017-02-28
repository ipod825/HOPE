#!/bin/sh
find ./src  -name "*.java" > source.txt
javac @source.txt -cp "bin/*:lib/*:/opt/localsolver_6_5/bin/localsolver.jar" -d bin
rm source.txt
java -ea -cp "bin/:lib/*:/opt/localsolver_6_5/bin/localsolver.jar" hope.$1
