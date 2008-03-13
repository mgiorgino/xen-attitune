#!/bin/sh

server=$1
delay=$2
size=$3

cd RMIPing

java -Xms16m -Xmx512m -Djava.security.policy=security.policy -jar RMIPing.jar $server $delay $size

cd ..
