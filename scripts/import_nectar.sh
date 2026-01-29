#!/bin/bash

# Import NectarSSH configuration via adb
# Usage: ./import_nectar.sh <export_file.json>

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
PACKAGE_NAME="com.rosi.nectarssh"
ACTIVITY_CLASS="com.rosi.nectarssh.ImportActivity"
IMPORT_ACTION="${PACKAGE_NAME}.IMPORT_DATA"
TEMP_DEVICE_PATH="/sdcard/nectar_import_temp.json"

# Function to display usage
usage() {
    echo "Usage: $0 <export_file.json>"
    echo ""
    echo "Import NectarSSH configuration via adb"
    echo ""
    echo "Arguments:"
    echo "  export_file.json    Path to exported configuration file"
    echo ""
    echo "Requirements:"
    echo "  - adb must be installed and in PATH"
    echo "  - Android device connected via USB or network"
    echo "  - NectarSSH app installed on device"
    echo ""
    echo "Example:"
    echo "  $0 nectarssh_export_1234567890.json"
    exit 1
}

# Check arguments
if [ $# -ne 1 ]; then
    echo -e "${RED}Error: Missing argument${NC}"
    echo ""
    usage
fi

EXPORT_FILE="$1"

echo -e "${BLUE}=== NectarSSH Import Tool ===${NC}"
echo ""

# Validate file exists
echo -e "${BLUE}[1/6]${NC} Validating export file..."
if [ ! -f "$EXPORT_FILE" ]; then
    echo -e "${RED}✗ Error: File '$EXPORT_FILE' not found${NC}"
    exit 1
fi
echo -e "${GREEN}✓${NC} File found: $EXPORT_FILE"

# Validate JSON (if jq available)
if command -v jq &> /dev/null; then
    if ! jq empty "$EXPORT_FILE" 2>/dev/null; then
        echo -e "${RED}✗ Error: Invalid JSON file${NC}"
        exit 1
    fi

    # Show export contents
    VERSION=$(jq -r '.version' "$EXPORT_FILE" 2>/dev/null || echo "unknown")
    IDENTITIES=$(jq '.identities | length' "$EXPORT_FILE" 2>/dev/null || echo "0")
    CONNECTIONS=$(jq '.connections | length' "$EXPORT_FILE" 2>/dev/null || echo "0")
    PORT_FORWARDS=$(jq '.portForwards | length' "$EXPORT_FILE" 2>/dev/null || echo "0")
    EXPORT_DATE=$(jq -r '.exportDate' "$EXPORT_FILE" 2>/dev/null || echo "unknown")

    echo -e "${GREEN}✓${NC} JSON validation passed"
    echo "  Export version: $VERSION"
    echo "  Identities: $IDENTITIES"
    echo "  Connections: $CONNECTIONS"
    echo "  Port Forwards: $PORT_FORWARDS"
    echo "  Export date: $EXPORT_DATE"
else
    echo -e "${YELLOW}⚠${NC}  jq not installed, skipping JSON validation"
fi

# Check adb installation
echo ""
echo -e "${BLUE}[2/6]${NC} Checking adb installation..."
if ! command -v adb &> /dev/null; then
    echo -e "${RED}✗ Error: adb not found${NC}"
    echo "  Please install Android SDK Platform Tools"
    echo "  Download from: https://developer.android.com/tools/releases/platform-tools"
    exit 1
fi
echo -e "${GREEN}✓${NC} adb found: $(which adb)"

# Check adb connection
echo ""
echo -e "${BLUE}[3/6]${NC} Checking device connection..."
ADB_DEVICES=$(adb devices 2>/dev/null | tail -n +2 | grep -v "^$")
if ! echo "$ADB_DEVICES" | grep -q "device$"; then
    echo -e "${RED}✗ Error: No device connected via adb${NC}"
    echo ""
    echo "Available devices:"
    adb devices
    echo ""
    echo "Troubleshooting:"
    echo "  1. Connect your device via USB"
    echo "  2. Enable USB debugging in Developer Options"
    echo "  3. Accept USB debugging prompt on device"
    echo "  4. Run 'adb devices' to verify connection"
    exit 1
fi
DEVICE_ID=$(echo "$ADB_DEVICES" | head -n 1 | awk '{print $1}')
echo -e "${GREEN}✓${NC} Device connected: $DEVICE_ID"

# Check if app is installed
echo ""
echo -e "${BLUE}[4/6]${NC} Checking NectarSSH installation..."
if ! adb shell pm list packages 2>/dev/null | grep -q "^package:$PACKAGE_NAME$"; then
    echo -e "${RED}✗ Error: NectarSSH is not installed on device${NC}"
    echo "  Please install the app first:"
    echo "  ./gradlew installDebug"
    exit 1
fi
echo -e "${GREEN}✓${NC} NectarSSH is installed"

# Push file to device
echo ""
echo -e "${BLUE}[5/6]${NC} Pushing configuration file to device..."
if ! adb push "$EXPORT_FILE" "$TEMP_DEVICE_PATH" 2>&1 | grep -q "pushed"; then
    echo -e "${RED}✗ Error: Failed to push file to device${NC}"
    exit 1
fi
FILE_SIZE=$(stat -f%z "$EXPORT_FILE" 2>/dev/null || stat -c%s "$EXPORT_FILE" 2>/dev/null || echo "unknown")
echo -e "${GREEN}✓${NC} File transferred successfully ($FILE_SIZE bytes)"

# Read file content for passing as string extra
echo ""
echo -e "${BLUE}[6/6]${NC} Launching import in NectarSSH..."
JSON_CONTENT=$(cat "$EXPORT_FILE")

# Launch import intent with JSON content as string extra
# Using file:// URI with the pushed file as fallback
if ! adb shell am start \
    -a "$IMPORT_ACTION" \
    -e IMPORT_CONTENT "file://$TEMP_DEVICE_PATH" \
    "$PACKAGE_NAME/$ACTIVITY_CLASS" 2>&1 | grep -q "Starting"; then
    echo -e "${RED}✗ Error: Failed to launch import intent${NC}"
    # Clean up temp file
    adb shell rm "$TEMP_DEVICE_PATH" 2>/dev/null || true
    exit 1
fi
echo -e "${GREEN}✓${NC} Import dialog launched successfully"

echo ""
echo -e "${YELLOW}┌─────────────────────────────────────────────┐${NC}"
echo -e "${YELLOW}│  Please confirm the import on your device  │${NC}"
echo -e "${YELLOW}└─────────────────────────────────────────────┘${NC}"
echo ""
echo "The import dialog is now showing on your device."
echo "Review the configuration details and tap 'Import & Replace' to proceed."
echo ""
echo -e "${YELLOW}⚠  WARNING: This will replace ALL existing data!${NC}"
echo ""

