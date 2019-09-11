This repo contains all of the files necessary for building the BirdBrain Robot Server. We created the project in Eclipse, so just import the project into Eclipse if you intend to work with it. Direct any questions to tlauwers@birdbraintechnologies.com

Much more information at http://www.hummingbirdkit.com/learning/software/snap

Instructions for building distribution files:

1. In Eclipse, export BirdBrainRobotServer.jar and save to the folder 'Packages'.
2. Sign the jar
	a. Copy your keystore file into Packages and make sure you know the password
	b. If you do not know the keystore alias you can find it using keytool (eg. 'keytool -v -list -storetype PKCS12 -keystore yourKeystoreFile.pfx')
	c. Sign using jarsigner (eg. 'jarsigner -keystore yourKeystoreFile.pfx -storetype PKCS12 -storepass yourPassword -digestalg SHA1 BirdBrainRobotServer.jar alias')
	d. Verify using 'jarsigner -verify BirdBrainRobotServer.jar'
3. To make the Mac distribution files (On a Mac):
	a. In Terminal, cd into Packages/Mac.
	b. Open buildMacDist.sh and update the version number if necessary.
	c. ./buildMacDist.sh
	d. You should now find both a .dmg file (for manual distribution) and a .pkg (for distribution through the Mac App Store.
4. To make the Windows distribution files (On a Windows machine with WIX installed):
	a. Open buildWinDist.bat and update version number if necessary.
	b. In a command prompt, go into Packages
	c. Run './buildWinDist.bat keystoreFile password' where password is the signing password.
	d. You should now find a .msi installer in Packages\Win\out\bundles