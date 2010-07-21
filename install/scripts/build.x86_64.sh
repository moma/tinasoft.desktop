#/bin/bash

echo "#############################################"
echo "# BUILD TINASOFT FOR 64 BIT LINUX PLATFORMS #"
echo "#############################################"
echo ""

name="Tinasoft"
version="1.0alpha4"
arch="Linux_x86"
outfile="$name-$version-$arch"
outpath="dist/$outfile"

if [ -e $outfile ]
  then
    rm $outfile
fi

cp -R static/ $outpath
cd TinasoftPytextminer
python freeze_linux.py build
cp -R build
find $outpath -name *~ -delete

#rm $outpath/tina
#rm $outpath/tina-stub
#rm -Rf $outpath/plugins
#rm -Rf $outpath/db
#rm -Rf $outpath/user
#rm -Rf $outpath/index
#rm -Rf $outpath/*.yaml
#cp install/skeletons/$arch/* $outpath
#cp -R install/data/* $outpath
#cp -R tests $outpath/tests

echo " - creating release archive.."
cd dist
tar -cjf $outfile.tar.bz2 $outfile
cd ..
mv dist/$outfile.tar.bz2 .

# echo " - uploading to the tinasoft server.."
