#!/bin/bash

echo $(javac -version)
echo $(java -version)
if [ -f ./JSON.jar ]; then
  javac -cp ./JSON.jar JSONTest.java
  java -cp ./JSON.jar:. JSONTest
  javac -cp ./JSON.jar JSONAdapterTest.java
  java -cp ./JSON.jar:. JSONAdapterTest
  javac -cp ./JSON.jar -d . A.java J.java
  javac -cp ./JSON.jar:. JSONObjectTest.java
  java -cp ./JSON.jar:. JSONObjectTest
  rm -f *.class ./json/*.class 
  rmdir ./json
else
  echo First make the ./JSON.jar file.
fi
echo
echo Completed. Press Enter to exit...
read