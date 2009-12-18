rm -Rf dist
rm tinasoft-*.*
mkdir dist

echo "Generating Linux 64 bit (amd64) distribution.."
cp -R tina dist/tinasoft-amd64
tar -cvf tinasoft-1.0-amd64.tar.gz dist/tinasoft-amd64

echo "Generating Linux 32 bit (i686) distribution.."
cp -R tina dist/tinasoft-i686
rm -Rf dist/tinasoft-i686/xulrunner
cp -R install/skeletons/i686/* dist/tinasoft-i686/.
tar -cvf tinasoft-1.0-i686.tar.gz dist/tinasoft-i686

echo "Generating Windows distribution.."
cp -R tina dist/tinasoft-win
rm -Rf dist/tinasoft-win/xulrunner
rm dist/tinasoft-win/tina # remove the linux executable
cp -R install/skeletons/windows/* dist/tinasoft-win/.
zip -r tinasoft-1.0-win.zip dist/tinasoft-win
#mv tina dist/macosx
#rm dist/macosx/xulrunner
#mount -o loop -t hfs posters_above_are_clueless.dmg /mnt
#cp -R /mnt/*  
#unmount /mnt

#tar -cvf tina-1.0-i386.tar.gz dist/linux32
#tar -cvf tina-1.0-x86_64.tar.gz dist/linux64
#install/tools/makedmg tina-1.0.dmg TinaSoft 50 dist/macosx

#upload to pantheon..
#scp ...

echo "archives sucessfully generated (they are ignored by git)"
