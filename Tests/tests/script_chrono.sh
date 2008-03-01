#!/bin/bash
mig=0
while :
do
	if [ $mig -eq 0 ];
	then	
		if xm list | grep $1 | grep migrating > /dev/null
        	then
			mig=1;
			echo debut;
                	./chrono_launch;
        	fi
	else
		if xm list | grep $1 | grep -v p | grep -v migrating > /dev/null
        	then
			mig=0;
			echo fin;
                	./chrono_launch;
        	fi
	fi
done
