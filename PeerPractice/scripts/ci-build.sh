#!/bin/sh
if [ -f "./mvnw" ]; then
  MVN="./mvnw"
else
  MVN="mvn"
fi
$MVN -B -Djavafx.platform=$1 test
exit $?