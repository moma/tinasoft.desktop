#!/usr/bin/env bash
echo "#############################################"
echo "# BUILD TINASOFT FOR 64 BIT LINUX PLATFORMS #"
echo "#############################################"
echo ""

name="Tinasoft"
version=$1
cpu=$2
arch="GNU_Linux_x86_64"
python=$3

buildname="exe.linux-x86_64-$python"
outfile="$name-$version-$arch"
outpath="dist/$outfile"

cd tinasoft

echo " - creating or emptying $outpath"
sleep 0
if [ -e $outpath ]
  then
    rm -rf $outpath
fi
if [ ! -e dist ]
    then
        mkdir dist
fi
mkdir $outpath

echo " - freezing pytextminer using $python"
sleep 0
cd TinasoftPytextminer
$python freeze_linux.py build
cd ..
cp -Rf TinasoftPytextminer/build/$buildname $outpath/TinasoftPytextminer

echo " - moving platform specific files to the $outpath"
sleep 0
cp ../devel/x86_64/installer/start_unix.sh $outpath
cp -f TinasoftPytextminer/config_unix.yaml $outpath

../devel/generic/common.sh "$outpath"

echo " - creating the compressed archive..."
sleep 0
cd dist
tar -cjf $outfile.tar.bz2 $outfile
cd ..
echo " - finished, archive is : $outfile.tar.bz2"
