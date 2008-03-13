#!/bin/sh

case $# in
	1)	;;
	*)	echo "use : ping.sh < address >" ;
			exit 1;
			;;
esac

address=$1;

ping -w 1 -c 1 -i 0.001 $1 > a.txt;
do test;
done

debut=`./chrono2 | cut -d ' ' -f 2`;
echo "debut : $debut";

while !(ping -w 1 -c 1 -i 0.001 $1 > /dev/null);
do test;
done;

fin=`./chrono2 | cut -d ' ' -f 2`;
echo "fin : $fin";

val=$(( $b - $a ));
sec=$(( $val / 1000000 ));
usec=$(( $val - $sec ));

echo diff : $sec sec $usec usec;

