echo "Generating Linux 32 bit (i686) distribution.."
cp -R tina dist/tinasoft-i686
rm -Rf dist/tinasoft-i686/xulrunner
cp -R install/skeletons/i686/* dist/tinasoft-i686/.
#tar -cvf tinasoft-1.0-i686.tar.gz dist/tinasoft-i686

