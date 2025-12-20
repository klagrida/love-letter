# Love Letter - Kotlin Multiplatform Game

A complete implementation of the classic Love Letter card game using Kotlin Multiplatform and Compose Multiplatform, with Supabase for real-time multiplayer functionality.

## Platforms Supported

- **Android** - Native Android app using Jetpack Compose
- **iOS** - Native iOS app using SwiftUI + Compose Multiplatform
- **Desktop** - Windows, macOS, and Linux desktop applications
- **Web** - Browser-based version using Compose for Web

## Features

- **Single Player** - Play locally with pass-and-play multiplayer
- **Online Multiplayer** - Real-time multiplayer using Supabase
- **Beautiful UI** - Material 3 design with Love Letter themed colors
- **Cross-Platform** - Same codebase runs on all platforms
- **Complete Game Rules** - Full implementation of Love Letter rules

## Game Rules

Love Letter is a game of risk, deduction, and luck for 2-4 players. Players draw and play cards to eliminate rivals and earn the Princess's affection.

### Cards

| Card | Value | Count | Effect |
|------|-------|-------|--------|
| Guard | 1 | 5 | Name a non-Guard card. If the target has it, they're eliminated |
| Priest | 2 | 2 | Look at another player's hand |
| Baron | 3 | 2 | Compare hands; lower card is eliminated |
| Handmaid | 4 | 2 | Protection until your next turn |
| Prince | 5 | 2 | Target discards their hand and draws new |
| King | 6 | 1 | Trade hands with another player |
| Countess | 7 | 1 | Must discard with King or Prince |
| Princess | 8 | 1 | Eliminated if you discard this |

### Winning

- Win a round by having the highest card when the deck runs out, or by being the last player standing
- Earn tokens of affection for each round won
- Win the game by earning enough tokens (7 for 2 players, 5 for 3, 4 for 4)

## Project Structure

```
love-letter/
├── shared/                 # Shared Kotlin Multiplatform code
│   └── src/
│       ├── commonMain/     # Common code for all platforms
│       │   └── kotlin/
│       │       └── com/loveletter/
│       │           ├── game/       # Game logic
│       │           ├── network/    # Supabase integration
│       │           └── ui/         # Compose UI
│       ├── androidMain/    # Android-specific code
│       ├── iosMain/        # iOS-specific code
│       ├── desktopMain/    # Desktop-specific code
│       └── jsMain/         # Web-specific code
├── androidApp/             # Android application
├── iosApp/                 # iOS application (Xcode project)
├── desktopApp/             # Desktop application
└── webApp/                 # Web application
```

## Setup

### Prerequisites

- JDK 17 or higher
- Android Studio (for Android development)
- Xcode (for iOS development, macOS only)
- Node.js (for web development)

### Supabase Configuration

1. Create a Supabase project at https://supabase.com
2. Run the SQL migration in `shared/src/commonMain/kotlin/com/loveletter/network/SupabaseConfig.kt`
3. Update the Supabase URL and anon key in `SupabaseConfig.kt`

### Building

#### Android
```bash
./gradlew :androidApp:assembleDebug
```

#### Desktop
```bash
./gradlew :desktopApp:run
```

#### Web
```bash
./gradlew :webApp:jsBrowserDevelopmentRun
```

#### iOS
Open `iosApp/iosApp.xcodeproj` in Xcode and build

## Dependencies

- **Kotlin Multiplatform** - Cross-platform development
- **Compose Multiplatform** - Declarative UI framework
- **Supabase Kotlin** - Real-time backend
- **Ktor** - HTTP client
- **Kotlinx Serialization** - JSON serialization
- **Kotlinx Coroutines** - Asynchronous programming
- **Kotlinx DateTime** - Date/time handling

## License

MIT License - feel free to use this code for your own projects!

## Credits

Love Letter is a game designed by Seiji Kanai, published by AEG. This is a fan-made digital implementation for educational purposes.
