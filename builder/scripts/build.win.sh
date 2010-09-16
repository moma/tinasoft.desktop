#/bin/bash

echo "#############################################"
echo "# BUILD TINASOFT FOR WINDOWS 32BIT PLATFORMS #"
echo "#############################################"
echo ""
sleep 2
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


echo " - moving platform specific files to 'dist/'"
sleep 2
cp builder/start_win.bat $outpath

if [ -e TinasoftPytextminer/build/exe.win32-2.6 ]
    then
        cp -Rf TinasoftPytextminer/build/exe.win32-2.6 $outpath/TinasoftPytextminer
fi
cp -Rf builder/Microsoft.VC90.CRT $outpath/TinasoftPytextminer

echo " - copying tinasoft desktop files to output..."
sleep 2
cp -Rf static $outpath
cp -f README $outpath
cp -f LICENSE $outpath
cp -f GNU-GPL.txt $outpath
cp -f $outpath/TinasoftPytextminer/config_win.yaml $outpath
#cp -Rf TinasoftPytextminer/shared $outpath
mv $outpath/TinasoftPytextminer/shared $outpath
mkdir $outpath/source_files
cp TinasoftPytextminer/source_files/tinacsv_test*.csv $outpath/source_files
#cp -Rf TinasoftPytextminer/source_files $outpath
cp -f TinasoftPytextminer/README $outpath/TinasoftPytextminer
cp -f TinasoftPytextminer/LICENSE $outpath/TinasoftPytextminer
cp -f TinasoftPytextminer/GNU-GPL.txt $outpath/TinasoftPytextminer
cp -f TinasoftPytextminer/user_stopwords.csv $outpath
cp -f builder/*.txt $outpath/TinasoftPytextminer

echo " - cleaning dist and creating the release compressed archive..."
sleep 2
find $outpath -name "*swp" -delete
find $outpath -name "*~" -delete
find $outpath -name "*swo" -delete
find $outpath -name "*.log" -delete
find $outpath/shared/nltk_data -name "*.zip" -delete
find $outpath/shared -name "*.cache" -delete
find $outpath/source_files -name "*.txt" -delete
cd dist
zip -q -r $outfile.zip $outfile
cd ..
echo " - created the archive : dist/$outfile.zip"
