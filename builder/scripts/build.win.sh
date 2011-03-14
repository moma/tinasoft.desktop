#!/usr/bin/env bash
echo "#############################################"
echo "# BUILD TINASOFT FOR WINDOWS 32BIT PLATFORMS #"
echo "#############################################"
echo ""
sleep 2
name="Tinasoft"
version="1.0beta"
arch="WIN32"
outfile="$name-$version-$arch"
outpath="dist/$outfile"
buildname="exe.win32-2.6"

echo " - creating or emptying $outpath"
sleep 2
if [ -e $outpath ]
  then
    rm -rf $outpath
fi
if [ ! -e dist ]
    then
        mkdir dist
fi
mkdir $outpath

echo " - copying freezed pytextminer from $buildname"
sleep 2
cp -Rf TinasoftPytextminer/build/$buildname $outpath/TinasoftPytextminer
#echo " - copying builder/Microsoft.VC90.CRT"
#sleep 2
#cp -Rf builder/Microsoft.VC90.CRT $outpath/TinasoftPytextminer

echo " - moving platform specific files to the $outpath"
sleep 2
cp builder/start_win.bat $outpath
cp -f TinasoftPytextminer/config_win.yaml $outpath

./builder/scripts/subscript.build.common.sh "$outpath"

echo " - creating the compressed archive..."
sleep 2
cd dist
rm -f $outfile.zip
zip -q -r $outfile.zip $outfile
cd ..
echo " - finished, archive is : $outfile.zip"
