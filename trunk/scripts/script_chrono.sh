!/bin/bash
while :
do
	if xm list | grep $1 | grep -v p > /dev/null
        then
                ./chrono_launch
        fi
done
