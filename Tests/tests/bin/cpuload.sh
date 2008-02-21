#!/bin/sh

cpu=`cut -f 2 -d ' ' /proc/stat | head -n 2 | tail -n 1`
lastcpu=$cpu;

echo $cpu;
echo $lastcpu;

while true;
do
	cpu=`cut -f 2 -d ' ' /proc/stat | head -n 2 | tail -n 1`;
	echo $(($cpu - $lastcpu));
	lastcpu=$cpu;
done
