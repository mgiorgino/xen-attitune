#!/bin/sh

resultFileName="result.txt"

while true
do
	echo $i >> $resultFileName;
	i=$(($i+1));
	./pause $pause;
done
