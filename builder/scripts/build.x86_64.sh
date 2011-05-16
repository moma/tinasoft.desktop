#!/usr/bin/env bash
echo "#############################################"
echo "# BUILD TINASOFT FOR 64 BIT LINUX PLATFORMS #"
echo "#############################################"
echo ""
name="Tinasoft"

version="1.0"
arch="GNU_Linux_x86_64"
buildname="exe.linux-x86_64-2.6"
outfile="$name-$version-$arch"
outpath="dist/$outfile"

./builder/scripts/subscript.build.linux.sh "$outpath" "$buildname" "$outfile"
