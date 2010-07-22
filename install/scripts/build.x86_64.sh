#/bin/bash

echo "#############################################"
echo "# BUILD TINASOFT FOR 64 BIT LINUX PLATFORMS #"
echo "#############################################"
echo ""

name="Tinasoft"

version="1.0alpha6"
arch="GNU_Linux_64"

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
cp install/start_unix.sh $outpath

echo " - freezing pytextminer"
cd TinasoftPytextminer
python freeze_linux.py build
cd ..
cp -Rf TinasoftPytextminer/build/exe.linux-x86_64-2.6 $outpath/TinasoftPytextminer

echo " - copying tinasoft desktop files to output..."
sleep 2
cp -R static $outpath
cp -R examples $outpath
cp README $outpath
cp LICENSE $outpath
cp GPL-LICENSE.txt $outpath
cp desktop_config_unix.yaml $outpath
cp -Rf TinasoftPytextminer/shared $outpath/TinasoftPytextminer
cp -Rf TinasoftPytextminer/source_files $outpath/TinasoftPytextminer
cp -Rf TinasoftPytextminer/README $outpath/TinasoftPytextminer
cp -Rf TinasoftPytextminer/LICENSE $outpath/TinasoftPytextminer
cp -Rf TinasoftPytextminer/user_stopwords.csv $outpath/TinasoftPytextminer
cp install/*.txt $outpath/TinasoftPytextminer

echo " - cleaning dist and creating the release compressed archive..."
sleep 2
find $outpath -name "*swp" -delete
find $outpath -name "*~" -delete
find $outpath -name "*swo" -delete
find $outpath/TinasoftPytextminer/shared/nltk_data -name "*.zip" -delete
cd dist
zip -q -r $outfile.zip $outfile
cd ..
echo " - created the archive : dist/$outfile.zip"
