
cd install/libs/pyxpcomext

wget http://downloads.mozdev.org/pyxpcomext/pythonext-2.6.0.20090330-WINNT_x86-msvc.xpi
unzip pythonext-2.6.0.20090330-WINNT_x86-msvc.xpi pythonext-2.6.0.20090330-WINNT_x86-msvc

wget http://downloads.mozdev.org/pyxpcomext/pythonext-2.6.0.20090330-Linux_x86-gcc3.xpi
unzip pythonext-2.6.0.20090330-Linux_x86-gcc3.xpi pythonext-2.6.0.20090330-Linux_x86-gcc3

wget http://downloads.mozdev.org/pyxpcomext/pythonext-2.6.1.20090330-Linux_x86_64-gcc3.xpi
unzip pythonext-2.6.1.20090330-Linux_x86_64-gcc3.xpi pythonext-2.6.1.20090330-Linux_x86_64-gcc3

wget http://downloads.mozdev.org/pyxpcomext/pythonext-2.6.2.20091013-Darwin_universal.xpi
unzip pythonext-2.6.2.20091013-Darwin_universal.xpi pythonext-2.6.2.20091013-Darwin_universal


cd ../xulrunner

wget http://releases.mozilla.org/pub/mozilla.org/xulrunner/releases/1.9.1.4/runtimes/xulrunner-1.9.1.4.en-US.linux-i686.tar.bz2
tar xcf xulrunner-1.9.1.4.en-US.linux-i686.tar.bz2 xulrunner-1.9.1.4.en-US.linux-i686

wget http://releases.mozilla.org/pub/mozilla.org/xulrunner/releases/1.9.1.4/runtimes/xulrunner-1.9.1.4.en-US.mac-pkg.dmg
#mount -o loop -t hfs xulrunner-1.9.1.4.en-US.mac-pkg.dmg /mnt
#cp -R /mnt/*  
#unmount /mnt

wget http://releases.mozilla.org/pub/mozilla.org/xulrunner/releases/1.9.1.4/runtimes/xulrunner-1.9.1.4.en-US.win32.zip
unzip xulrunner-1.9.1.4.en-US.win32.zip xulrunner-1.9.1.4.en-US.win32

cd ../../..

# This gets and builds a patched version of Apple's diskdev_cmds package which will work on Linux
wget http://www.mythic-beasts.com/resources/appletv/mb_boot_tv/diskdev_cmds-332.14.tar.gz
wget http://www.ecl.udel.edu/~mcgee/diskdev_cmds/diskdev_cmds-332.14.patch.bz2
tar xzf diskdev_cmds-332.14.tar.gz
bunzip2 -c diskdev_cmds-332.14.patch.bz2 | patch -p0
cd diskdev_cmds-332.14
make -f Makefile.lnx

# Create symlinks to the mkfs and fsck commands for HFS+
sudo cp newfs_hfs.tproj/newfs_hfs /sbin/mkfs.hfsplus
sudo cp fsck_hfs.tproj/fsck_hfs /sbin/fsck.hfsplus

# Get and enable the hfsplus kernel module
sudo apt-get install hfsplus
sudo modprobe hfsplus

cd ..
rm -Rf diskdev_cmds-332.14
