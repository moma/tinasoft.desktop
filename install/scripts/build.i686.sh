#/bin/bash

echo "#############################################"
echo "# BUILD TINASOFT FOR 32 BIT LINUX PLATFORMS #"
echo "#############################################"
echo ""

name="Tinasoft"
version="1.0alpha4"
arch="Linux_x86"
platform="$arch-gcc3"
xulrunner="xulrunner-1.9.1"
xulrunnerdownfile="xulrunner-1.9.1.7.en-US.linux-i686.tar.bz2"
xulrunnerdownpath="http://dl.dropbox.com/u/122451/static/tina/xulrunner"
outfile="$name-$version-$arch"
outpath="dist/$outfile"

javaurl="http://dl.dropbox.com/u/122451/static/tina/java"
javazip="java-x86.tar.gz"

if [ -e $outfile ]
  then
    rm $outfile
fi

if [ -e ".packaging/$arch/$xulrunner/xulrunner" ]
  then
    echo " - xulrunner found"
  else
    echo " - xulrunner not found, downloading.."
    mkdir -p .packaging/$arch/$xulrunner
    wget $xulrunnerdownpath/$xulrunnerdownfile
    tar xjf $xulrunnerdownfile
    rm $xulrunnerdownfile
    mv xulrunner-1.9.1.7 .packaging/$arch/$xulrunner/xulrunner
fi


if [ -e ".packaging/$arch/java" ]
  then
    echo " - platform-specific libraries found"
  else
    echo " - platform-specific libraries not found, downloading.."
    wget $javaurl/$javazip
    tar xf $javazip
    chmod +x java/bin/*
    mv java .packaging/$arch
    rm $javazip
fi

echo " - copying xulrunner files to output distribution.."

cp -R tina $outpath
rm -Rf $outpath/db
rm -Rf $outpath/platform
rm -Rf $outpath/java
rm -Rf $outpath/log/*
rm -Rf $outpath/shared/gexf/gexf.template.*
rm -Rf $outpath/.autoreg
find $outpath -name *~ -delete

rm $outpath/tina
rm $outpath/tina-stub
rm $outpath/plugins/*
rm -Rf $outpath/xulrunner
rm -Rf $outpath/index
rm -Rf $outpath/*.yaml
cp install/skeletons/$arch/* $outpath
#cp -R install/data/* $outpath
cp -R tests $outpath/tests
cp -R .packaging/$arch/java $outpath
cp -R .packaging/$arch/$xulrunner/xulrunner $outpath
cp $outpath/xulrunner/xulrunner-stub $outpath/tina-stub
echo " - creating release archive.."
cd dist
tar -cjf $outfile.tar.bz2 $outfile
cd ..
mv dist/$outfile.tar.bz2 .



# echo " - uploading to the tinasoft server.."
