#!/bin/bash

if [ $# != 2 ]
then
	echo "use : ping_pong <VM-name> <dest-address>";
	exit 1;
fi

while :
do
	if xm list | grep $1 | grep -v p > /dev/null
        then
                echo migrating VM $1 to node $2
                set -v
                xm migrate --live $1 $2
                set +v
        else
                echo VM $1 is not running here
        fi
done

