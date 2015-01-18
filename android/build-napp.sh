##
## Build an Appcelerator Android Module
## Then copy it to the default module directory
##
## (c) Napp ApS
## Mads Møller
##


## How to run the script
## write in terminal in the root folder of your project:
## bash ./build-napp.sh

## compile the module
ant dist

## where is manifest
FILENAME='./manifest'


## FIND MODULE ID
MODULE_ID=$(grep 'moduleid' $FILENAME -m 1)
MODULE_ID=${MODULE_ID#*: } # Remove everything up to a colon and space

## FIND MODULE VERSION
MODULE_VERSION=$(grep 'version' $FILENAME -m 1) ## only one match
MODULE_VERSION=${MODULE_VERSION#*: } # Remove everything up to a colon and space

## Delete the old build if existing
rm -rf /Users/$USER/Library/Application\ Support/Titanium/modules/android/$MODULE_ID/$MODULE_VERSION/*

## unzip compiled module
unzip -o ./dist/$MODULE_ID-android-$MODULE_VERSION.zip -d /Users/$USER/Library/Application\ Support/Titanium

## Optional: You could run a app now - using your new module
PROJECT_PATH='/Volumes/DATA/Titanium/DownloadManagerTest'
cd $PROJECT_PATH
titanium clean

## build the titanium project
titanium build -p android -T device --device-id cd49adbe # Samsung S4
#titanium build -p android -T device --device-id R32D300N6VF ## Nexus 10
#titanium build -p android -T device --device-id J505E1A3C0621C6 ## Kazam
#titanium build -p android -T device --device-id 015d483bac1c1e0a ##Medion