package ru.aevshvetsov.testproject.repository

import android.location.Address
import android.os.Handler
import android.os.Looper
import android.util.Log
import org.osmdroid.bonuspack.location.GeocoderNominatim
import org.osmdroid.bonuspack.location.NominatimPOIProvider
import org.osmdroid.bonuspack.location.POI
import org.osmdroid.util.GeoPoint
import ru.aevshvetsov.testproject.utils.GEOLOCATE_ERROR
import ru.aevshvetsov.testproject.utils.MAX_DISTANCE_POI_SEARCH
import ru.aevshvetsov.testproject.utils.NOMINATIM_SERVICE_URL
import ru.aevshvetsov.testproject.utils.POI_MAX_RESULT
import java.util.*

/**
 * Created by Alexander Shvetsov on 09.10.2020
 */
class MainScreenRepositoryImpl : IMainScreenRepository {
    private val poiProvider = NominatimPOIProvider("OSMBonusPackTestProjectUserAgent")

    init {
        poiProvider.setService(NOMINATIM_SERVICE_URL)
    }

    override fun getPointsOfInterestsInfo(startPoint: GeoPoint, placeType: String, onSuccess: (List<POI>) -> Unit) {
        var pois: List<POI>
        Thread {
            val poisResponse = poiProvider.getPOICloseTo(startPoint, placeType, POI_MAX_RESULT, MAX_DISTANCE_POI_SEARCH)
            Log.d("M_MainScreenRepoImpl", "Background thread response")
            Log.d("M_MainScreenRepoImpl", "pois response is: $poisResponse")
            if (!poisResponse.isNullOrEmpty()) {
                Handler(Looper.getMainLooper()).post {
                    pois = poisResponse
                    onSuccess(pois)
                }
            } else {
                Log.d("M_MainScreenRepoImpl", "request")
                Handler(Looper.getMainLooper()).post {
                    onSuccess(emptyList())
                }
            }
        }.start()

    }

    override fun getPlaceInfoFromLocationName(
        searchQuery: String, maxResult: Int, onSuccess: (Address) -> Unit
    ) {
        val geoCoder = GeocoderNominatim("OSMBonusPackTestProjectUserAgent")
        geoCoder.setService(NOMINATIM_SERVICE_URL)
        Thread {
            val placesResult = geoCoder.getFromLocationName(searchQuery, 1)
            Handler(Looper.getMainLooper()).post {
                if (placesResult.size > 0) {
                    val searchingAddress = placesResult[0]
                    Log.d("M_MapHelper", "searching address is: $searchingAddress")
                    //val geoPoint = GeoPoint(searchingAddress.latitude, searchingAddress.longitude)
                    onSuccess(searchingAddress)
                } else {
                    val emptyAddress = Address(Locale("ru"))
                    emptyAddress.countryName = GEOLOCATE_ERROR
                    onSuccess(emptyAddress)
                }
            }
        }.start()

    }
}