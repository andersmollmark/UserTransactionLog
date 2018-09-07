#!/bin/bash

function initInstallation() {
    if [ ! -d /var/log/app_install ] ; then
        mkdir /var/log/app_install
    fi
    echo 99 > /var/log/app_install/exitvalue
    echo "The installation of User Transaction Server was unexpected terminated" > /var/log/app_install/exitmsg
}

function failInstallation() {
    echo $1 > /var/log/app_install/exitvalue
    echo $2 > /var/log/app_install/exitmsg
    echo $1 - $2
    exit 1
}

function endInstallation() {
    echo 0 > /var/log/app_install/exitvalue
    echo "The installation of User Transaction Server succeeded" > /var/log/app_install/exitmsg
    echo 0 - The installation of UTL server succeeded
}


function addService ()
{
	if [ "`rc-update show | grep $1 | grep amsserver`" == "" ] ; then
		rc-update add $1 amsserver
	fi ;
}

function addServiceVms ()
{
	if [ "`rc-update show vms2 | grep $1 | grep vms2`" == "" ] ; then
		rc-update add $1 vms2
	fi ;
}

build=$1

initInstallation

echo "Installing ver $build of utls"
/usr/bin/logger -t install Installing ver $build of utls

echo "changing rights in /tmp"
/usr/bin/logger  -p user.debug -t install Changing rights in /tmp
chown vms:vms /tmp
chmod a+w /tmp

if [ -e /opt/utls/dist ] ; then
    rm -r /opt/utls/dist
fi

if [ ! -e /opt/utls ] ; then
    mkdir /opt/utls
    chown vms:vms /opt/utls
fi

if [ ! -e /opt/utls/dist ] ; then
    mkdir /opt/utls/dist
    chown vms:vms /opt/utls/dist
fi

if [ ! -e /etc/runlevels/amsserver ] ; then
    mkdir /etc/runlevels/amsserver
fi

if [ ! -e /var/log/amsserver ] ; then
    mkdir /var/log/amsserver
    chown vms:vms /var/log/amsserver
fi

echo "untaring utlsdist"
/usr/bin/logger  -p user.debug -t install Untaring utlsdist
tar -xzf /tmp/utlsdist.tar.gz -C /opt
echo "untaring activemq"
/usr/bin/logger  -p user.debug -t install Untaring activemq
tar -xzf /tmp/activemq-bin.tar.gz -C /opt/utls
    
if [ ! -h /etc/init.d/publishUTLServer ] ; then
    ln -s /opt/utls/script/init.d/publishUTLServer /etc/init.d/publishUTLServer
fi
    
if [ ! -h /etc/init.d/utlServer ] ; then
    ln -s /opt/utls/script/init.d/utlServer /etc/init.d/utlServer
fi
    
if [ ! -h /etc/init.d/activemq ] ; then
    ln -s /opt/utls/script/init.d/activemq /etc/init.d/activemq
fi

# Removed by BE, the directory /opt/utls/lib does not exist ??
#if [ ! -h /opt/utls/bin/lib ] ; then
#	ln -s /opt/utls/lib /opt/utls/bin/lib
#fi

if [ ! -h /var/lib/mysql ] ; then
    echo "linking mysql"
    /usr/bin/logger  -p user.debug -t install Linking mysql
    ln -s /var/log/mysqlDb /var/lib/mysql
fi

chown -R vms:vms /opt/utls

cp /opt/utls/mysql/mysqlaccess.conf /etc/mysql/mysqlaccess.conf
chown root:root /etc/mysql/mysqlaccess.conf
chmod 644 /etc/mysql/mysqlaccess.conf
chmod +x /opt/utls/dist/UserTransactionLogServer.jar
#chmod g+x,u+x /opt/utls/script/init.d/publishUTLServer
#chmod g+x,u+x /opt/utls/script/init.d/utlServer
#chmod g+x,u+x /opt/utls/script/init.d/activemq

echo "Stop services"
/usr/bin/logger  -p user.debug -t install Stop services
/etc/init.d/mysql stop >/dev/null 2>&1
/etc/init.d/activemq stop >/dev/null 2>&1
/etc/init.d/publishUTLServer stop >/dev/null 2>&1
/etc/init.d/utlServer stop >/dev/null 2>&1

echo "setting jdk-version"
/usr/bin/logger  -p user.debug -t install Setting jdk-version
eselect java-vm set system oracle-jdk-bin-1.8
    
echo "Add services to runlevel amsserver"
/usr/bin/logger  -p user.debug -t install Add services to runlevel amsserver
addService vixie-cron
addService net.net0
addService syslog-ng
addService local
addService dnsmasq
addService xinetd
addService sshd
addService pure-ftpd
addService atd
addService dbus
addService avahi-daemon
addService ntpd
if [ ! -e /etc/runlevels/amsserver/servicecluster ] ; then
    addService utlServer
    addService publishUTLServer
    addService activemq
fi
addService mysql
rc-update -u

#echo starting new daemons >>/var/log/utls/utlsinstall.log
    
echo "Starting services"
/usr/bin/logger  -p user.debug -t install Starting services
/etc/init.d/net.net0 start
/etc/init.d/syslog-ng start
/etc/init.d/dnsmasq start
/etc/init.d/pure-ftpd start
/etc/init.d/sshd start
/etc/init.d/vixie-cron start
/etc/init.d/xinetd start
/etc/init.d/mysql start
if [ ! -e /etc/runlevels/amsserver/servicecluster ] ; then
    /etc/init.d/aaserver start
    /etc/init.d/publishAAServer start
    /etc/init.d/vmscontroller start
fi

echo "creating db..."
/usr/bin/logger  -p user.debug -t install Creating db...
mysql --user=root --password=delavalpwd -e "CREATE DATABASE IF NOT EXISTS user_transaction_log_server"
#echo "granting super..."
#mysql --user=root --password=delavalpwd -e "GRANT SUPER ON *.* TO logAdmin@localhost IDENTIFIED BY 'admin'"


if [ ! -e /etc/runlevels/amsserver/servicecluster ] ; then
    /etc/init.d/activemq start
    /etc/init.d/utlServer start
    /etc/init.d/publishUTLServer start
fi
/etc/init.d/local start

endInstallation
/usr/bin/logger -t install Installation ver $build of UTLS finished!
