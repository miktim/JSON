#!/bin/bash

if [ -f ../srcs/JSON.jar ]; then
  java -cp ../srcs/JSON.jar JSONTest.java
else
  echo First build the ../srcs/JSON.jar file.
fi