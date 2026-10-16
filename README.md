![KRAMER-SWITCH](/preview/favicon.png)

# KRAMER-SWITCH

**A local network remote control for Kramer HDMI switchers on Android and Android TV.**

KRAMER-SWITCH lets you select HDMI inputs and check the connection to a compatible Kramer video switcher directly from your phone, tablet or Android TV. It is designed for straightforward **one-output HDMI switching** over your local network, with customizable device profiles, input labels, connection settings and a TV-friendly widescreen interface. No cloud account or external control service is required.

![KRAMER-SWITCH](/preview/preview-collage.png)

*Documented app version: **1.2.7***

## Highlights

- **One-tap HDMI switching** — Route a selected HDMI input to the switcher's single output.
- **Kramer VS-81H support** — Start with the built-in 8-input VS-81H profile or choose another compatible one-output profile.
- **Flexible input configuration** — Use predefined 2-, 4-, 8- and 16-input profiles or a custom one-output configuration with personalized HDMI labels.
- **Connection tools** — Test network connectivity, refresh the reported active input and view status messages and the last protocol reply.
- **Android and Android TV** — Portrait and widescreen layouts with D-pad, keyboard and compatible TV remote navigation.
- **Persistent settings** — Save network details, device preferences, input labels and the chosen display mode locally.
- **Backup and restore** — Export settings as JSON or import a saved configuration on another device.
- **Offline legal information** — Review the EULA, Privacy Policy and Legal Notice on first launch and reopen them from the app's information screen.

## Navigation

| Section | What you'll find |
| --- | --- |
| **Main screen** | Current device, connection state, active-input indication and status messages. |
| **HDMI Inputs** | Buttons for each available input, using the configured names. |
| **Quick Actions** | Test Connection, Refresh Status and Power Off. |
| **Connection** | Device model, I/O configuration, network target, machine number, timeout, reply validation and last reply. |
| **Settings** | Device profile, network settings, HDMI labels, display mode, JSON backup/restore and maintenance options. |
| **About / Information** | App version and build type, author, external links and the legal information viewer. |

## Getting Started

1. Connect your compatible Kramer HDMI switcher and Android device to the same reachable local network.
2. Make sure the switcher's TCP control interface is enabled and note its IP address and port.
3. Start KRAMER-SWITCH and review the legal information. Choose **Accept** to continue.
4. Open **Settings** using the gear icon.
5. Select your **Device profile**, check the IP address and TCP port, and save the configuration.
6. Select **Test Connection** to check whether the device can be reached.
7. Tap an **HDMI Input** button to route that source to the output.

For the default **Kramer VS-81H** profile, the initial network values are `192.168.1.39` and TCP port `5000`. Change them if your switch uses a different network configuration.

## HDMI Switching & Connection Tools

### HDMI Inputs

The main screen displays the available HDMI inputs according to the selected profile. Each button can have a custom label so sources are easier to recognize—for example, a receiver, console or media player. The active-input indicator is updated from supported device replies or, when reply validation is disabled, from the command sent by the app.

### Quick Actions

- **Test Connection:** Attempts a TCP connection to the configured switcher. It checks reachability, not whether an HDMI signal is present.
- **Refresh Status:** Requests the current input selection from the switcher using Kramer Protocol 2000.
- **Power Off:** Sends the protocol command to **disconnect the video output**. It does **not** physically switch off the device's mains power.

Connection feedback such as **Ready**, **Connected**, **Working** or a timeout/error message appears on the main screen. The **Connection** card also shows configuration details and the last received protocol reply, when available.

## Device Profiles & Settings

KRAMER-SWITCH includes the following configurations:

| Device profile | Inputs | Outputs |
| --- | ---: | ---: |
| Kramer VS-81H | 8 | 1 |
| Generic Kramer 2x1 | 2 | 1 |
| Generic Kramer 4x1 | 4 | 1 |
| Generic Kramer 8x1 | 8 | 1 |
| Generic Kramer 16x1 | 16 | 1 |
| Custom one-output switcher | Configurable, 1–16 | 1 |

Generic and custom profiles are intended for switchers that use the **compatible Kramer Protocol 2000 command format**. They do not guarantee compatibility with every Kramer product or firmware version. **Matrix routing and multiple-output control are not supported.**

Settings let you configure the device name and model, input count, IP address, TCP port, command timeout, machine number and input labels. **Validate replies** is enabled by default: the app checks for a matching response before confirming a switching command. If disabled, a command can be reported as sent without confirmation from the switch.

Use **Clear** to clear cached app data, or **Reset** to restore the app's initial settings. Resetting the app also clears its recorded legal acceptance, so the first-start information screen will appear again.

## Android TV & Remote Control

KRAMER-SWITCH provides a dedicated widescreen layout for larger screens and Android TV. The app includes an Android TV launcher entry and banner, so it can appear in compatible TV launchers.

The **Display mode** setting provides three choices:

- **Auto:** Selects the layout based on available screen dimensions and orientation.
- **Smartphone / portrait:** Uses the compact phone-style layout.
- **TV / landscape:** Uses the widescreen TV-style layout.

You can operate the interface by touch, keyboard or a compatible Android TV remote. Standard D-pad directions navigate focusable controls, while **OK / Enter** activates the selected control. The app also includes the following keyboard shortcuts:

| Key | Action |
| --- | --- |
| **Arrow keys / D-pad** | Navigate focusable controls. |
| **Enter / OK** | Activate the focused button or control. |
| **Back / Escape** | Close Settings or return from a dialog where supported. |
| **Menu / Settings** | Open the Settings screen where the key is supported by the device. |
| **1–9** | Select the corresponding HDMI input, if present. |
| **0** | Select HDMI input 10, if the configured profile includes it. |

Remote key behavior and focus handling may vary with the Android TV device, launcher and remote model.

## Backup & Restore

Use **Export** in Settings to create a local JSON file containing the selected device profile, network configuration, display mode, input count and custom HDMI labels. Use **Import** to load a previously exported configuration.

These backups contain **app settings**, not device firmware, HDMI media or a copy of the switcher's internal configuration. The imported settings are stored locally on the Android device.

## Privacy & Permissions

KRAMER-SWITCH communicates directly with the configured switcher over **TCP/IP on your local network**. It does not require registration, a cloud account, analytics or third-party control services. Local settings and the legal acceptance state are stored on the device.

Android network permissions are required to reach the HDMI switcher. Opening a GitHub or website link from the About card may launch an external browser; the destination site's own privacy terms then apply.

On first launch, the app presents the **EULA, Privacy Policy and Legal Notice** for acceptance. You can reopen the information view later through the **i** button in the About card. The legal documents are bundled with the application and can be read without an internet connection. The project also includes a separate Copyright notice:

- [EULA](legal/EULA.md)
- [Privacy Policy](legal/PRIVACY_POLICY.md)
- [Legal Notice](legal/LEGAL_NOTICE.md)
- [Copyright](legal/COPYRIGHT.md)

## Important Notes

- KRAMER-SWITCH is a **remote controller**, not an HDMI video player, streaming service or signal converter.
- The Android device and switcher must have working network connectivity for control commands to succeed.
- **Power Off** disconnects the switcher's video output; it is not a hardware power switch.
- The connection test does not verify video signal quality or active HDMI sources.
- Only **single-output switching** is implemented; Kramer matrix devices are outside the current feature scope.
- Kramer Protocol 2000 traffic is sent over a direct TCP connection. Use the app on a trusted local network rather than exposing the switcher's control port to the public internet.
- Generic profiles and TV remote behavior depend on the capabilities of the connected hardware.

## Project & Copyright

KRAMER-SWITCH is an independent, non-commercial hobby project by **Sven Klocke** (*complicatiion / sksdesign*).

**GitHub profile:** [github.com/complicatiion](https://github.com/complicatiion)  
**Website:** [sksdesign.de](https://sksdesign.de)

KRAMER-SWITCH is **not affiliated with, endorsed by or sponsored by Kramer Electronics Ltd. or any related company**. All trademarks, product names and brands belong to their respective owners.

Copyright © 2026 Sven Klocke. All rights reserved. Software usage, redistribution and third-party component rights are governed by the applicable legal notices supplied with the project.
