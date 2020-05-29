#!/bin/bash

echo $(javac --version)
jname=JSON
cpath=./org/samples/java
if [ ! -d ${cpath} ]
  then mkdir -p ${cpath}
  else rm -f ${cpath}/*.*
fi
javac -Xstdout ./compile.log -Xlint:unchecked -cp ${cpath} -d ./ \
  JSON.java
if [ $? -eq 0 ] ; then
  jar cvf ./${jname}.jar ${cpath}/*.class
#  javadoc -d ./${jname}Doc -nodeprecated -use package-info.java \
#  JSON.java
fi
rm -f -r ./org
more < ./compile.log