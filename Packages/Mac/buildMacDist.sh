#!/bin/sh

# Set the app version number here
export VER=1.83

echo "**** About to build BirdBrainRobotServer version $VER for Mac ****"
echo
echo "Removing any old products..."

# Remove old products
rm -rfv supportFiles/BirdBrainRobotServer.app
rm -fv BirdBrainRobotServer.dmg
rm -fv BirdBrainRobotServer.pkg

echo
echo "**** Making the .app ****"

# Make the .app
javapackager -deploy -native image -name BirdBrainRobotServer -Bicon=supportFiles/LightBulbKnockedOut.icns -BappVersion=$VER -Bidentifier=com.birdbraintechnologies.robotserver -srcdir ../ -srcfiles BirdBrainRobotServer.jar -outfile BirdBrainRobotServer -outdir supportFiles -v -appclass birdbrain.finchandHummingbirdServer.BirdBrainRobotServer

# Copy extra files needed (that cannot be included automatically because of a bug in javapackager
/bin/cp  -R  appFiles/*  supportFiles/BirdBrainRobotServer.app/Contents/Java/.

echo
echo "**** Making the .dmg ****"

# Make a .dmg for manual distribution
appdmg supportFiles/appdmg.json BirdBrainRobotServer.dmg

echo
echo "**** Making the .pkg ****"

# Make a .pkg for app store distribution (using the 'Application Loader' developer tool in Xcode)
productbuild --component supportFiles/BirdBrainRobotServer.app /Applications BirdBrainRobotServer.pkg
