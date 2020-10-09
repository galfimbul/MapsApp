package ru.aevshvetsov.testproject.ui.fragments

import android.content.Context
import android.graphics.drawable.Drawable
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.android.synthetic.main.fragment_main_screen.*
import kotlinx.android.synthetic.main.maps_screen_bottom_sheet.*
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Overlay
import org.osmdroid.views.overlay.Polygon
import ru.aevshvetsov.testproject.R
import ru.aevshvetsov.testproject.repository.IMainScreenRepository
import ru.aevshvetsov.testproject.repository.MainScreenRepositoryImpl
import ru.aevshvetsov.testproject.utils.*
import ru.aevshvetsov.testproject.viewmodels.MainScreenViewModel

class MainScreen : Fragment(R.layout.fragment_main_screen), View.OnClickListener {
    lateinit var mapView: MapView
    lateinit var mapHelper: MapHelper
    lateinit var locationManager: LocationManager
    lateinit var locationHelper: CurrentLocationHelper
    lateinit var bottomSheetBehavior: BottomSheetBehavior<View>
    private lateinit var currentPlaceGeoPoint: GeoPoint
    private lateinit var myPositionPointIcon: Drawable
    lateinit var pointIcon: Drawable
    private var circleColor: Int = 0
    private var isCircleDrawn = false
    private lateinit var repo: IMainScreenRepository
    private val mainScreenViewModel by lazy {
        ViewModelProvider(this).get(MainScreenViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        repo = MainScreenRepositoryImpl()
        mainScreenViewModel.setRepository(repo)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bottomSheetBehavior = BottomSheetBehavior.from(maps_screen_bottom_sheet)
        val sharedPref = activity?.getSharedPreferences("TestProject", Context.MODE_PRIVATE)
        Configuration.getInstance().load(activity?.applicationContext, sharedPref)
        mapView = map
        mapHelper = MapHelper(mapView)
        locationManager = activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locationHelper = CurrentLocationHelper(locationManager)
        initMap()
        initViews()
        subscribeObservers()
    }

    private fun subscribeObservers() {
        mainScreenViewModel.placeInfoFromLocationName.observe(this) { address ->
            Log.d("M_MainScreen:subscribe", "called")
            if (address.countryName != GEOLOCATE_ERROR) {
                mapHelper.geoLocate(address, pointIcon) {
                    hideKeyboard()
                    et_bottom_sheet_search.setText("")
                    et_bottom_sheet_search.clearFocus()
                    clearSearchView()
                }
            } else {
                showToast(getString(R.string.main_screen_geolocate_failed_toast_message))
                hideKeyboard()
                clearSearchView()
            }
        }
        mainScreenViewModel.pointsOfInterestsInfo.observe(this) { pointsList ->
            mapHelper.showPointsOfInterest(
                pointsList, pointIcon
            )
            Log.d("M_MainScreen", "test")
        }
    }

    private fun clearSearchView() {
        et_bottom_sheet_search.setText("")
        et_bottom_sheet_search.clearFocus()
    }

    private fun initViews() {
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        iv_open_bottom_sheet.setOnClickListener {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
        iv_bottom_sheet_grocery.setOnClickListener(this)

        iv_bottom_sheet_pharmacy.setOnClickListener(this)
        iv_bottom_sheet_restaurants.setOnClickListener(this)
        iv_bottom_sheet_cafe.setOnClickListener(this)

        iv_draw_circle_500m.setOnClickListener {
            if (!isCircleDrawn) {
                currentPlaceGeoPoint = GeoPoint(mapView.mapCenter)
                val circleColor = resources.getColor(R.color.drawCircle, activity?.theme)
                mapHelper.drawCircle(currentPlaceGeoPoint, 500.0, circleColor)
                isCircleDrawn = true
            } else {
                val existingCircle: Overlay? = mapView.overlays.find {
                    if (it is Polygon) {
                        return@find it.id == CIRCLE_500M_ID
                    } else return@find false
                }
                if (existingCircle != null) {
                    mapView.overlays.remove(existingCircle as Polygon)
                    mapView.invalidate()
                    isCircleDrawn = false
                    if (existingCircle.bounds.centerLatitude / 10 != mapView.mapCenter.latitude / 10
                        || existingCircle.bounds.centerLongitude / 10 != mapView.mapCenter.longitude / 10
                    ) {
                        currentPlaceGeoPoint = GeoPoint(mapView.mapCenter)
                        mapHelper.drawCircle(currentPlaceGeoPoint, 500.0, circleColor)
                        isCircleDrawn = true
                    }
                }

            }
        }
        iv_show_current_location.setOnClickListener {
            mapHelper.setDefaultMapViewPoint(locationHelper, myPositionPointIcon)
        }
        iv_zoom_in.setOnClickListener { mapView.controller.zoomIn() }
        iv_zoom_out.setOnClickListener { mapView.controller.zoomOut() }

        et_bottom_sheet_search.setOnEditorActionListener { textView, actionId, keyEvent ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH
                || actionId == EditorInfo.IME_ACTION_DONE
                || keyEvent.action == KeyEvent.ACTION_DOWN
                || keyEvent.action == KeyEvent.KEYCODE_ENTER
            ) {
                mainScreenViewModel.geoLocate(textView.text.toString(), POI_MAX_RESULT)
            }
            return@setOnEditorActionListener true
        }

    }

    private fun initMap() {
        circleColor = resources.getColor(R.color.drawCircle, activity?.theme)
        currentPlaceGeoPoint = GeoPoint(mapView.mapCenter)
        myPositionPointIcon =
            resources.getDrawable(R.drawable.ic_start_marker, activity?.theme)
        pointIcon = resources.getDrawable(R.drawable.ic_map_marker, activity?.theme)
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        Log.d("M_MainScreen", "map initialized")
        mapView.zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
        mapView.setMultiTouchControls(true)
        mapHelper.setMarkerInfoTitleColor(resources.getColor(R.color.drawCircle, activity?.theme))
        mapHelper
            .setDefaultMapViewPoint(locationHelper, myPositionPointIcon)
    }


    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    companion object {
        fun newInstance() = MainScreen()
    }

    override fun onClick(view: View?) {
        val placeType = when (view?.id) {
            R.id.iv_bottom_sheet_grocery -> GROCERY_PLACE_TYPE
            R.id.iv_bottom_sheet_pharmacy -> PHARMACY_PLACE_TYPE
            R.id.iv_bottom_sheet_cafe -> CAFE_PLACE_TYPE
            R.id.iv_bottom_sheet_restaurants -> RESTAURANT_PLACE_TYPE
            else -> throw Exception(getString(R.string.iv_points_of_interests_error_message))
        }
        currentPlaceGeoPoint = GeoPoint(mapView.mapCenter)
        mainScreenViewModel.getPointsOfInterestsInfo(currentPlaceGeoPoint, placeType)
    }
}
