#!/bin/bash

crdir=/tmp/$$_utls
mkdir $crdir
mkdir $crdir/utls
mkdir $crdir/utls/dist
THIS_SCRIPT=$(/usr/bin/readlink -nf "$0")
DIR=${THIS_SCRIPT%/*}
pushd $DIR

cp -r ../../targetsystem/opt/* $crdir
cp -r ../../dist/* $crdir/utls/dist

pushd $crdir
tar -czf utlsdist.tar.gz .
mv utlsdist.tar.gz $DIR/../target/
rm -r *
mv $DIR/../target/utlsdist.tar.gz .

pushd $DIR
cp -r ../../resources/activemq* $crdir
cp target.sh $crdir

pushd $crdir
rm $DIR/../target/*.gz
tar -czf $DIR/../target/utls-install.tar.gz .
popd
rm -r $crdir

cd ../..

echo command line create.sh
datestr=`date +%y%m%d`
timestr=`date +%H%M`
makeself install install_pkg/utls_"$datestr"_$timestr "User transaction logserver for Asterix" ./bin/install.sh --self

#popd
