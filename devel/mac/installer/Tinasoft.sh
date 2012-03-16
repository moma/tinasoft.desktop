#!/bin/bash
echo "*************************"
echo " TINASOFT SERVER STARTED "
echo "*************************"
echo ""
echo " - Listening to http://localhost:8888"
echo " - Trying to start Safari, will open in 5 seconds.."
echo ""
echo "Note 1: If Safari don't pop-up, please open it manually and go to this address."
echo ""
echo "Note 2: Be careful! Closing this window will close the server."
echo ""
/Applications/Tinasoft/Tinasoft.app/Contents/MacOS/Tinasoft &
sleep 6
open -a /Applications/Safari.app "http://localhost:8888"
