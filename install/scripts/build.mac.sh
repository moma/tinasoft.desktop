#/bin/bash
echo "############################################"
echo "# BUILD TINASOFT FOR SNOW LEOPARD PLATFORM #"
echo "############################################"
echo ""

name="Tinasoft"
version="1.0alpha6"
arch="macosx-10.6"

outpath="TinasoftPytextminer/dist/$name.app"
outpathres="$outpath/Contents/Resources"
outfile="$name-$version-$arch"

if [ -e TinasoftPytextminer/dist ]
  then
    rm -rf TinasoftPytextminer/dist
fi

if [ -e TinasoftPytextminer/build ]
  then
    rm -rf TinasoftPytextminer/build
fi

if [ -e $outfile.zip ]
  then
    rm -rf $outfile.zip
fi
if [ -e $outfile.dmg ]
  then  
    rm -rf $outfile.dmg
fi

echo " - freezing TinasoftPytextminer.."
cd TinasoftPytextminer
cp httpserver.py Tinasoft.py
python freeze_mac.py py2app
rm Tinasoft.py
cd ..

echo " - copying tinasoft.desktop files to $outpath.."
cp -r static $outpathres/static
cp -r examples $outpathres/examples
cp README $outpathres/README
cp LICENCE $outpathres/LICENCE
cp desktop_config_unix.yaml $outpathres
cp install/*.txt $outpathres
echo " - creating release archive.."

#find $outpath -name "*.swp" -delete
#find $outpath -name "*~" -delete
#find $outpath -name "*.swo" -delete
#find $outpathres -name "*.zip" -delete

zip -r $outfile.zip $outpath
hdiutil create $outfile.dmg -volname "$name $version" -fs HFS+ -srcfolder "$outpath"

