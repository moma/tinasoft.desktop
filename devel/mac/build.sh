#!/bin/bash
echo "##############################"
echo "# BUILD TINASOFT FOR MAC OSX #"
echo "##############################"
echo ""

name="Tinasoft"
version=$1
arch="mac"
python=$2

pytextminer="TinasoftPytextminer"
outpath="$pytextminer/dist/$name.app"
outpathres="$outpath/Contents/Resources"
outfile="$name-$version-$arch"


cd tinasoft

echo " - cleaning temporary dist and build directories..."
sleep 0
if [ -e $pytextminer/dist ]
  then
    rm -rf $pytextminer/dist
fi

if [ -e $pytextminer/build ]
  then
    rm -rf $pytextminer/build
fi

mkdir $pytextminer/build
mkdir $pytextminer/dist

echo " - removing older packages..."
sleep 0
if [ -e $outfile.zip ]
  then
    rm -rf $outfile.zip
fi
if [ -e $outfile.dmg ]
  then
    rm -rf $outfile.dmg
fi

echo " - freezing $pytextminer with the py2app tool..."
sleep 0
cd $pytextminer
cp httpserver.py $name.py
$python freeze_mac.py py2app
rm $name.py
cd dist/$name.app/Contents/Resources/lib/$python/ # hu
mkdir site-packages
unzip -q site-packages.zip -d site-packages/
rm site-packages.zip # HO..
mv numpy site-packages/numpy
zip -q -r site-packages.zip site-packages # ..LY..
#rm -r site-packages/ # do not remove it (or Numpy will not load..)
cd ../../../../../../../ # ..COW!

echo " - moving platform specific files to the $outpathres"
cp $pytextminer/config_mac.yaml $outpathres
# special directory for common files
mkdir -p $outpathres/TinasoftPytextminer
../devel/generic/build.sh "$outpathres" "$python"

echo " - creating a release archive and a DMG"
sleep 0
cd $pytextminer/dist
mkdir Tinasoft
mv $name.app Tinasoft/
installer="../devel/mac/installer"
cp $installer/Tinasoft.sh Tinasoft/
cp.$installer/install.sh Tinasoft/
cp $installer/README-MAC.txt Tinasoft/
cp $installer/LISEZ-MOI-MAC.txt Tinasoft/
zip -q -r $outfile.zip Tinasoft
hdiutil create $outfile.dmg -volname "$name $version" -fs HFS+ -srcfolder "Tinasoft"
cd ../../
mv $pytextminer/dist/$outfile.dmg ..
mv $pytextminer/dist/$outfile.zip ..
echo "cleaning.."
rm -Rf $pytextminer/dist/*
rm -Rf $pytextminer/build/*
cd ..
