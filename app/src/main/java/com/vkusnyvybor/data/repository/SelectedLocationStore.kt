package com.vkusnyvybor.data.repository

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Локальное хранилище выбранной на карте локации (предприятия/кластера).
 *
 * Источник данных — микросервис карты (VV_Map_Service). Пользователь выбирает
 * точку во встроенной WebView-карте, и мы складываем сюда то, что вернул
 * фронт карты:
 *  - либо КЛАСТЕР целиком (clusterId + clusterName, без франшизы),
 *  - либо КОНКРЕТНОЕ предприятие франшизы (clusterId + franchiseId/franchiseName).
 *
 * Адрес и координаты — это адрес/центр КЛАСТЕРА (так отдаёт сервис в /select).
 * По образцу [AuthSessionStore]: SharedPreferences + StateFlow для реактивного
 * обновления адресной панели на главном экране.
 */
@Singleton
class SelectedLocationStore @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _location = MutableStateFlow(load())
    val location: StateFlow<SelectedLocation?> = _location.asStateFlow()

    fun save(location: SelectedLocation) {
        prefs.edit()
            .putInt(KEY_CLUSTER_ID, location.clusterId)
            .putString(KEY_CLUSTER_NAME, location.clusterName)
            .putString(KEY_ADDRESS, location.address)
            .putInt(KEY_FRANCHISE_ID, location.franchiseId ?: -1)
            .putString(KEY_FRANCHISE_NAME, location.franchiseName)
            .putFloat(KEY_LAT, location.latitude.toFloat())
            .putFloat(KEY_LON, location.longitude.toFloat())
            .apply()
        _location.value = location
    }

    fun clear() {
        prefs.edit().clear().apply()
        _location.value = null
    }

    private fun load(): SelectedLocation? {
        if (!prefs.contains(KEY_CLUSTER_ID)) return null
        val franchiseId = prefs.getInt(KEY_FRANCHISE_ID, -1).takeIf { it >= 0 }
        return SelectedLocation(
            clusterId = prefs.getInt(KEY_CLUSTER_ID, 0),
            clusterName = prefs.getString(KEY_CLUSTER_NAME, null),
            address = prefs.getString(KEY_ADDRESS, "") ?: "",
            franchiseId = franchiseId,
            franchiseName = prefs.getString(KEY_FRANCHISE_NAME, null),
            latitude = prefs.getFloat(KEY_LAT, 0f).toDouble(),
            longitude = prefs.getFloat(KEY_LON, 0f).toDouble()
        )
    }

    companion object {
        private const val PREFS_NAME = "vkusny_location_prefs"
        private const val KEY_CLUSTER_ID = "cluster_id"
        private const val KEY_CLUSTER_NAME = "cluster_name"
        private const val KEY_ADDRESS = "address"
        private const val KEY_FRANCHISE_ID = "franchise_id"
        private const val KEY_FRANCHISE_NAME = "franchise_name"
        private const val KEY_LAT = "latitude"
        private const val KEY_LON = "longitude"
    }
}

/**
 * Выбранная локация.
 *
 * @param clusterName  имя кластера — заполнено, если выбран весь кластер; для
 *                     выбора конкретной франшизы держим null (важна франшиза).
 * @param franchiseId  id франшизы — заполнен только при выборе конкретного
 *                     предприятия. null означает «выбран весь кластер».
 */
data class SelectedLocation(
    val clusterId: Int,
    val clusterName: String?,
    val address: String,
    val franchiseId: Int?,
    val franchiseName: String?,
    val latitude: Double,
    val longitude: Double
) {
    /** Заголовок для адресной панели: франшиза, если выбрана, иначе кластер. */
    val displayTitle: String
        get() = franchiseName?.takeIf { it.isNotBlank() }
            ?: clusterName?.takeIf { it.isNotBlank() }
            ?: address
}
