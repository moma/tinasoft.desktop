rm -Rf dist/*
touch HERE_ARE_THE_VARIOUS_BUILDS

mv tina dist/linux64
# nothing to do for the 64 bit version..

mv tina dist/linux32
rm dist/linux32/xulrunner
cp -R install/libs/xulrunner/xulrunner-1.9.1.4.en-US.linux-i686 dist/linux32/xulrunner

#mv tina dist/win32
#rm dist/win32/xulrunner
#cp -RL install/xulrunner


#mv tina dist/macosx
#rm dist/macosx/xulrunner
#mount -o loop -t hfs posters_above_are_clueless.dmg /mnt
#cp -R /mnt/*  
#unmount /mnt

rm output
mkdir output
#zip -r output/tina-1.0-for-windows.zip dist/win32 
tar -cvf output/tina-1.0-i386.tar.gz dist/linux32
tar -cvf output/tina-1.0-x86_64.tar.gz dist/linux64
#install/tools/makedmg tina-1.0.dmg TinaSoft 50 dist/macosx
#cp tina-1.0.dmg output/

#upload to pantheon..
#scp ...

echo "archives sucessfully generated to output!"
