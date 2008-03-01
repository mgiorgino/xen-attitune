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
			a=`./chrono2 | cut -d ' ' -f 2`;
   	fi
	else
		if xm list | grep $1 | grep -v p | grep -v migrating > /dev/null
        	then
			mig=0;
			echo fin;
			b=`./chrono2 | cut -d ' ' -f 2`;
			val=$(( $b - $a ));
			sec=$(( $val / 1000000 ));
			usec=$(( $val - $sec ));
			echo diff : $sec sec $usec usec;
        	fi
	fi
done
