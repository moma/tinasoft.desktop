#/bin/bash

echo #############################################
echo # BUILD TINASOFT FOR 64 BIT LINUX PLATFORMS #
echo #############################################
echo ""

name="Tinasoft"
version="1.0alpha"
arch="Linux_x86_64"
xulrunner="xulrunner-1.9.1" 
#xulrunnerdownfile="xulrunner-1.9.1.7.en-US.linux-i686.tar.bz2"
#xulrunnerdownpath="http://mirrors.ircam.fr/pub/mozillla/xulrunner/releases/1.9.1.7/runtimes/"
outfile="$name-$version-$arch"
outpath="dist/$outfile"
#pyxpcomextdownpath="http://downloads.mozdev.org/pyxpcomext"
#pyxpcomextdownfile="pythonext-2.6.1.20090330-Linux_x86_64-gcc3.xpi"

if [ -e $outfile ]
  then
    rm $outfile
fi

echo " - copying xulrunner files to output distribution.."
cp -R tina $outpath
rm $outpath/tina
rm $outpath/tina-stub
rm -Rf $outpath/plugins
rm -Rf $outpath/db
rm -Rf $outpath/user
rm -Rf $outpath/index
rm -Rf $outpath/*.yaml
cp install/skeletons/$arch/* $outpath
cp -R install/data/* $outpath
cp -R tests $outpath/tests

echo " - creating release archive.."
tar -cf $outfile.tar.gz $outpath

# echo " - uploading to the tinasoft server.."
