#/bin/bash
echo "##########################################"
echo "# BUILD TINASOFT FOR LION (MAC OSX 10.x) #"
echo "##########################################"
echo ""

name="Tinasoft"
version="1.1.3"
arch="mac"

pytextminer="TinasoftPytextminer"
outpath="$pytextminer/dist/$name.app"
outpathres="$outpath/Contents/Resources"
outfile="$name-$version-$arch"

echo " - cleaning temporary dist and build directories..."
sleep 2
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
sleep 2
if [ -e $outfile.zip ]
  then
    rm -rf $outfile.zip
fi
if [ -e $outfile.dmg ]
  then
    rm -rf $outfile.dmg
fi

echo " - freezing $pytextminer with the py2app tool..."
sleep 2
cd $pytextminer
cp httpserver.py $name.py
python2.7 freeze_mac.py py2app
rm $name.py
cd dist/Tinasoft.app/Contents/Resources/lib/python2.7/
mkdir site-packages
unzip -q site-packages.zip -d site-packages/
rm site-packages.zip
mv numpy site-packages/numpy
zip -q -r site-packages.zip site-packages
#rm -r site-packages/ # do not remove it (or Numpy will not load..)
cd ../../../../../../../

echo " - moving platform specific files to the $outpathred"
cp $pytextminer/config_unix.yaml $outpathres
# special directory for common files
if [ ! -e $outpathres/TinasoftPytextminer ]
    then
        mkdir $outpathres/TinasoftPytextminer
fi
./builder/scripts/subscript.build.common.python2.7.sh "$outpathres"

echo " - creating a release archive and a DMG"
sleep 2
cd $pytextminer/dist
mkdir Tinasoft
mv $name.app Tinasoft/
cp ../../builder/Tinasoft.sh Tinasoft/
cp ../../builder/install.sh Tinasoft/
cp ../../builder/README-MAC.txt Tinasoft/
cp ../../builder/LISEZ-MOI-MAC.txt Tinasoft/
zip -q -r $outfile.zip Tinasoft
hdiutil create $outfile.dmg -volname "$name $version" -fs HFS+ -srcfolder "Tinasoft"
cd ../../
mv $pytextminer/dist/$outfile.dmg .
mv $pytextminer/dist/$outfile.zip .
