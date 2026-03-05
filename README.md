# expo-play-games-services

An Expo native module for Google Play Games Services on Android.

Provides a simple JavaScript/TypeScript API for:
- Sign-In: Silent and explicit GPGS authentication
- Achievements: Unlock, increment, and show the native overlay UI
- Saved Games: Save and load string data via Play Games Snapshots
- Local Persistence: SharedPreferences with Android Auto Backup support
- Install Time: Get the original install timestamp

## Installation

```bash
npx expo install expo-play-games-services
```

## Configuration

Add the config plugin to your `app.config.js` or `app.json`:

```js
export default {
  expo: {
    plugins: [
      [
        "expo-play-games-services",
        {
          appId: "YOUR_PLAY_GAMES_APP_ID",
        },
      ],
    ],
  },
};
```

The `appId` can also be set via the `PLAY_GAMES_APP_ID` environment variable.

Note: This package is Android-only. All functions return fallback values (false, null, Date.now()) on non-Android platforms safely.

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
const success = await signIn();
const signed = await isSignedIn();
const playerId = await getPlayerId();
```

### Achievements

```typescript
await unlockAchievement("CgkI...AQAQ");
await incrementAchievement("CgkI...BQAQ", 5);
await showAchievementsUI();
```

### Saved Games (Snapshots)

```typescript
const saved = await saveSnapshot("my-save", JSON.stringify({ level: 5 }));
const data = await loadSnapshot("my-save");
```

### Local Persistence

```typescript
saveLocalFlag("onboarding_done", true);
const done = loadLocalFlag("onboarding_done");

saveLocalString("player_name", "Adrian");
const name = loadLocalString("player_name");
```

### Device Info

```typescript
const installTime = getFirstInstallTime();
```

## Requirements

- Expo SDK 52 or higher
- Android only
- Google Play Console project with Play Games Services enabled

## License

MIT © Adrian Legaspi
