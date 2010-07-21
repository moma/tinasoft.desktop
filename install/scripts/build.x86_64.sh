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
echo " - copying tinasoft desktop files to output..."

cp -R static $outpath
cp -R examples $outpath
cp README $outpath
cp LICENSE $outpath
cp desktop_config_unix.yaml $outpath
cp start_unix.sh $outpath

echo " - freezing pytextminer..."
cd TinasoftPytextminer
python freeze_linux.py build
cd ..
chmod -R 775 TinasoftPytextminer/build/
cp -Rf TinasoftPytextminer/build/exe.linux-x86_64-2.6 $outpath/TinasoftPytextminer

echo " - creating release archive..."
find $outpath -name *swp -delete
find $outpath -name *~ -delete
find $outpath -name *swo -delete

cd dist
tar -cjf $outfile.tar.bz2 $outfile
cd ..
mv dist/$outfile.tar.bz2 .
