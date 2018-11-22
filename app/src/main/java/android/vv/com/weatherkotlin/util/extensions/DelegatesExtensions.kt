package android.vv.com.weatherkotlin.util.extensions

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.vv.com.weatherkotlin.data.ForecastResult
import android.vv.com.weatherkotlin.ui.MyApplication
import kotlin.reflect.KProperty

object DelegatesExt {
    fun <T> notNullSingleValue() = NotNullSingleValueVar<T>()
    fun <T> preference(name: String, default: T) = Preference( name, default)
    var forecastResult: ForecastResult? = null
}

class NotNullSingleValueVar<T> {

    private var value: T? = null

    operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return value ?: throw IllegalStateException("${property.name} not initialized")
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        this.value = if (this.value == null) value
        else throw IllegalStateException("${property.name} already initialized")
    }
}

class Preference<T>(val name: String, val default: T) {

    val prefs: SharedPreferences by lazy { MyApplication.instance.getDefaultSharePreference }


    operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return findPreference(name, default)
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        putPreference(name, value)
    }

    @Suppress("UNCHECKED_CAST")
    private fun findPreference(name: String, default: T): T = with(prefs) {
        val res: Any = when (default) {
            is Long -> getLong(name, default)
            is String -> getString(name, default)
            is Int -> getInt(name, default)
            is Boolean -> getBoolean(name, default)
            is Float -> getFloat(name, default)
            else -> throw IllegalArgumentException("This type can be saved into Preferences")
        }

        res as T
    }

    private fun putPreference(name: String, value: T) = with(prefs.edit()) {
        when (value) {
            is Long -> putLong(name, value)
            is String -> putString(name, value)
            is Int -> putInt(name, value)
            is Boolean -> putBoolean(name, value)
            is Float -> putFloat(name, value)
            else -> throw IllegalArgumentException("This type can't be saved into Preferences")
        }.apply()
    }
}

val Context.getDefaultSharePreference: SharedPreferences
    get() = PreferenceManager.getDefaultSharedPreferences(this)


