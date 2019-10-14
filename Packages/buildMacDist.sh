#!/bin/sh

# Set the app version number here
export VER=1.83

echo "**** About to build BirdBrainRobotServer version $VER for Mac ****"
echo
echo "Removing any old products..."

# Remove old products
rm -rfv Mac/supportFiles/BirdBrainRobotServer.app
rm -fv Mac/BirdBrainRobotServer.dmg
rm -fv Mac/BirdBrainRobotServer.pkg

echo
echo "**** Making the .app ****"

# Make the .app
javapackager -deploy -native image -name BirdBrainRobotServer -Bicon=Mac/supportFiles/HummingbirdRoundPurple.icns -BappVersion=$VER -Bidentifier=com.birdbraintechnologies.robotserver -srcdir . -srcfiles BirdBrainRobotServer.jar -outfile BirdBrainRobotServer -outdir Mac/supportFiles -v -appclass birdbrain.finchandHummingbirdServer.BirdBrainRobotServer

# Copy extra files needed (that cannot be included automatically because of a bug in javapackager
/bin/cp  -R  Mac/appFiles/*  Mac/supportFiles/BirdBrainRobotServer.app/Contents/Java/.
/bin/cp  -R  sharedResources/*  Mac/supportFiles/BirdBrainRobotServer.app/Contents/Java/.

# Sign the app again because of the changes
codesign -f -s "Developer ID Application: Tom Lauwers" Mac/supportFiles/BirdBrainRobotServer.app/

echo
echo "**** Making the .dmg ****"

# Make a .dmg for manual distribution
appdmg Mac/supportFiles/appdmg.json Mac/BirdBrainRobotServer.dmg
codesign -f -s "Developer ID Application: Tom Lauwers" Mac/BirdBrainRobotServer.dmg

echo
echo "**** Making the .pkg ****"

# Make a .pkg for app store distribution (using the 'Application Loader' developer tool in Xcode)
# Requires a 'Developer ID Installer' for signing. Then you can use productsign or the --sign
# flag in productbuild
productbuild --component Mac/supportFiles/BirdBrainRobotServer.app /Applications Mac/BirdBrainRobotServer.pkg

echo
echo "**** Checking signatures..."
echo "... for the .app"
spctl -a -t exec -vv Mac/supportFiles/BirdBrainRobotServer.app
echo "... for the .dmg"
spctl -a -t install -vv Mac/BirdBrainRobotServer.dmg
echo "... for the .pkg"
spctl -a -t install -vv Mac/BirdBrainRobotServer.pkg
