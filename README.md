# TravelRouter - Android VPN Router App

A native Android application that turns your phone into a travel router by connecting to WireGuard VPN and allowing hotspot sharing.

## Features

- **WireGuard VPN Integration**: Connect to WireGuard VPN servers using imported `.conf` files
- **Secure Configuration Storage**: Uses Android's EncryptedSharedPreferences for secure config storage
- **Hotspot Sharing**: Deep-link to Android hotspot/tethering settings for sharing VPN connection
- **Modern UI**: Built with Jetpack Compose and Material3 design
- **Real-time Status**: Shows connection status, data usage, and server information
- **Configuration Management**: Add, edit, and delete VPN configurations

## Requirements

- Android 8.0 (API level 26) or higher
- VPN permissions granted by the user
- WireGuard configuration files

## Setup

1. **Clone the repository**:
   ```bash
   git clone <repository-url>
   cd MyTravelRouter
   ```

2. **Open in Android Studio**:
   - Open Android Studio
   - Select "Open an existing project"
   - Navigate to the project folder and select it

3. **Build the project**:
   - Sync the project with Gradle files
   - Build the project (Build → Make Project)

## Usage

### Adding VPN Configurations

1. Open the app and tap the settings icon
2. Tap the "+" button to add a new configuration
3. Enter a name for your configuration
4. Paste your WireGuard `.conf` file content
5. Tap "Save Configuration"

### Connecting to VPN

1. On the main dashboard, tap "Connect" to start the VPN
2. Grant VPN permissions when prompted
3. The app will establish the WireGuard tunnel
4. Use the "Open Hotspot Settings" button to enable hotspot sharing

### Managing Configurations

- **Edit**: Tap the edit icon next to any configuration
- **Delete**: Tap the delete icon and confirm deletion
- **Connect**: Tap "Connect" on any configuration to use it

## Project Structure

```
app/src/main/java/com/kentonprojects/mytravelrouter/
├── data/
│   └── ConfigManager.kt              # Secure config storage and parsing
├── navigation/
│   └── TravelRouterNavigation.kt     # Navigation setup
├── service/
│   └── TravelRouterVpnService.kt     # VPN service implementation
├── ui/
│   ├── components/                   # Compose UI components
│   │   ├── AddConfigScreen.kt
│   │   ├── ConfigScreen.kt
│   │   ├── DashboardScreen.kt
│   │   └── EditConfigScreen.kt
│   └── theme/                        # Material3 theming
│       ├── Color.kt
│       ├── Theme.kt
│       └── Type.kt
├── viewmodel/
│   └── VpnViewModel.kt               # ViewModel for state management
└── MainActivity.kt                   # Main activity
```

## Dependencies

- **WireGuard-Go**: Native WireGuard implementation
- **Jetpack Compose**: Modern UI toolkit
- **Material3**: Material Design components
- **Lottie**: Animation support
- **Navigation Compose**: Navigation between screens
- **Security Crypto**: Encrypted storage

## Permissions

The app requires the following permissions:

- `INTERNET`: For VPN connectivity
- `ACCESS_NETWORK_STATE`: To monitor network status
- `ACCESS_WIFI_STATE` / `CHANGE_WIFI_STATE`: For hotspot management
- `ACCESS_FINE_LOCATION` / `ACCESS_COARSE_LOCATION`: Required for WiFi scanning
- `WRITE_SETTINGS`: For hotspot configuration
- `BIND_VPN_SERVICE`: For VPN service binding

## Security

- All VPN configurations are stored using Android's EncryptedSharedPreferences
- Private keys and sensitive data are encrypted at rest
- No data is transmitted to external servers

## Troubleshooting

### VPN Connection Issues

1. **Permission Denied**: Ensure VPN permissions are granted in Android settings
2. **Invalid Configuration**: Check that your WireGuard config is properly formatted
3. **Network Issues**: Verify your internet connection and server availability

### Hotspot Issues

1. **Hotspot Not Working**: Check if hotspot is enabled in Android settings
2. **Devices Can't Connect**: Ensure the VPN is connected before enabling hotspot

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Test thoroughly
5. Submit a pull request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Disclaimer

This app is for personal use only. Users are responsible for complying with local laws and regulations regarding VPN usage and internet sharing.
