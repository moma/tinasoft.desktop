#/bin/bash

echo "#############################################"
echo "# BUILD TINASOFT FOR 32 BIT LINUX PLATFORMS #"
echo "#############################################"
echo ""

name="Tinasoft"
version="1.0alpha6"
arch="GNU_Linux_32"
buildname="exe.linux-i686-2.6"
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
cp desktop_config_unix.yaml $outpath
#### platform specific starter
cp install/start_unix.sh $outpath

echo " - freezing pytextminer..."
cd TinasoftPytextminer
#### platform specific freezeer
python-2.6 freeze_linux.py build
cd ..
chmod -R 775 TinasoftPytextminer/build/
#### platform specific build directory
cp -Rf TinasoftPytextminer/build/$buildname $outpath/TinasoftPytextminer
cp -Rf TinasoftPytextminer/shared $outpath/TinasoftPytextminer
cp -Rf TinasoftPytextminer/source_files $outpath/TinasoftPytextminer
cp -Rf TinasoftPytextminer/README $outpath/TinasoftPytextminer
cp -Rf TinasoftPytextminer/LICENSE $outpath/TinasoftPytextminer
cp -Rf TinasoftPytextminer/user_stopwords.csv $outpath/TinasoftPytextminer
cp install/*txt $outpath/TinasoftPytextminer

echo " - creating release archive..."
find $outpath -name "*swp" -delete
find $outpath -name "*~" -delete
find $outpath -name "*swo" -delete
find $outpath/TinasoftPytextminer/shared/nltk_data -name "*.zip" -delete

cd dist
tar -cjf $outfile.tar.bz2 $outfile
cd ..
mv dist/$outfile.tar.bz2 .
