cp -R viz/dist/lib/*.jar tina/chrome/content/applet/

rm -Rf dist
rm tinasoft-*.*
mkdir dist

install/scripts/build.linux64.sh
tar -cvf tinasoft-1.0-x86_64.tar.gz dist/tinasoft-x86_64

install/scripts/build.linux32.sh
tar -cvf tinasoft-1.0-i686.tar.gz dist/tinasoft-i686

install/scripts/build.win.sh
zip -r tinasoft-1.0-win.zip dist/tinasoft-win

install/scripts/build.mac.sh
zip -r tinasoft-1.0-mac.zip dist/tinasoft-mac
#mount -o loop -t hfs tinasoft.dmg /mnt/mydmg
#cp -R /mnt/mydmg/*  
#unmount /mnt/mydmg
#install/tools/makedmg tinasoft.dmg TinaSoft 100 dist/tinasoft-mac

#upload to pantheon..
#scp ...
echo "archives sucessfully generated (they are ignored by git)"
