#!/usr/bin/env bash

outpath=$1
buildname=$2
outfile=$3

echo " - creating or emptying $outpath"
sleep 2
if [ -e $outpath ]
  then
    rm -rf $outpath
fi
if [ ! -e dist ]
    then
        mkdir dist
fi
mkdir $outpath

echo " - freezing pytextminer"
sleep 2
cd TinasoftPytextminer
python2.6 freeze_linux.py build
cd ..
cp -Rf TinasoftPytextminer/build/$buildname $outpath/TinasoftPytextminer

echo " - moving platform specific files to the $outpath"
sleep 2
cp builder/start_unix.sh $outpath
cp -f TinasoftPytextminer/config_unix.yaml $outpath
./builder/scripts/subscript.build.common.sh "$outpath"

echo " - creating the compressed archive..."
sleep 2
cd dist
tar -cjf $outfile.tar.bz2 $outfile
cd ..
echo " - finished, archive is : $outfile.tar.bz2"
