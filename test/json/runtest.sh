#!/bin/bash

echo $(javac -version)
echo $(java -version)
if [ -f ./JSON.jar ]; then
  javac -cp ./JSON.jar:. JsonTest.java
  java -cp ./JSON.jar:. JsonTest
  javac -cp ./JSON.jar JsonCastTest.java
  java -cp ./JSON.jar:. JsonCastTest
  javac -cp ./JSON.jar:. JsonObjectTest.java
  java -cp ./JSON.jar:. JsonObjectTest
else
  echo First make the ./JSON.jar file.
fi
echo
echo Completed. Press Enter to exit...
read