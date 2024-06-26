# Fake Power Off
The Fake Power Off app convincingly simulates device shutdown using a subtle animation, effectively deterring unauthorized access without actually powering off the device. It may seamlessly be integrated with anti-theft apps and remains effective even when the device is locked.

## Demo Video:
Short demo: <a href="https://youtube.com/shorts/NDdwKGHlrnw"/>Link</a>

## Usage Instructions:
0. Download the app from the <a href="https://github.com/BinitDOX/FakePowerOff/releases/tag/v0.1">releases</a> page and install it.
1. Give the necessary accessibility and dnd permission.
2. Enable Fake Power Off in the settings menu.
3. Configure rest of the settings as needed, specially the dismiss sequence.
4. Hold power button for ~5s for original power menu.
5. FPO will dismiss the original power menu with a fake menu.
6. Pressing any of the buttons will trigger the fake shutdown.
7. Recover from the black overlay using the set dismiss sequence (Default: UUDD).

## Notes:
- You can also hard-restart the device (hold power button for ~10s), if unable to dismiss the black overlay.
- Some elements like fingerprint animation may be visible over the overlay, so turn off the lock-device setting.
- Everything else, may it be calls, notifications, alarms etc, wont be able to appear over the overlay.
