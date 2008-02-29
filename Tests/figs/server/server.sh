#!/bin/sh

cd RMIServer

CLASS_PATH=RMICommon.jar

IP=$(/sbin/ifconfig eth0 | grep -Eo 'dr:([0-9]{1,3}\.){3}[0-9]{1,3}' | cut -d: -f2)

a=`ps -ef | grep rmiregistry | grep -v grep` > /dev/null 2> /dev/null

if [ "$a" = "" ]; then
	rmiregistry -J-classpath -J$CLASS_PATH &
fi

java -Djava.security.policy=security.policy -Djava.rmi.server.hostname=${IP} -jar RMIServer.jar ${IP} &

cd ..

sleep 1
