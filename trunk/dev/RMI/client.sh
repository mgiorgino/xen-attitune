#!/bin/sh

server=$1
size=$2

cd RMIClient

java -Xms16m -Xmx512m -Djava.security.policy=security.policy -jar RMIClient.jar $server $size

cd ..
