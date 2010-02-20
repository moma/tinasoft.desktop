#/bin/bash

echo #############################################
echo # BUILD TINASOFT FOR 32 BIT LINUX PLATFORMS #
echo #############################################
echo ""

name="Tinasoft"
version="0.1"
arch="i686"
xulrunner="xulrunner-1.9.1"
xulrunnerdownfile="xulrunner-1.9.1.7.en-US.linux-i686.tar.bz2"
xulrunnerdownpath="http://mirrors.ircam.fr/pub/mozillla/xulrunner/releases/1.9.1.7/runtimes/"
outfile="$name-$version-$arch"
outpath="dist/$outfile"
pyxpcomextdownpath="http://downloads.mozdev.org/pyxpcomext"
pyxpcomextdownfile="pythonext-2.6.0.20090330-Linux_x86-gcc3.xpi"

if [ -e ".packaging/$arch/$xulrunner/xulrunner" ]
  then
    echo " - $xulrunner for $arch is already downloaded. great!"
  else
    echo " - downloading $xulrunner for $arch.."
    mkdir -p .packaging/$arch/$xulrunner
    wget $xulrunnerdownpath/$xulrunnerdownfile
    tar xjf $xulrunnerdownfile
    rm $xulrunnerdownfile
    mv xulrunner .packaging/$arch/$xulrunner/
fi

if [ -e ".packaging/$arch/$xulrunner/xulrunner/python" ]
  then
    echo " - pyxpcomext for $xulrunner, Python 2.6 and $arch is already downloaded! great!"
  else
    echo " - downloading pyxpcom for $xulrunner, Python 2.6 and $arch.."
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
cp install/skeletons/$arch/tina $outpath
cp -R .packaging/$arch/$xulrunner/ $outpath

echo " - creating release archive.."
tar -cf $outfile.tar.gz $outpath

# echo " - uploading to the tinasoft server.."
