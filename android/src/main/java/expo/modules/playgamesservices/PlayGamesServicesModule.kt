package expo.modules.playgamesservices

import android.app.Activity
import android.app.backup.BackupManager
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import expo.modules.kotlin.modules.Module
import expo.modules.kotlin.modules.ModuleDefinition
import expo.modules.kotlin.Promise
import com.google.android.gms.games.GamesSignInClient
import com.google.android.gms.games.PlayGames
import com.google.android.gms.games.PlayGamesSdk
import com.google.android.gms.games.SnapshotsClient
import com.google.android.gms.games.snapshot.SnapshotMetadataChange
import com.google.android.play.core.review.ReviewManagerFactory

class PlayGamesServicesModule : Module() {

  private var initialized = false

  companion object {
    private const val TAG = "PlayGamesServices"
    private const val PREFS_NAME = "expo_play_games_flags"
  }

  override fun definition() = ModuleDefinition {
    Name("PlayGamesServices")

    // -----------------------------------------------------------------------
    // Local Persistence (SharedPreferences)
    // -----------------------------------------------------------------------

    Function("saveLocalFlag") { key: String, value: Boolean ->
      val context = appContext.reactContext ?: run {
        Log.w(TAG, "saveLocalFlag: no context")
        return@Function false
      }
      try {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(key, value).apply()
        Log.d(TAG, "saveLocalFlag: $key = $value")
        try { BackupManager.dataChanged(context.packageName) } catch (_: Exception) {}
        true
      } catch (e: Exception) {
        Log.e(TAG, "saveLocalFlag error: ${e.message}")
        false
      }
    }

    Function("loadLocalFlag") { key: String ->
      val context = appContext.reactContext ?: run {
        Log.w(TAG, "loadLocalFlag: no context")
        return@Function false
      }
      try {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val value = prefs.getBoolean(key, false)
        Log.d(TAG, "loadLocalFlag: $key = $value")
        value
      } catch (e: Exception) {
        Log.e(TAG, "loadLocalFlag error: ${e.message}")
        false
      }
    }

    Function("saveLocalString") { key: String, value: String ->
      val context = appContext.reactContext ?: run {
        Log.w(TAG, "saveLocalString: no context")
        return@Function false
      }
      try {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(key, value).apply()
        Log.d(TAG, "saveLocalString: $key = $value")
        try { BackupManager.dataChanged(context.packageName) } catch (_: Exception) {}
        true
      } catch (e: Exception) {
        Log.e(TAG, "saveLocalString error: ${e.message}")
        false
      }
    }

    Function("loadLocalString") { key: String ->
      val context = appContext.reactContext ?: run {
        Log.w(TAG, "loadLocalString: no context")
        return@Function null as String?
      }
      try {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val value = prefs.getString(key, null)
        Log.d(TAG, "loadLocalString: $key = $value")
        value
      } catch (e: Exception) {
        Log.e(TAG, "loadLocalString error: ${e.message}")
        null as String?
      }
    }

    // -----------------------------------------------------------------------
    // Sign-In
    // -----------------------------------------------------------------------

    AsyncFunction("signIn") { promise: Promise ->
      val activity = currentActivity ?: run {
        Log.w(TAG, "signIn: no activity")
        promise.resolve(false)
        return@AsyncFunction
      }

      try {
        if (!initialized) {
          PlayGamesSdk.initialize(activity)
          initialized = true
          Log.d(TAG, "PlayGamesSdk initialized")
        }

        val gamesSignInClient: GamesSignInClient = PlayGames.getGamesSignInClient(activity)
        gamesSignInClient.isAuthenticated.addOnCompleteListener { isAuthTask ->
          if (isAuthTask.isSuccessful && isAuthTask.result.isAuthenticated) {
            Log.d(TAG, "signIn: already authenticated")
            promise.resolve(true)
          } else {
            Log.d(TAG, "signIn: not authenticated, trying explicit sign-in. isAuthTask success=${isAuthTask.isSuccessful}, exception=${isAuthTask.exception?.message}")
            gamesSignInClient.signIn().addOnCompleteListener { signInTask ->
              Log.d(TAG, "signIn: explicit result=${signInTask.isSuccessful}, exception=${signInTask.exception?.message}")
              promise.resolve(signInTask.isSuccessful)
            }
          }
        }
      } catch (e: Exception) {
        Log.e(TAG, "signIn error: ${e.message}")
        promise.resolve(false)
      }
    }

    AsyncFunction("isSignedIn") { promise: Promise ->
      val activity = currentActivity ?: run {
        promise.resolve(false)
        return@AsyncFunction
      }

      try {
        val gamesSignInClient = PlayGames.getGamesSignInClient(activity)
        gamesSignInClient.isAuthenticated.addOnCompleteListener { task ->
          promise.resolve(task.isSuccessful && task.result.isAuthenticated)
        }
      } catch (e: Exception) {
        promise.resolve(false)
      }
    }

    AsyncFunction("getPlayerId") { promise: Promise ->
      val activity = currentActivity ?: run {
        promise.resolve(null)
        return@AsyncFunction
      }

      try {
        val playersClient = PlayGames.getPlayersClient(activity)
        playersClient.currentPlayer.addOnCompleteListener { task ->
          if (task.isSuccessful) {
            promise.resolve(task.result.playerId)
          } else {
            promise.resolve(null)
          }
        }
      } catch (e: Exception) {
        promise.resolve(null)
      }
    }

    // -----------------------------------------------------------------------
    // Device Info
    // -----------------------------------------------------------------------

    Function("getFirstInstallTime") {
      val context = appContext.reactContext ?: return@Function 0.0
      try {
        val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        packageInfo.firstInstallTime.toDouble()
      } catch (e: PackageManager.NameNotFoundException) {
        0.0
      }
    }

    // -----------------------------------------------------------------------
    // Achievements
    // -----------------------------------------------------------------------

    AsyncFunction("unlockAchievement") { achievementId: String, promise: Promise ->
      val activity = currentActivity ?: run {
        promise.resolve(false)
        return@AsyncFunction
      }

      try {
        val achievementsClient = PlayGames.getAchievementsClient(activity)
        achievementsClient.unlock(achievementId)
        promise.resolve(true)
      } catch (e: Exception) {
        promise.resolve(false)
      }
    }

    AsyncFunction("incrementAchievement") { achievementId: String, steps: Int, promise: Promise ->
      val activity = currentActivity ?: run {
        promise.resolve(false)
        return@AsyncFunction
      }

      try {
        val achievementsClient = PlayGames.getAchievementsClient(activity)
        achievementsClient.increment(achievementId, steps)
        promise.resolve(true)
      } catch (e: Exception) {
        promise.resolve(false)
      }
    }

    AsyncFunction("showAchievementsUI") { promise: Promise ->
      val activity = currentActivity ?: run {
        promise.resolve(false)
        return@AsyncFunction
      }

      try {
        val achievementsClient = PlayGames.getAchievementsClient(activity)
        achievementsClient.achievementsIntent.addOnCompleteListener { task ->
          if (task.isSuccessful) {
            activity.startActivityForResult(task.result, 9003)
            promise.resolve(true)
          } else {
            promise.resolve(false)
          }
        }
      } catch (e: Exception) {
        promise.resolve(false)
      }
    }

    // -----------------------------------------------------------------------
    // Saved Games (Snapshots)
    // -----------------------------------------------------------------------

    AsyncFunction("saveSnapshot") { name: String, data: String, promise: Promise ->
      Log.d(TAG, "saveSnapshot: name=$name")
      val activity = currentActivity ?: run {
        Log.w(TAG, "saveSnapshot: no activity")
        promise.resolve(false)
        return@AsyncFunction
      }

      try {
        val snapshotsClient = PlayGames.getSnapshotsClient(activity)
        snapshotsClient.open(name, true, SnapshotsClient.RESOLUTION_POLICY_MOST_RECENTLY_MODIFIED)
          .addOnCompleteListener { task ->
            if (!task.isSuccessful) {
              Log.e(TAG, "saveSnapshot: open failed, exception=${task.exception?.message}")
              promise.resolve(false)
              return@addOnCompleteListener
            }

            val dataOrConflict = task.result
            val snapshot = if (dataOrConflict.isConflict) {
              // On conflict, resolve by keeping the server version and writing new data into it
              val conflict = dataOrConflict.conflict
              val resolved = conflict?.snapshot ?: conflict?.conflictingSnapshot
              if (conflict != null && resolved != null) {
                try {
                  snapshotsClient.resolveConflict(conflict.conflictId, resolved)
                } catch (e: Exception) {
                  Log.w(TAG, "saveSnapshot: conflict resolution failed: ${e.message}")
                }
              }
              resolved
            } else {
              dataOrConflict.data
            }

            if (snapshot == null) {
              promise.resolve(false)
              return@addOnCompleteListener
            }

            snapshot.snapshotContents.writeBytes(data.toByteArray(Charsets.UTF_8))

            val metadata = SnapshotMetadataChange.Builder()
              .setDescription("Saved game data")
              .build()

            snapshotsClient.commitAndClose(snapshot, metadata)
              .addOnCompleteListener { commitTask ->
                Log.d(TAG, "saveSnapshot: commit result=${commitTask.isSuccessful}, exception=${commitTask.exception?.message}")
                promise.resolve(commitTask.isSuccessful)
              }
          }
      } catch (e: Exception) {
        Log.e(TAG, "saveSnapshot error: ${e.message}")
        promise.resolve(false)
      }
    }

    AsyncFunction("loadSnapshot") { name: String, promise: Promise ->
      Log.d(TAG, "loadSnapshot: name=$name")
      val activity = currentActivity ?: run {
        Log.w(TAG, "loadSnapshot: no activity")
        promise.resolve(null)
        return@AsyncFunction
      }

      try {
        val snapshotsClient = PlayGames.getSnapshotsClient(activity)
        snapshotsClient.open(name, false, SnapshotsClient.RESOLUTION_POLICY_MOST_RECENTLY_MODIFIED)
          .addOnCompleteListener { task ->
            if (!task.isSuccessful) {
              Log.e(TAG, "loadSnapshot: open failed, exception=${task.exception?.message}")
              promise.resolve(null)
              return@addOnCompleteListener
            }

            val dataOrConflict = task.result
            val snapshot = if (dataOrConflict.isConflict) {
              dataOrConflict.conflict?.snapshot
            } else {
              dataOrConflict.data
            }

            if (snapshot == null) {
              promise.resolve(null)
              return@addOnCompleteListener
            }

            try {
              val contents = snapshot.snapshotContents.readFully()
              val result = String(contents, Charsets.UTF_8)
              snapshotsClient.discardAndClose(snapshot)
              Log.d(TAG, "loadSnapshot: success")
              promise.resolve(result)
            } catch (e: Exception) {
              Log.e(TAG, "loadSnapshot: read error: ${e.message}")
              promise.resolve(null)
            }
          }
      } catch (e: Exception) {
        Log.e(TAG, "loadSnapshot error: ${e.message}")
        promise.resolve(null)
      }
    }

    // -----------------------------------------------------------------------
    // In-App Review
    // -----------------------------------------------------------------------

    AsyncFunction("requestInAppReview") { promise: Promise ->
      val activity = currentActivity ?: run {
        Log.w(TAG, "requestInAppReview: no activity")
        promise.resolve(false)
        return@AsyncFunction
      }

      try {
        val reviewManager = ReviewManagerFactory.create(activity)
        reviewManager.requestReviewFlow().addOnCompleteListener { requestTask ->
          if (!requestTask.isSuccessful) {
            Log.e(TAG, "requestInAppReview: requestReviewFlow failed: ${requestTask.exception?.message}")
            promise.resolve(false)
            return@addOnCompleteListener
          }
          val reviewInfo = requestTask.result
          reviewManager.launchReviewFlow(activity, reviewInfo).addOnCompleteListener { launchTask ->
            Log.d(TAG, "requestInAppReview: launch complete, isSuccessful=${launchTask.isSuccessful}")
            promise.resolve(launchTask.isSuccessful)
          }
        }
      } catch (e: Exception) {
        Log.e(TAG, "requestInAppReview error: ${e.message}")
        promise.resolve(false)
      }
    }
  }

  private val currentActivity: Activity?
    get() = appContext.currentActivity
}
