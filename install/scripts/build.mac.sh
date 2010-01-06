echo "mac"
cp -R tina dist/tinasoft-mac
rm -Rf dist/tinasoft-mac/xulrunner
rm dist/tinasoft-mac/tina # remove the linux executable
cp -R install/skeletons/mac/* dist/tinasoft-mac/.
