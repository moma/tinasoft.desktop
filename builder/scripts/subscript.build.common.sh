#!/usr/bin/env bash
outpath=$1

echo " - copying shared files to $outpath"
sleep 2
cp -Rf static $outpath
cp -Rf shared $outpath
mkdir $outpath/source_files
cp TinasoftPytextminer/source_files/tinacsv_test*.csv $outpath/source_files
cp TinasoftPytextminer/user_stopwords.csv $outpath

echo " - copying information texts to $outpath..."
sleep 2
cp README $outpath
cp LICENSE $outpath
cp GNU-GPL.txt $outpath
cp TinasoftPytextminer/README $outpath/TinasoftPytextminer
cp TinasoftPytextminer/LICENSE $outpath/TinasoftPytextminer
cp TinasoftPytextminer/GNU-GPL.txt $outpath/TinasoftPytextminer
cp builder/*.txt $outpath/TinasoftPytextminer

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
