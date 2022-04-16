#!/bin/bash

echo $(javac -version)
jname=JSON
cpath=/org/miktim/json/
if [ ! -d ${cpath} ]
  then mkdir -p .${cpath}
  else rm -f ${cpath}/*.*
fi
#javac -Xstdout ./compile.log -Xlint:unchecked -cp .${cpath} -d ./ \
javac -Xstdout ./compile.log -Xlint:deprecation -cp .${cpath} -d ./ \
  ../../src${cpath}JSON.java  ../../src${cpath}JSONAdapter.java ../../src${cpath}JSONObject.java
if [ $? -eq 0 ] ; then
  jar cvf ./${jname}.jar .${cpath}*.class
#  javadoc -d ./${jname}Doc -nodeprecated -use package-info.java \
#  JSON.java
fi
rm -f -r ./org
#more < ./compile.log
cat compile.log
echo
echo Completed. Press Enter to exit...
read