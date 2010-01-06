echo "Generating Windows distribution.."
cp -R tina dist/tinasoft-win
rm -Rf dist/tinasoft-win/xulrunner
rm dist/tinasoft-win/tina # remove the linux executable
cp -R install/skeletons/windows/* dist/tinasoft-win/.
