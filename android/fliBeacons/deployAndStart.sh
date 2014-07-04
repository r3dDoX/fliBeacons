#!/bin/bash

gradle assembleDebug 
adbplus shell pm uninstall -k ch.fork.flibeacons
#adbplus uninstall -k ch.fork.flibeacons 
adbplus install -r app/build/outputs/apk/app-debug.apk 
adbplus shell am start -n ch.fork.flibeacons/.activities.StartActivity
