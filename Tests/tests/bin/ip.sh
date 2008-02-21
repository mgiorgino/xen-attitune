IP=$(/sbin/ifconfig eth0 | grep -Eo 'adr:([0-9]{1,3}\.){3}[0-9]{1,3}' | cut -d: -f2)
echo ${IP}:8080;
