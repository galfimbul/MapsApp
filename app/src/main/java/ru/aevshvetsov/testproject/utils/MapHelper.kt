package ru.aevshvetsov.testproject.utils

import android.graphics.drawable.Drawable
import android.location.Address
import android.util.Log
import android.widget.Toast
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
    var isMapInitialized = false

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
        isMapInitialized = true
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

    fun showPointsOfInterest(pois: List<POI>, poiIcon: Drawable) {
        if (!pois.isNullOrEmpty()) {
            val existingPlaces: List<Overlay> = mapView.overlays.filter {
                if (it is FolderOverlay) {
                    it.name.isNotBlank()
                } else false
            }
            if (existingPlaces.isNotEmpty()) {
                mapView.overlays.removeAll(existingPlaces)
            }
            val foldersName = pois[0].mType
            pois.forEach { poi ->
                val poiMarker = Marker(mapView)
                poiMarker.title = poi.mType
                poiMarker.snippet = poi.mDescription
                poiMarker.position = poi.mLocation
                poiMarker.icon = poiIcon
                val poiMarkers = FolderOverlay()
                poiMarkers.name = foldersName
                poiMarkers.add(poiMarker)
                mapView.overlays.add(poiMarkers)
            }
            mapView.invalidate()
        } else {
            Log.d("M_MapHelper", "request failed")
            Toast.makeText(
                mapView.context,
                mapView.context.getString(R.string.points_of_interes_not_found_message),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    fun geoLocate(address: Address, markerIcon: Drawable, result: () -> Unit) {
        val geoPoint = GeoPoint(address.latitude, address.longitude)
        setMarker(geoPoint, address.extras.get("display_name") as String, markerIcon)
        mapView.controller.animateTo(geoPoint)
        mapView.controller.setZoom(PLACE_SEARCH_ZOOM)
        result()
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