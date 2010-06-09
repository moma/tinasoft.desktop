#!/bin/bash

echo "########################################"
echo "# BUILD TINASOFT FOR WINDOWS PLATFORMS #"
echo "########################################"
echo ""

name="Tinasoft"
version="1.0alpha4"
arch="WINNT_x86"
xulrunner="xulrunner-1.9.1"
platform="$arch-msvc"
javaurl="http://dl.dropbox.com/u/122451/static/tina/java"
javazip="java.zip"

xulrunnerdownfile="xulrunner-1.9.1.7.en-US.win32.zip"
xulrunnerdownpath="http://mirrors.ircam.fr/pub/mozilla/xulrunner/releases/1.9.1.7/runtimes"

outfile="$name-$version-$arch"
outpath="dist/$outfile"

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
    if [ -e $xulrunnerdownfile ]
      then
        echo " - seems to already be downloading, unpacking.."
      else
        wget $xulrunnerdownpath/$xulrunnerdownfile
    fi
    mkdir .tmp
    mv $xulrunnerdownfile .tmp
    cd .tmp
    unzip $xulrunnerdownfile
    rm $xulrunnerdownfile
    cd ..
    mv .tmp/xulrunner .packaging/$arch/$xulrunner/
    echo " - cleaning temporary download files.."
    rm -Rf .tmp
fi


echo " - moving files around to create the windows build, and cleaning distro files"

cp -R tina $outpath
rm -Rf $outpath/xulrunner
rm -Rf $outpath/platform
if [ -e $outpath/java ]
  then
    rm -Rf $outpath/java
    #cp -R .packaging/$arch/java $outpath
fi
if [ -e $outpath/plugins ]
  then
    rm -Rf $outpath/plugins
fi


rm -Rf $outpath/db
rm -Rf $outpath/log/*
rm -Rf $outpath/shared/gexf/gexf.template.*
find $outpath -name *~ -delete

if [ -e $outpath/user ]
  then
    rm -Rf $outpath/user
fi
if [ -e $outpath/index ]
  then
    rm -Rf $outpath/index
fi
rm -Rf $outpath/*.yaml
rm $outpath/tina
rm $outpath/tina-stub
cp install/skeletons/$arch/* $outpath
#cp -R install/data/* $outpath
mkdir $outpath/db
mkdir $outpath/index
mkdir $outpath/user
cp -R .packaging/$arch/$xulrunner/xulrunner $outpath
cp -R tests $outpath/tests

echo " - creating release archive.."
cd dist
zip -r $outfile.zip $outfile
cd ..
mv dist/$outfile.zip .
# echo " - uploading to the tinasoft server.."
