#!/bin/bash

# Script to view debug logs for image picker flow
echo "📱 Watching logs for image picker flow..."
echo "🔍 Use Ctrl+C to stop"
echo ""

adb logcat -c  # Clear logcat
adb logcat | grep -E "(YNSUPER|TedImagePicker|SlideShowActivity)"

