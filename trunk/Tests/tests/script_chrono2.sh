#!/bin/bash

mig=0
start_sec=0
start_usec=0
end_sec=0
end_usec=0

while :
do
	if [ $mig -eq 0 ];
	then	
		if xm list | grep $1 | grep migrating > /dev/null
		then
			mig=1;
			echo debut;
			start_sec=`date +%s`;
			start_usec=`date +%N`;
   	fi
	else
		if xm list | grep $1 | grep -v p | grep -v migrating > /dev/null
		then
			mig=0;
			echo fin;
			end_sec=`date +%s`;
			end_usec=`date +%N`;
			sec=$(( $end_sec - $start_sec ));
			usec=$(( $end_usec - $start_usec ));
			echo diff : ${sec},${usec} s;
		fi
	fi
done
