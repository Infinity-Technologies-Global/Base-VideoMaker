package com.ynsuper.slideshowver1.remoteconfig

import com.google.android.gms.tasks.Task
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig

import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import kotlin.properties.Delegates
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

abstract class FirebaseRemoteEasy(private val devMode: Boolean) {

    fun fetch(onComplete: ((Task<Void>) -> Unit)? = null) {
        val fetchTask =
            if (devMode) frc.fetch(0) else frc.fetch() // The default value expiration duration is 12 hours
        if (onComplete != null) fetchTask.addOnCompleteListener(onComplete)
    }

    // Delegate methods

    protected fun string(defaultValue: String = "", key: String? = null) =
            FRCDelegate(defaultValue, key) { frc.getString(it) }

    protected fun boolean(defaultValue: Boolean = false, key: String? = null) =
            FRCDelegate(defaultValue, key) { frc.getBoolean(it) }

//    protected fun byteArray(defaultValue: ByteArray? = null, key: String? = null) =
//        FRCDelegate(defaultValue, key) { frc.getByteArray(it) }

    protected fun double(defaultValue: Double = 0.0, key: String? = null) =
            FRCDelegate(defaultValue, key) { frc.getDouble(it) }

    protected fun long(defaultValue: Long = 0L, key: String? = null) =
            FRCDelegate(defaultValue, key) { frc.getLong(it) }

    protected fun int(defaultValue: Int = 0, key: String? = null) =
            FRCDelegate(defaultValue, key) { frc.getLong(it).toInt() }

    override fun toString(): String =
        (_defaults + (frc.getKeysByPrefix("").map { it to frc.getString(it) }
            .toMap())).toSortedMap().toString()

    // Private

    private val _defaults = HashMap<String, Any?>()

    private val frc by lazy {
        val remoteConfig = Firebase.remoteConfig
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = if (devMode) {
                0
            } else {
                3600
            }
        }
        remoteConfig.apply {
            setConfigSettingsAsync(configSettings)
            remoteConfig.setDefaultsAsync(_defaults)
            fetchAndActivate()
            if (devMode) fetch(0) else fetch()
        }
    }

    protected class FRCDelegate<T>(
        private val defaultValue: T,
        private val keyFRC: String?,
        private val getMethod: (String) -> T
    ) : ReadOnlyProperty<FirebaseRemoteEasy, T> {
        var key: String by Delegates.notNull()

        operator fun provideDelegate(
            thisRef: FirebaseRemoteEasy,
            property: KProperty<*>
        ): FRCDelegate<T> {
            key = keyFRC ?: property.name
            thisRef._defaults[key] = defaultValue
            return this
        }

        override fun getValue(thisRef: FirebaseRemoteEasy, property: KProperty<*>) = getMethod(key)
    }
}