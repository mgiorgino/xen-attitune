cp ~adminxen/network/interfaces_files/interfaces_static_147 /etc/network/interfaces
/etc/init.d/networking restart

cp ~adminxen/network/dhcpdconf_files/dhcpd147.conf /etc/dhcp3/dhcpd.conf
/etc/init.d/dhcp3-server restart
