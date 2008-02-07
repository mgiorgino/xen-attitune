cp ~adminxen/network/interfaces_files/interfaces_static_192 /etc/network/interfaces
/etc/init.d/networking restart

cp ~adminxen/network/dhcpdconf_files/dhcpd192.conf /etc/dhcp3/dhcpd.conf
/etc/init.d/dhcp3-server restart
