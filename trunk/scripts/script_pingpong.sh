!/bin/bash
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

