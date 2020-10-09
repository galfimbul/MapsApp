package ru.aevshvetsov.testproject.viewmodels

import android.location.Address
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.osmdroid.bonuspack.location.POI
import org.osmdroid.util.GeoPoint
import ru.aevshvetsov.testproject.repository.IMainScreenRepository

/**
 * Created by Alexander Shvetsov on 09.10.2020
 */
class MainScreenViewModel : ViewModel() {
    private val _pointsOfInterestsInfo = MutableLiveData<List<POI>>()
    private val _placeInfoFromLocationName = MutableLiveData<Address>()
    val pointsOfInterestsInfo: LiveData<List<POI>>
        get() = _pointsOfInterestsInfo
    val placeInfoFromLocationName: LiveData<Address>
        get() = _placeInfoFromLocationName

    private lateinit var repo: IMainScreenRepository
    fun setRepository(repository: IMainScreenRepository) {
        repo = repository
    }

    fun getPointsOfInterestsInfo(startPoint: GeoPoint, placeType: String) {
        repo.getPointsOfInterestsInfo(startPoint, placeType) { placesList ->
            _pointsOfInterestsInfo.value = placesList
        }
    }

    fun geoLocate(searchQuery: String, maxResult: Int) {
        repo.getPlaceInfoFromLocationName(searchQuery, maxResult) { searchingPlace ->
            _placeInfoFromLocationName.value = searchingPlace
        }
    }
}