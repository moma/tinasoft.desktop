#/bin/bash
echo "############################################"
echo "# BUILD TINASOFT FOR SNOW LEOPARD PLATFORM #"
echo "############################################"
echo ""

name="Tinasoft"
version="1.0alpha6"
arch="macosx-10.6"

pytextminer="TinasoftPytextminer"
outpath="$pytextminer/dist/$name.app"
outpathres="$outpath/Contents/Resources"
outfile="$name-$version-$arch"

if [ -e $pytextminer/dist ]
  then
    rm -rf $pytextminer/dist
fi

if [ -e $pytextminer/build ]
  then
    rm -rf $pytextminer/build
fi

if [ -e $outfile.zip ]
  then
    rm -rf $outfile.zip
fi
if [ -e $outfile.dmg ]
  then
    rm -rf $outfile.dmg
fi

echo " - freezing $pytextminer.."
sleep 2
cd $pytextminer
cp httpserver.py $name.py
python freeze_mac.py py2app
rm $name.py
cd ..

echo " - copying tinasoft.desktop files to $outpath.."
sleep 2
cp -r static $outpathres/static
cp README $outpathres/README
cp LICENCE $outpathres/LICENCE
cp GNU-GPL.txt $outpathres
if [ ! -e $outpathres/$pytextminer ]
  then
    mkdir $outpathres/$pytextminer
fi
mv $outpathres/shared $outpathres
mv $outpathres/source_files $outpathres

cp desktop_config_unix.yaml $outpathres
cp builder/*.txt $outpathres
cp TinasoftPytextminer/user_stopwords.csv $outpathres

echo " - creating release archive.."
sleep 2

#find $outpath -name "*.swp" -delete
#find $outpath -name "*~" -delete
#find $outpath -name "*.swo" -delete
#find $outpathres -name "*.zip" -delete

zip -r $outfile.zip $outpath
hdiutil create $outfile.dmg -volname "$name $version" -fs HFS+ -srcfolder "$outpath"
