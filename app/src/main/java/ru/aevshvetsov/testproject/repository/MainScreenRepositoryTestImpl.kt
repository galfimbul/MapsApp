package ru.aevshvetsov.testproject.repository

import android.location.Address
import org.osmdroid.bonuspack.location.POI
import org.osmdroid.util.GeoPoint

/**
 * Created by Alexander Shvetsov on 09.10.2020
 */
class MainScreenRepositoryTestImpl : IMainScreenRepository {


    override fun getPointsOfInterestsInfo(startPoint: GeoPoint, placeType: String, onSuccess: (List<POI>) -> Unit) {
        val poi1 = POI(1)

        val poi2 = POI(1)
        val placesList = listOf(poi1, poi2)
        onSuccess(placesList)

    }

    override fun getPlaceInfoFromLocationName(
        searchQuery: String, maxResult: Int, onSuccess: (Address) -> Unit
    ) {

    }
}