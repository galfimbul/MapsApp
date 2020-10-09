package ru.aevshvetsov.testproject.repository

import android.location.Address
import org.osmdroid.bonuspack.location.POI
import org.osmdroid.util.GeoPoint

/**
 * Created by Alexander Shvetsov on 09.10.2020
 */
interface IMainScreenRepository {
    fun getPointsOfInterestsInfo(startPoint: GeoPoint, placeType: String, onSuccess: (List<POI>) -> Unit)
    fun getPlaceInfoFromLocationName(searchQuery: String, maxResult: Int, onSuccess: (Address) -> Unit)
}