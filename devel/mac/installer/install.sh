#!/bin/bash
echo "Installing Tinasoft.."
base=/Applications/Tinasoft
if [ -e $base ]
  then
    echo "Uninstalling previous version.."
    rm -rf $base
fi
mkdir -p $base
mv Tinasoft.app $base/
echo "Moving config files out of the Tinasoft.app directory.."
mkdir -p $base/shared/
mkdir -p $base/whitelists/
mkdir -p $base/source_files/
mkdir -p $base/sessions/
resources=$base/Tinasoft.app/Contents/Resources
mv $resources/shared $base/
mv $resources/source_files $base/
mv $resources/whitelists $base/
mv $resources/config_mac.yaml $base/config.yaml
echo "Installing executable in Desktop and Applications.."
chmod +x Tinasoft.sh
cp Tinasoft.sh $base/Tinasoft
cp Tinasoft.sh ~/Desktop/Tinasoft
echo "Cleaning.."
rm *
echo "you can now start the application by running 'Tinasoft'"
