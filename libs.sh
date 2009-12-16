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

