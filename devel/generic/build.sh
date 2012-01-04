#!/usr/bin/env bash
outpath=$1
python=$2
echo " - copying shared files to $outpath"
sleep 0
cp -Rf static $outpath
cp -Rf shared $outpath
mkdir $outpath/source_files
cp TinasoftPytextminer/source_files/tinacsv_test_3.csv $outpath/source_files
cp TinasoftPytextminer/user_stopwords.csv $outpath

echo " - copying information texts to $outpath..."
sleep 0
cp README $outpath
cp LICENSE $outpath
cp TinasoftPytextminer/README $outpath/TinasoftPytextminer/
cp TinasoftPytextminer/LICENSE $outpath/TinasoftPytextminer/
cp ../devel/generic/txt/*.txt $outpath/TinasoftPytextminer

echo " - cleaning some files..."
sleep 0
find $outpath -name "*swp" -delete
find $outpath -name "*~" -delete
find $outpath -name "*swo" -delete
find $outpath -name "*.gexf" -delete
find $outpath -name "*.gitignore" -delete
find $outpath -name "*-log.txt*" -delete
find $outpath/shared/nltk_data -name "*.zip" -delete
find $outpath/shared -name "*.cache" -delete
rm -rf $outpath/.git/
rm -rf $outpath/static/tinaweb/.git/
rm -rf $outpath/TinasoftPytextminer/.git/
