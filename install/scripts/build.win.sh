#/bin/bash

echo "#############################################"
echo "# BUILD TINASOFT FOR 32 BIT LINUX PLATFORMS #"
echo "#############################################"
echo ""

name="Tinasoft"
version="1.0alpha6"
arch="WIN32"

outfile="$name-$version-$arch"
outpath="dist/$outfile"


if [ -e $outpath ]
  then
    rm -rf $outpath
fi
mkdir dist
mkdir $outpath
echo " - copying tinasoft desktop files to output..."

cp -R static $outpath
cp -R examples $outpath
cp README $outpath
cp LICENSE $outpath
cp desktop_config_win.yaml $outpath
#### platform specific starter
cp install/start_win.bat $outpath



#### platform specific build directory
if [ -e TinasoftPytextminer/build/exe.win32-2.6 ]
	then
		cp -Rf TinasoftPytextminer/build/exe.win32-2.6 $outpath/TinasoftPytextminer
fi
cp -Rf TinasoftPytextminer/shared $outpath/TinasoftPytextminer
cp -Rf TinasoftPytextminer/source_files $outpath/TinasoftPytextminer
cp -Rf TinasoftPytextminer/README $outpath/TinasoftPytextminer
cp -Rf TinasoftPytextminer/LICENSE $outpath/TinasoftPytextminer
cp -Rf TinasoftPytextminer/user_stopwords.csv $outpath/TinasoftPytextminer


echo " - creating release archive..."
find $outpath -name *swp -delete
find $outpath -name *~ -delete
find $outpath -name *swo -delete

cd dist
zip -r $outfile.zip $outfile
cd ..
mv dist/$outfile.zip .