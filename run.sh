#!/bin/bash

mvn -e exec:java -Dexec.classpathScope="runtime" -Dexec.mainClass="net.sourceforge.sql2java.Main" -Dexec.args="$1"
