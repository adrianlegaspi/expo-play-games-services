# expo-play-games-services

An [Expo](https://expo.dev) native module for **Google Play Games Services** on Android.

Provides a simple JavaScript/TypeScript API for:

- 🔐 **Sign-In** — silent + explicit GPGS authentication
- 🏆 **Achievements** — unlock, increment, and show the native overlay UI
- 💾 **Saved Games** — save/load string data via Play Games Snapshots
- 📦 **Local Persistence** — SharedPreferences with Android Auto Backup support
- 📅 **Install Time** — get the original install timestamp

## Installation

```bash
npx expo install expo-play-games-services
```

Or with npm/yarn:

```bash
npm install expo-play-games-services
# or
yarn add expo-play-games-services
```

## Configuration

Add the config plugin to your `app.config.js` (or `app.json`):

```js
// app.config.js
export default {
  expo: {
    plugins: [
      [
        "expo-play-games-services",
        {
          appId: "YOUR_PLAY_GAMES_APP_ID", // from Google Play Console
        },
      ],
    ],
  },
};
```

The `appId` can also be set via the `PLAY_GAMES_APP_ID` environment variable.

> **Note:** This package is Android-only. All functions return safe fallback values (`false`, `null`, `Date.now()`) on non-Android platforms, so you can safely call them cross-platform without guards.

## Usage

```typescript
import {
  signIn,
  isSignedIn,
  getPlayerId,
  getFirstInstallTime,
  saveSnapshot,
  loadSnapshot,
  unlockAchievement,
  incrementAchievement,
  showAchievementsUI,
  saveLocalFlag,
  loadLocalFlag,
  saveLocalString,
  loadLocalString,
} from "expo-play-games-services";
```

### Sign-In

```typescript
// Trigger silent sign-in (call on app startup)
const success = await signIn();

// Check current sign-in status
const signed = await isSignedIn();

// Get the GPGS player ID
const playerId = await getPlayerId(); // string | null
```

### Achievements

```typescript
// Unlock a standard achievement
await unlockAchievement("CgkI...AQAQ");

// Increment an incremental achievement
await incrementAchievement("CgkI...BQAQ", 5);

// Show the native achievements overlay
await showAchievementsUI();
```

### Saved Games (Snapshots)

```typescript
// Save data
const saved = await saveSnapshot("my-save", JSON.stringify({ level: 5 }));

// Load data
const data = await loadSnapshot("my-save"); // string | null
if (data) {
  const parsed = JSON.parse(data);
}
```

### Local Persistence

```typescript
// Boolean flags
saveLocalFlag("onboarding_done", true);
const done = loadLocalFlag("onboarding_done"); // boolean

// String values
saveLocalString("player_name", "Adrian");
const name = loadLocalString("player_name"); // string | null
```

### Device Info

```typescript
// Get first install time (ms since epoch)
const installTime = getFirstInstallTime();
```

## Requirements

- **Expo SDK** ≥ 52
- **Android** only (all functions return safe no-op values on other platforms)
- A Google Play Console project with Play Games Services enabled

## How It Works

This package uses the [Expo Modules API](https://docs.expo.dev/modules/overview/) to bridge the native [Play Games Services v2 SDK](https://developers.google.com/games/services) to JavaScript.

The included **Expo config plugin** automatically configures your `AndroidManifest.xml` and `strings.xml` with your Play Games App ID at build time — no manual native code changes needed.

## License

MIT © Adrian Legaspi
