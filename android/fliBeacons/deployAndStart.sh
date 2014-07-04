#!/bin/bash

gradle assembleDebug 
adbplus install -r app/build/outputs/apk/app-debug.apk 
adbplus shell am start -n ch.fork.flibeacons/.activities.StartActivity
