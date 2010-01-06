echo "Generating Linux 64 bit (x86_64) distribution.."
cp -R tina dist/tinasoft-x86_64
rm -Rf dist/tinasoft-i686/xulrunner
cp -R install/skeletons/x86_64/* dist/tinasoft-x86_64/.
