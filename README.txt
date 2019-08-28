This repo contains all of the files necessary for building the BirdBrain Robot Server. We created the project in Eclipse, so just import the project into Eclipse if you intend to work with it. Direct any questions to tlauwers@birdbraintechnologies.com

Much more information at http://www.hummingbirdkit.com/learning/software/snap

Instructions for building distribution files:

1. In Eclipse, export BirdBrainRobotServer.jar and save to the folder 'Packages'.
2. To make the Mac distribution files:
	a. In Terminal, cd into Packages/Mac.
	b. Open buildMacDist.sh and update the version number if necessary.
	c. ./buildMacDist.sh
	d. You should now find both a .dmg file (for manual distribution) and a .pkg (for distribution through the Mac App Store.