#!/bin/bash
echo ""
echo "**************************"
echo " TINASOFT - MAC INSTALLER "
echo "**************************"
echo ""
echo "  - installing Tinasoft to /Applications"
base=/Applications/Tinasoft
if [ -e $base ]
  then
    echo "  - uninstalling previous version.."
    rm -rf $base
fi
mkdir -p $base
mv Tinasoft.app $base/
echo "  - installing config files to /Applications/Tinasoft/"
mkdir -p $base/shared/
mkdir -p $base/whitelists/
mkdir -p $base/source_files/
mkdir -p $base/sessions/
resources=$base/Tinasoft.app/Contents/Resources
cp -r $resources/shared/* $base/shared/
rm -Rf $resources/shared
cp -r $resources/source_files/* $base/source_files/
rm -Rf $resources/shared
#mv $resources/whitelists $base/
cp -r $resources/config_mac.yaml $base/config.yaml
rm $resources/config_mac.yaml
cp -r $resources/user_stopwords.csv $base/shared/
rm $resources/user_stopwords.csv
echo "  - installing shortcut to /Applications/Tinasoft/"
cp Tinasoft.sh $base/Tinasoft
chmod +x $base/Tinasoft
echo "  - installing shortcut to ~/Desktop/"
mkdir -p ~/Desktop/
if [ -e ~/Desktop/Tinasoft ]
  then
    rm -Rf ~/Desktop/Tinasoft
fi
cp Tinasoft.sh ~/Desktop/Tinasoft
chmod +x ~/Desktop/Tinasoft
echo ""
echo ""
echo ""
if [ -e /Applications/Tinasoft/Tinasoft ]
  then
    echo "*************************"
    echo " INSTALLATION SUCCESSFUL "
    echo "*************************"
    echo ""
    #rm *
    echo "Will now try to start server automatically. Please wait."
    echo ""
    echo "cd ~/Desktop"
    cd ~/Desktop
    echo "./Tinasoft"
    ./Tinasoft
  else
    echo "!!!!!!!!!!!!!!!!!!!!!!!!!"
    echo "!! INSTALLATION FAILED !!"
    echo "!!!!!!!!!!!!!!!!!!!!!!!!!"
    echo ""
    echo "please send the installation history (copy/pasted from this terminal)"
    echo "to julian.bilcke@gmail.com - Thanks a lot!"
    #if [ -e /Applications/Tinasoft ]
    #  then
    #    #rm -Rf /Applications/Tinasoft
    #fi
fi