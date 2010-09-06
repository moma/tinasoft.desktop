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
cp install/start_win.bat $outpath

if [ -e TinasoftPytextminer/build/exe.win32-2.6 ]
    then
        cp -Rf TinasoftPytextminer/build/exe.win32-2.6 $outpath/TinasoftPytextminer
fi
cp -Rf install/Microsoft.VC90.CRT $outpath/TinasoftPytextminer

echo " - copying tinasoft desktop files to output..."
sleep 2
cp -Rf static $outpath
cp -Rf examples $outpath
cp -f README $outpath
cp -f LICENSE $outpath
cp -f GNU-GPL.txt $outpath
cp -f desktop_config_win.yaml $outpath
cp -Rf TinasoftPytextminer/shared $outpath/TinasoftPytextminer
cp -Rf TinasoftPytextminer/source_files $outpath/TinasoftPytextminer
cp -f TinasoftPytextminer/README $outpath/TinasoftPytextminer
cp -f TinasoftPytextminer/LICENSE $outpath/TinasoftPytextminer
cp -f TinasoftPytextminer/GNU-GPL.txt $outpath/TinasoftPytextminer
cp -f TinasoftPytextminer/user_stopwords.csv $outpath/TinasoftPytextminer
cp -f install/*.txt $outpath/TinasoftPytextminer

echo " - cleaning dist and creating the release compressed archive..."
sleep 2
find $outpath -name "*swp" -delete
find $outpath -name "*~" -delete
find $outpath -name "*swo" -delete
find $outpath/TinasoftPytextminer/shared/nltk_data -name "*.zip" -delete
find $outpath/TinasoftPytextminer/source_files -name "*.txt" -delete
cd dist
zip -q -r $outfile.zip $outfile
cd ..
echo " - created the archive : dist/$outfile.zip"
