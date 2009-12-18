rm dist
mkdir dist
echo "Generating Windows distribution.."
mv tina dist/windows
rm dist/windows/xulrunner
cp -R install/skeleton/windows dist/windows
zip -r tinasoft-1.0.zip dist/windows
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

echo "archives sucessfully generated"
