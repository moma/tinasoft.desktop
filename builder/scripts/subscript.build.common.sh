#!/usr/bin/env bash
outpath=$1

echo " - copying shared files to $outpath"
sleep 2
cp -Rf static $outpath
cp -Rf shared $outpath
mkdir $outpath/source_files
cp TinasoftPytextminer/source_files/tinacsv_test*.csv $outpath/source_files
cp -f user_stopwords.csv $outpath

echo " - copying information texts to output..."
sleep 2
cp -f README $outpath
cp -f LICENSE $outpath
cp -f GNU-GPL.txt $outpath
cp -f TinasoftPytextminer/README $outpath/TinasoftPytextminer
cp -f TinasoftPytextminer/LICENSE $outpath/TinasoftPytextminer
cp -f TinasoftPytextminer/GNU-GPL.txt $outpath/TinasoftPytextminer
cp -f builder/*.txt $outpath/TinasoftPytextminer


echo " - cleaning some files..."
sleep 2
find $outpath -name "*swp" -delete
find $outpath -name "*~" -delete
find $outpath -name "*swo" -delete
find $outpath -name "*.log" -delete
find $outpath/shared/nltk_data -name "*.zip" -delete
find $outpath/shared -name "*.cache" -delete
#find $outpath/shared -name "*.pickle" -delete
find $outpath/source_files -name "*.txt" -delete
