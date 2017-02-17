#!/bin/bash



TEMP=`getopt -o se --long self,embedded -n 'install.sh' -- "$@"`

if [ $? != 0 ] ; then echo "Terminating..." >&2 ; exit 1 ; fi

# Note the quotes around `$TEMP': they are essential!
eval set -- "$TEMP"
embedded=false
addr="self"

while true ; do
	case "$1" in
        -e|--embedded) embedded=true
		shift 1 ;;

        -s|--self)
        addr="self"
		shift 1 ;;

		--)
        shift ; break ;;

        *) 
        break ;;
	esac
done

if [ "$addr" == "self" ] ; then
    if [ "$1" == "" ] ; then
        addr=localhost
    else
        addr=$1
    fi
fi

THIS_SCRIPT=$(/usr/bin/readlink -nf "$0")
DIR=${THIS_SCRIPT%/*}
pushd $DIR

if [ "$addr" == "" ] ; then
    echo Input Ip-address
    read addr
fi

echo Installing $addr
chmod 600 ../key/sparedisk-3.0.key
ssh -o StrictHostKeyChecking=no -i ../key/sparedisk-3.0.key install@$addr 'sudo chmod a+w /tmp/; sudo rm -rf /tmp/*utls*'
scp -o StrictHostKeyChecking=no -i ../key/sparedisk-3.0.key ../target/utls-install.tar.gz install@$addr:/tmp/
if [ $embedded = true ] ; then
    ssh -o StrictHostKeyChecking=no -i ../key/sparedisk-3.0.key install@$addr "sudo /bin/tar -xzf /tmp/utls-install.tar.gz -C /tmp;sudo /bin/bash /tmp/target.sh -e"
else
    ssh -o StrictHostKeyChecking=no -i ../key/sparedisk-3.0.key install@$addr "sudo /bin/tar -xzf /tmp/utls-install.tar.gz -C /tmp;sudo /bin/bash /tmp/target.sh"
fi
popd


