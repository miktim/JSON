#!/bin/bash

echo $(javac -version)
echo $(java -version)
if [ -f ../srcs/JSON.jar ]; then
#  java -cp ../srcs/JSON.jar JSONTest.java
  javac -cp ../srcs/JSON.jar JSONTest.java
  java -cp ../srcs/JSON.jar:. JSONTest
  rm -f *.class 
else
  echo First make the ../srcs/JSON.jar file.
fi
