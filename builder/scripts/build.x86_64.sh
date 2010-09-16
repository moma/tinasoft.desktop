#/bin/bash

echo "#############################################"
echo "# BUILD TINASOFT FOR 64 BIT LINUX PLATFORMS #"
echo "#############################################"
echo ""
name="Tinasoft"

version="1.0alpha6"
arch="GNU_Linux_x86_64"
buildname="exe.linux-x86_64-2.6"
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
cp builder/start_unix.sh $outpath

echo " - freezing pytextminer"
cd TinasoftPytextminer
python freeze_linux.py build
cd ..
cp -Rf TinasoftPytextminer/build/$buildname $outpath/TinasoftPytextminer

echo " - copying tinasoft desktop files to output..."
sleep 2
cp -Rf static $outpath
cp -f README $outpath
cp -f LICENSE $outpath
cp -f GNU-GPL.txt $outpath
cp -f $outpath/TinasoftPytextminer/config_unix.yaml $outpath
#cp -Rf TinasoftPytextminer/shared $outpath
mv TinasoftPytextminer/shared $outpath
mkdir $outpath/source_files
cp TinasoftPytextminer/source_files/tinacsv_test*.csv $outpath/source_files
cp -f TinasoftPytextminer/README $outpath
cp -f TinasoftPytextminer/LICENSE $outpath
cp -f TinasoftPytextminer/GNU-GPL.txt $outpath
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
tar -cjf $outfile.tar.bz2 $outfile
cd ..
echo " - created the archive : $outfile.tar.bz2"
