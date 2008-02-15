!/bin/bash
while :
do
	if xm list | grep $1 | grep migrating > /dev/null
        then
                ./chrono_launch
        fi
done
