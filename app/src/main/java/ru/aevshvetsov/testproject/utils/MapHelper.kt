package ru.aevshvetsov.testproject.utils

import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import org.osmdroid.bonuspack.location.GeocoderNominatim
import org.osmdroid.bonuspack.location.NominatimPOIProvider
import org.osmdroid.bonuspack.location.POI
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.FolderOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Overlay
import org.osmdroid.views.overlay.Polygon
import ru.aevshvetsov.testproject.R

/**
 * Created by Alexander Shvetsov on 07.10.2020
 */
class MapHelper(private val mapView: MapView) {
    private var markerInfoTitleColor: Int = 0

    fun setDefaultMapViewPoint(
        locationHelper: CurrentLocationHelper,
        defaultPointIcon: Drawable
    ) {
        val mapController = mapView.controller
        val defaultPoint = locationHelper.getLocation()
        mapController.animateTo(defaultPoint)
        mapController.setZoom(DEFAULT_ZOOM)
        Log.d("M_MapHelper", "my location is $defaultPoint")
        setStartMarker(defaultPoint, defaultPointIcon)
        Log.d("M_MapHelper", "")
    }

    private fun setStartMarker(startPoint: GeoPoint, markerIcon: Drawable) {
        Log.d("M_MapHelper", "setting start marker")
        val startMarker = Marker(mapView)
        startMarker.id = START_GEOPOINT_ID
        startMarker.title = START_GEOPOINT_TITLE
        startMarker.position = startPoint
        startMarker.icon = markerIcon
        val existingCircle: Overlay? = mapView.overlays.find {
            if (it is Marker) {
                return@find it.id == START_GEOPOINT_ID
            } else return@find false
        }
        if (existingCircle != null) {
            mapView.overlays.remove(existingCircle as Marker)
        }
        mapView.overlays.add(startMarker)
        mapView.invalidate()
    }

    fun drawCircle(center: GeoPoint, distanceInMeters: Double, color: Int) {
        val circlePoints = Polygon.pointsAsCircle(center, distanceInMeters)
        val circle = Polygon(mapView)
        circle.id = CIRCLE_500M_ID
        circle.points = circlePoints
        circle.outlinePaint.color = color
        mapView.overlays.add(0, circle)
        mapView.invalidate()
    }

    fun setMarkerInfoTitleColor(color: Int) {
        markerInfoTitleColor = color
    }

    fun showPointsOfInterest(startPoint: GeoPoint, poiIcon: Drawable, placeType: String) {
        val poiProvider = NominatimPOIProvider("OSMBonusPackTestProjectUserAgent")
        poiProvider.setService(NOMINATIM_SERVICE_URL)
        var pois: List<POI>

        Thread {
            val poisResponse = poiProvider.getPOICloseTo(startPoint, placeType, POI_MAX_RESULT, MAX_DISTANCE_POI_SEARCH)
            Log.d("M_MapHelper", "Background thread response")
            Log.d("M_MapHelper", "pois response is: $poisResponse")
            if (!poisResponse.isNullOrEmpty()) {
                Handler(Looper.getMainLooper()).post {
                    pois = poisResponse
                    pois.forEach { poi ->
                        val poiMarker = Marker(mapView)
                        poiMarker.title = poi.mType
                        poiMarker.snippet = poi.mDescription
                        poiMarker.position = poi.mLocation
                        poiMarker.icon = poiIcon
                        /*if (poi.mThumbnail != null){
                            poiItem.setImage(BitmapDrawable(poi.mThumbnail))
                        }*/
                        val poiMarkers = FolderOverlay()
                        poiMarkers.add(poiMarker)
                        mapView.overlays.add(poiMarkers)

                    }
                    mapView.invalidate()
                }
            } else {
                Log.d("M_MapHelper", "request")
                Toast.makeText(
                    mapView.context,
                    mapView.context.getString(R.string.points_of_interes_not_found_message),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }.start()
    }

    fun geoLocate(searchQuery: String, markerIcon: Drawable, result: () -> Unit) {
        val geoCoder = GeocoderNominatim("OSMBonusPackTestProjectUserAgent")
        geoCoder.setService(NOMINATIM_SERVICE_URL)
        Thread {
            val placesResult = geoCoder.getFromLocationName(searchQuery, 1)
            Handler(Looper.getMainLooper()).post {
                if (placesResult.size > 0) {
                    val searchingAddress = placesResult[0]
                    Log.d("M_MapHelper", "searching address is: $searchingAddress")
                    val geoPoint = GeoPoint(searchingAddress.latitude, searchingAddress.longitude)
                    setMarker(geoPoint, searchingAddress.extras.get("display_name") as String, markerIcon)
                    mapView.controller.animateTo(geoPoint)
                    mapView.controller.setZoom(PLACE_SEARCH_ZOOM)
                    result()

                }
            }
        }.start()
    }

    private fun setMarker(geoPoint: GeoPoint, displayName: String, markerIcon: Drawable) {
        val searchingMarker = Marker(mapView)
        with(searchingMarker) {
            title = displayName
            position = geoPoint
            icon = markerIcon
        }
        mapView.overlays.add(searchingMarker)
        mapView.invalidate()
    }
}