import { ConfigPlugin, withAndroidManifest, withStringsXml } from 'expo/config-plugins';

interface PluginProps {
  /** Your Google Play Games App ID (numeric string from Play Console). */
  appId?: string;
}

/**
 * Expo config plugin that configures Google Play Games Services for Android.
 *
 * Adds the required <meta-data> tag to AndroidManifest.xml and writes the
 * GPGS App ID into res/values/strings.xml so the SDK can find it at runtime.
 *
 * Usage in app.config.js / app.json:
 *   ["expo-play-games-services", { appId: "123456789012" }]
 *
 * The appId can also come from the PLAY_GAMES_APP_ID env var.
 */
const withPlayGamesServices: ConfigPlugin<PluginProps> = (config, props = {}) => {
  const appId = props.appId || process.env.PLAY_GAMES_APP_ID || '000000000000';

  // 1. Add <meta-data> to AndroidManifest.xml
  config = withAndroidManifest(config, (config) => {
    const mainApplication = config.modResults.manifest.application?.[0];

    if (!mainApplication) return config;

    if (!mainApplication['meta-data']) {
      mainApplication['meta-data'] = [];
    }

    // Remove existing entry if present (idempotent)
    mainApplication['meta-data'] = mainApplication['meta-data'].filter(
      (item: any) => item.$?.['android:name'] !== 'com.google.android.gms.games.APP_ID',
    );

    // Add the GPGS App ID meta-data
    mainApplication['meta-data'].push({
      $: {
        'android:name': 'com.google.android.gms.games.APP_ID',
        'android:value': '@string/play_games_app_id',
      },
    });

    return config;
  });

  // 2. Add the app_id string resource
  config = withStringsXml(config, (config) => {
    const strings = config.modResults.resources.string || [];

    // Remove existing entry if present (idempotent)
    config.modResults.resources.string = strings.filter(
      (item: any) => item.$?.name !== 'play_games_app_id',
    );

    config.modResults.resources.string.push({
      $: { name: 'play_games_app_id', translatable: 'false' },
      _: appId,
    });

    return config;
  });

  return config;
};

export default withPlayGamesServices;
