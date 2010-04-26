#!/bin/bash

echo "#############################"
echo "# TINASOFT BUILD SCRIPT 1.0 #"
echo "#############################"
echo ""

rm tina/log/*.txt

#cp -R viz/dist/lib/*.jar tina/chrome/content/applet/
applet="viz/dist/tinaviz.jar"    # /   (root directory)
if [ -e "$applet" ]
then
  echo "copying applet from $applet"
  cp -R $applet tina/chrome/content/applet
fi

if [ -e "dist" ]
then
  echo "cleaning old dist directory"
  rm -Rf dist/*
else
  mkdir dist
fi

install/scripts/build.x86_64.sh
