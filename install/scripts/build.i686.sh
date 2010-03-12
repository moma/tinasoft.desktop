#/bin/bash

echo "#############################################"
echo "# BUILD TINASOFT FOR 32 BIT LINUX PLATFORMS #"
echo "#############################################"
echo ""

name="Tinasoft"
version="1.0-alpha"
arch="i686"
xulrunner="xulrunner-1.9.1"
xulrunnerdownfile="xulrunner-1.9.1.7.en-US.linux-i686.tar.bz2"
xulrunnerdownpath="http://mirrors.ircam.fr/pub/mozilla/xulrunner/releases/1.9.1.7/runtimes"
outfile="$name-$version-$arch"
outpath="dist/$outfile"
pyxpcomextdownpath="http://downloads.mozdev.org/pyxpcomext"
pyxpcomextdownfile="pythonext-2.6.0.20090330-Linux_x86-gcc3.xpi"

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
    mv xulrunner .packaging/$arch/$xulrunner/
fi

if [ -e ".packaging/$arch/$xulrunner/xulrunner/python" ]
  then
    echo " - pyxpcomext found"
  else
    echo " - pyxpcomext not found, downloading.."
    wget $pyxpcomextdownpath/$pyxpcomextdownfile
    echo " - installing pyxpcom inside $xulrunner download cache.."
    mkdir .tmp
    mv $pyxpcomextdownfile .tmp
    cd .tmp
    unzip $pyxpcomextdownfile
    rm  $pyxpcomextdownfile
    cd ..
    mv .tmp/python .packaging/$arch/$xulrunner/xulrunner/
    mv .tmp/pylib .packaging/$arch/$xulrunner/xulrunner/
    mv .tmp/components/* .packaging/$arch/$xulrunner/xulrunner/components/
    echo " - cleaning temporary download files.."
    rm -Rf .tmp
fi

echo " - copying xulrunner files to output distribution.."
cp -R tina $outpath
rm -Rf $outpath/xulrunner
rm $outpath/tina
rm $outpath/tina-stub
cp install/skeletons/$arch/tina $outpath
cp -R .packaging/$arch/$xulrunner/xulrunner $outpath
cp $outpath/xulrunner/xulrunner-stub $outpath/xulrunner
cp -R tests $outpath/tests
rm -Rf $outpath/db
rm -Rf $outpath/index

echo " - creating release archive.."
tar -cf $outfile.tar.gz $outpath

# echo " - uploading to the tinasoft server.."
