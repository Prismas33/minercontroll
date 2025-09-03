# Screenshots

This directory contains screenshots of MinerControl Android for documentation purposes.

## Planned Screenshots

- `dashboard.png` - Main dashboard showing miners and statistics
- `settings.png` - Settings screen with configuration options  
- `details.png` - Detailed miner information view
- `gauges.png` - Circular gauge panels showing metrics

## Guidelines for Screenshots

- **Resolution**: 1080x2400 (typical Android phone)
- **Format**: PNG with transparency where applicable
- **Content**: Show realistic mining data
- **Quality**: High resolution for documentation use

## Taking Screenshots

1. Connect real miners or use mock data
2. Set up ideal dashboard state (multiple miners online)
3. Use Android Studio device screenshots or ADB:
   ```bash
   adb shell screencap -p /sdcard/screenshot.png
   adb pull /sdcard/screenshot.png
   ```
4. Edit for privacy (blur IP addresses if needed)
5. Optimize file size while maintaining quality
