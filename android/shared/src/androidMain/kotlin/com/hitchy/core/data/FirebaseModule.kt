package com.hitchy.core.data

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.MemoryCacheSettings
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.storage.FirebaseStorage
import com.hitchy.core.model.AppConfig
import com.hitchy.core.model.Env

object FirebaseModule {
    private const val EMULATOR_HOST = "10.0.2.2"
    private const val AUTH_PORT = 9099
    private const val FIRESTORE_PORT = 8080
    private const val FUNCTIONS_PORT = 5001
    private const val STORAGE_PORT = 9199

    @Volatile
    private var initialized = false

    fun init(context: Context, config: AppConfig) {
        if (initialized) return
        synchronized(this) {
            if (initialized) return

            // Official SDK initializes from google-services.json
            if (FirebaseApp.getApps(context).isEmpty()) {
                FirebaseApp.initializeApp(context)
            }

            if (config.useEmulator) {
                // Connect emulators first in onCreate, before any other Firebase call, exactly once
                FirebaseAuth.getInstance().useEmulator(EMULATOR_HOST, AUTH_PORT)

                val firestore = FirebaseFirestore.getInstance()
                firestore.useEmulator(EMULATOR_HOST, FIRESTORE_PORT)
                firestore.firestoreSettings = FirebaseFirestoreSettings.Builder()
                    .setLocalCacheSettings(MemoryCacheSettings.newBuilder().build())
                    .build()

                FirebaseFunctions.getInstance(config.functionsRegion).useEmulator(EMULATOR_HOST, FUNCTIONS_PORT)
                FirebaseStorage.getInstance().useEmulator(EMULATOR_HOST, STORAGE_PORT)

                // Crashlytics: collection off when USE_EMULATOR
                FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(false)
            } else {
                // App Check: Play Integrity provider in prod, debug provider in dev
                val appCheck = FirebaseAppCheck.getInstance()
                when (config.env) {
                    Env.PROD -> appCheck.installAppCheckProviderFactory(
                        PlayIntegrityAppCheckProviderFactory.getInstance(),
                    )

                    Env.DEV -> appCheck.installAppCheckProviderFactory(
                        DebugAppCheckProviderFactory.getInstance(),
                    )
                }
            }

            initialized = true
        }
    }

    /**
     * Crashlytics: user ID = Firebase UID after sign-in.
     */
    fun setCrashlyticsUserId(uid: String?) {
        FirebaseCrashlytics.getInstance().setUserId(uid ?: "")
    }
}
