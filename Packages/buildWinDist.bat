:: Build windows installer from jar file
SET CERT=%1
SET PWD=%2
SET VER=1.83

if not defined PWD goto missingPWD

rmdir Win\out /S /Q
mkdir Win\out
rmdir Win\pkgFiles /S /Q
mkdir Win\pkgFiles

copy BirdBrainRobotServer.jar Win\pkgFiles
xcopy sharedResources Win\pkgFiles /s /e
xcopy Win\winResources Win\pkgFiles /s /e

:: Create an msi installer with the java jre packaged.
:: Set BshortcutHint to true to make a desktop shortcut.
javapackager -deploy -native msi -name BirdBrainRobotServer -Bicon=Win\HummingbirdRoundPurple.ico -BappVersion=%ver% -Bidentifier=com.birdbraintechnologies.robotserver -Bvendor="BirdBrain Technologies" -BshortcutHint=true -srcdir Win\pkgFiles -outfile BirdBrainRobotServer -outdir Win\out -v -appclass birdbrain.finchandHummingbirdServer.BirdBrainRobotServer

signtool sign /f %cert% /p %pwd% Win\out\bundles\BirdBrainRobotServer-%ver%.msi

goto end

:missingPWD
echo you must enter the signing certificate and password as the first and second argument

:end
