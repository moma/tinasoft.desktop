#!/bin/bash

echo "########################################"
echo "# BUILD TINASOFT FOR WINDOWS PLATFORMS #"
echo "########################################"
echo ""

name="Tinasoft"
version="1.0alpha6"
arch="WINNT_x86"
xulrunner="xulrunner-1.9.2"
platform="$arch-msvc"


#javaurl="http://dl.dropbox.com/u/122451/static/tina/java"
#javazip="java.zip"

xulrunnerdownfile="xulrunner-1.9.2.en-US.win32.zip"
xulrunnerdownpath="http://releases.mozilla.org/pub/mozilla.org/xulrunner/releases/1.9.2/runtimes/"

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


if [ -e ".packaging/$arch/$xulrunner/xulrunner" ]
  then
    echo " - tinasoft server found"
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



echo " - copying Tinasoft desktop xulrunner app"
cp -R tina $outpath
echo " - cleaning platform dependent files"
if [ -e $outpath/java ]
  then
    rm -Rf $outpath/java
    #cp -R .packaging/$arch/java $outpath
fi
if [ -e $outpath/plugins ]
  then
    rm -Rf $outpath/plugins
fi

echo " - copying platform dependent files"
cp install/skeletons/$arch/* $outpath

cp -R .packaging/$arch/$xulrunner/xulrunner $outpath
cp -R tests $outpath/tests

echo " - creating the package archive.."
find $outpath -name *~ -delete
find $outpath -name *swp -delete
find $outpath -name .* -delete
cd dist
zip -r $outfile.zip $outfile
cd ..
mv dist/$outfile.zip .
# echo " - uploading to the tinasoft server.."
