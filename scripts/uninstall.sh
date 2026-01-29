#!/bin/bash

# Uninstall NectarSSH app from connected Android device
# App ID: com.rosi.nectarssh

echo "Uninstalling NectarSSH (com.rosi.nectarssh)..."
adb uninstall com.rosi.nectarssh

if [ $? -eq 0 ]; then
    echo "✓ NectarSSH uninstalled successfully"
else
    echo "✗ Failed to uninstall NectarSSH"
    echo "  Make sure a device is connected: adb devices"
    exit 1
fi
