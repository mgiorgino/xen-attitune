#!/bin/sh

server=$1
delay=$2
timeout=$3

cd Ping

java -jar Ping.jar $server $delay $timeout

cd ..
