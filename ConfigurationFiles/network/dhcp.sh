sudo /etc/init.d/dhcp3-server stop

cp ./interfaces_files/interfaces_dhcp /etc/network/interfaces
/etc/init.d/networking restart
