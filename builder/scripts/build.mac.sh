#/bin/bash
echo "############################################"
echo "# BUILD TINASOFT FOR SNOW LEOPARD PLATFORM #"
echo "############################################"
echo ""

name="Tinasoft"
version="1.0alpha7"
arch="macosx-10.6"

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
python freeze_mac.py py2app
rm $name.py
cd ..

echo " - moving platform specific files to the $outpathred"
cp $pytextminer/config_unix.yaml $outpathres
# special directory for common files
if [ ! -e $outpathres/TinasoftPytextminer ]
    then
        mkdir $outpathres/TinasoftPytextminer
fi
./builder/scripts/subscript.build.common.sh "$outpathres"

echo " - creating a release archive and a DMG"
sleep 2
zip -q -r $outfile.zip $pytextminer/dist/$name.app
hdiutil create $outfile.dmg -volname "$name $version" -fs HFS+ -srcfolder "$outpath"

