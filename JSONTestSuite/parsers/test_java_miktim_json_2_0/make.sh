#!/bin/bash

javac -cp ".:json-2.0.0.jar" TestJSONParsing.java
jar cvfm TestJSONParsing.jar META-INF/MANIFEST.MF json-2.0.0.jar TestJSONParsing.class
java -jar ./TestJSONParsing.jar