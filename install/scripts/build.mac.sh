#/bin/bash

echo "############################################"
echo "# BUILD TINASOFT FOR SNOW LEOPARD PLATFORM #"
echo "############################################"
echo ""

name="Tinasoft"
version="1.0alpha6"
arch="macosx-10.6"

outpath="TinasoftPytextminer/dist/httpserver.app"
outpathres="$outpath/Resources"
outfile="$name-$version-$arch"

if [ -e $outpath ]
  then
    rm -rf $outpath
fi

echo " - freezing TinasoftPytextminer.."
cd TinasoftPytextminer
python freeze_mac.py py2app
cd ..

echo " - copying tinasoft.desktop files to $outpath.."
cp -r static $outpathres
cp -r examples $outpathres
cp README $outpath
cp LICENCE $outpath
cp desktop_config_unix.yaml $outpathres

echo " - creating release archive.."
#find $outpath -name *swp -delete
#find $outpath -name *~ -delete
#find $outpath -name *swo -delete

zip -r $outfile.zip $outpath
hdiutil create $outfile.dmg -volname "$name $version" -fs HFS+ -srcfolder "$outpath"

