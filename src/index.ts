import { requireNativeModule, Platform } from 'expo-modules-core';

const MODULE_NAME = 'PlayGamesServices';

// On non-Android platforms the native module won't exist.
const NativeModule =
  Platform.OS === 'android' ? requireNativeModule(MODULE_NAME) : null;

// ---------------------------------------------------------------------------
// Sign-In
// ---------------------------------------------------------------------------

/**
 * Trigger a silent GPGS sign-in. Falls back to explicit sign-in if needed.
 * Resolves to `true` on success.
 */
export async function signIn(): Promise<boolean> {
  if (!NativeModule) return false;
  return NativeModule.signIn();
}

/**
 * Check whether the user is currently signed in to Play Games.
 */
export async function isSignedIn(): Promise<boolean> {
  if (!NativeModule) return false;
  return NativeModule.isSignedIn();
}

/**
 * Returns the Play Games player ID, or `null` if not signed in.
 */
export async function getPlayerId(): Promise<string | null> {
  if (!NativeModule) return null;
  return NativeModule.getPlayerId();
}

// ---------------------------------------------------------------------------
// Device Info
// ---------------------------------------------------------------------------

/**
 * Returns the timestamp (ms since epoch) when this app was first installed.
 * Uses Android `PackageManager.firstInstallTime`.
 * Returns `Date.now()` on non-Android platforms.
 */
export function getFirstInstallTime(): number {
  if (!NativeModule) return Date.now();
  return NativeModule.getFirstInstallTime();
}

// ---------------------------------------------------------------------------
// Saved Games (Snapshots)
// ---------------------------------------------------------------------------

/**
 * Save a string to a Play Games Saved Games snapshot.
 * @param name - Snapshot name (identifier)
 * @param data - String data to persist
 */
export async function saveSnapshot(name: string, data: string): Promise<boolean> {
  if (!NativeModule) return false;
  return NativeModule.saveSnapshot(name, data);
}

/**
 * Load data from a Play Games Saved Games snapshot.
 * @param name - Snapshot name (identifier)
 * @returns The stored string, or `null` if not found.
 */
export async function loadSnapshot(name: string): Promise<string | null> {
  if (!NativeModule) return null;
  return NativeModule.loadSnapshot(name);
}

// ---------------------------------------------------------------------------
// Achievements
// ---------------------------------------------------------------------------

/**
 * Unlock a standard (non-incremental) achievement by its Play Console ID.
 */
export async function unlockAchievement(achievementId: string): Promise<boolean> {
  if (!NativeModule) return false;
  return NativeModule.unlockAchievement(achievementId);
}

/**
 * Increment an incremental achievement by the given number of steps.
 */
export async function incrementAchievement(achievementId: string, steps: number): Promise<boolean> {
  if (!NativeModule) return false;
  return NativeModule.incrementAchievement(achievementId, steps);
}

/**
 * Show the native Play Games achievements overlay UI.
 */
export async function showAchievementsUI(): Promise<boolean> {
  if (!NativeModule) return false;
  return NativeModule.showAchievementsUI();
}

// ---------------------------------------------------------------------------
// Local Persistence (SharedPreferences)
// ---------------------------------------------------------------------------

/**
 * Save a boolean flag to Android SharedPreferences.
 * Persists across reinstalls via Android Auto Backup.
 */
export function saveLocalFlag(key: string, value: boolean): boolean {
  if (!NativeModule) return false;
  return NativeModule.saveLocalFlag(key, value);
}

/**
 * Load a boolean flag from Android SharedPreferences.
 */
export function loadLocalFlag(key: string): boolean {
  if (!NativeModule) return false;
  return NativeModule.loadLocalFlag(key);
}

/**
 * Save a string value to Android SharedPreferences.
 * Persists across reinstalls via Android Auto Backup.
 */
export function saveLocalString(key: string, value: string): boolean {
  if (!NativeModule) return false;
  return NativeModule.saveLocalString(key, value);
}

/**
 * Load a string value from Android SharedPreferences.
 * Returns `null` if not found or on non-Android platforms.
 */
export function loadLocalString(key: string): string | null {
  if (!NativeModule) return null;
  return NativeModule.loadLocalString(key);
}
