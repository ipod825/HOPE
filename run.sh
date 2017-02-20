#!/bin/sh

javac -sourcepath src -cp "bin/*:lib/*:/opt/localsolver_6_5/bin/localsolver.jar" -d bin src/*
java -cp "bin:lib/*:/opt/localsolver_6_5/bin/localsolver.jar" $1
