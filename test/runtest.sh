#!/bin/bash

echo $(javac -version)
echo $(java -version)
if [ -f ./JSON.jar ]; then
  javac -cp ./JSON.jar JSONTest.java
  java -cp ./JSON.jar:. JSONTest
  rm -f *.class 
else
  echo First make the ./JSON.jar file.
fi
echo
echo Completed. Press any key...
read