#!/bin/sh

export VER=1.83

javapackager -deploy -native msi -name BirdBrainRobotServer -Bicon=supportFiles/LightBulbKnockedOut.icns -BappVersion=1.83 -Bidentifier=com.birdbraintechnologies.robotserver -srcdir ../ -srcfiles BirdBrainRobotServer.jar -outfile BirdBrainRobotServer -outdir supportFiles -v -appclass birdbrain.finchandHummingbirdServer.BirdBrainRobotServer
