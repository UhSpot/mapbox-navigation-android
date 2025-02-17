package com.uhspot.navigation.ui.core

import android.annotation.SuppressLint
import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.mapbox.android.core.location.LocationEngineCallback
import com.mapbox.android.core.location.LocationEngineResult
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.EdgeInsets
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.plugin.LocationPuck2D
import com.mapbox.maps.plugin.animation.MapAnimationOptions
import com.mapbox.maps.plugin.animation.camera
import com.mapbox.maps.plugin.delegates.listeners.OnMapLoadErrorListener
import com.mapbox.maps.plugin.delegates.listeners.eventdata.MapLoadErrorType
import com.mapbox.maps.plugin.locationcomponent.LocationComponentPlugin
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.navigation.base.options.NavigationOptions
import com.mapbox.navigation.core.MapboxNavigation
import com.mapbox.navigation.core.replay.MapboxReplayer
import com.mapbox.navigation.core.replay.ReplayLocationEngine
import com.mapbox.navigation.core.replay.history.ReplayEventBase
import com.mapbox.navigation.core.replay.history.ReplayEventsObserver
import com.mapbox.navigation.core.replay.history.ReplaySetRoute
import com.mapbox.navigation.core.replay.route.ReplayProgressObserver
import com.mapbox.navigation.core.trip.session.LocationObserver
import com.uhspot.navigation.ui.core.databinding.ActivityReplayHistoryLayoutBinding
import com.uhspot.navigation.ui.core.replay.HistoryFileLoader
import com.uhspot.navigation.ui.core.replay.HistoryFilesActivity
import com.uhspot.navigation.ui.util.Utils
import com.mapbox.navigation.ui.maps.camera.NavigationCamera
import com.mapbox.navigation.ui.maps.camera.data.MapboxNavigationViewportDataSource
import com.mapbox.navigation.ui.maps.location.NavigationLocationProvider
import com.mapbox.navigation.ui.utils.internal.ifNonNull
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference
import java.util.Collections

class ReplayHistoryActivity : AppCompatActivity() {

    private var loadNavigationJob: Job? = null
    private val mapboxReplayer = MapboxReplayer()
    private val historyFileLoader = HistoryFileLoader()
    private val replayProgressObserver = ReplayProgressObserver(mapboxReplayer)
    private val navigationLocationProvider = NavigationLocationProvider()
    private val locationEngineCallback = MyLocationEngineCallback(this)
    private lateinit var mapboxNavigation: MapboxNavigation
    private lateinit var locationComponent: LocationComponentPlugin
    private lateinit var navigationCamera: NavigationCamera
    private lateinit var viewportDataSource: MapboxNavigationViewportDataSource
    private lateinit var binding: ActivityReplayHistoryLayoutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReplayHistoryLayoutBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        handleHistoryFileSelected()
        initNavigation()
        initMapStyle()

        findViewById<Button>(R.id.selectHistoryButton).setOnClickListener {
            val activityIntent = Intent(this, HistoryFilesActivity::class.java)
            startActivityForResult(activityIntent, HistoryFilesActivity.REQUEST_CODE)
        }
        setupReplayControls()
    }

    override fun onStart() {
        super.onStart()

        binding.mapView.onStart()
        if (::mapboxNavigation.isInitialized) {
            mapboxNavigation.registerLocationObserver(locationObserver)
            mapboxNavigation.registerRouteProgressObserver(replayProgressObserver)
        }
    }

    override fun onStop() {
        super.onStop()
        if (::mapboxNavigation.isInitialized) {
            mapboxNavigation.unregisterLocationObserver(locationObserver)
            mapboxNavigation.unregisterRouteProgressObserver(replayProgressObserver)
        }
        binding.mapView.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        findViewById<MapView>(R.id.mapView).onDestroy()
        mapboxNavigation.onDestroy()
    }

    @SuppressLint("MissingPermission")
    private fun initMapStyle() {
        viewportDataSource = MapboxNavigationViewportDataSource(
            binding.mapView.getMapboxMap()
        )
        binding.mapView.getMapboxMap().loadStyleUri(
            Style.MAPBOX_STREETS,
            { style: Style ->
                mapboxNavigation.navigationOptions.locationEngine.getLastLocation(
                    locationEngineCallback
                )
                locationComponent = binding.mapView.location.apply {
                    this.locationPuck = LocationPuck2D(
                        bearingImage = ContextCompat.getDrawable(
                            this@ReplayHistoryActivity,
                            R.drawable.mapbox_navigation_puck_icon
                        )
                    )
                    setLocationProvider(navigationLocationProvider)
                    enabled = true
                }
                navigationCamera = NavigationCamera(
                    binding.mapView.getMapboxMap(),
                    binding.mapView.camera,
                    viewportDataSource
                )

                viewportDataSource.evaluate()
            },
            object : OnMapLoadErrorListener {
                override fun onMapLoadError(mapLoadErrorType: MapLoadErrorType, msg: String) {
                    // intentionally blank
                }
            }
        )
    }

    private val locationObserver = object : LocationObserver {
        override fun onRawLocationChanged(rawLocation: Location) {}
        override fun onEnhancedLocationChanged(
            enhancedLocation: Location,
            keyPoints: List<Location>
        ) {
            navigationLocationProvider.changePosition(
                enhancedLocation,
                keyPoints,
            )
            updateCamera(enhancedLocation)
        }
    }

    private fun updateCamera(location: Location) {
        val mapAnimationOptionsBuilder = MapAnimationOptions.Builder()
            .duration(1500L)
        binding.mapView.camera.easeTo(
            CameraOptions.Builder()
                .center(Point.fromLngLat(location.longitude, location.latitude))
                .bearing(location.bearing.toDouble())
                .pitch(45.0)
                .zoom(17.0)
                .padding(EdgeInsets(1000.0, 0.0, 0.0, 0.0))
                .build(),
            mapAnimationOptionsBuilder.build()
        )
    }

    @SuppressLint("MissingPermission")
    private fun initNavigation() {
        mapboxNavigation = MapboxNavigation(
            NavigationOptions.Builder(this)
                .accessToken(Utils.getMapboxAccessToken(this))
                .locationEngine(ReplayLocationEngine(mapboxReplayer))
                .build()
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == HistoryFilesActivity.REQUEST_CODE) {
            handleHistoryFileSelected()
        }
    }

    @SuppressLint("MissingPermission")
    private fun handleHistoryFileSelected() {
        loadNavigationJob = CoroutineScope(Dispatchers.Main).launch {
            val events = historyFileLoader
                .loadReplayHistory(this@ReplayHistoryActivity)
            mapboxReplayer.clearEvents()
            mapboxReplayer.pushEvents(events)
            binding.playReplay.visibility = View.VISIBLE
            mapboxNavigation.resetTripSession()
            mapboxReplayer.playFirstLocation()
            mapboxNavigation.navigationOptions.locationEngine.getLastLocation(
                locationEngineCallback
            )
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateReplayStatus(playbackEvents: List<ReplayEventBase>) {
        playbackEvents.lastOrNull()?.eventTimestamp?.let {
            val currentSecond = mapboxReplayer.eventSeconds(it).toInt()
            val durationSecond = mapboxReplayer.durationSeconds().toInt()
            binding.playerStatus.text = "$currentSecond:$durationSecond"
        }
    }

    @SuppressLint("MissingPermission")
    private fun setupReplayControls() {
        binding.seekBar.max = 8
        binding.seekBar.progress = 1
        binding.seekBarText.text = getString(
            R.string.replay_playback_speed_seekbar,
            binding.seekBar.progress
        )
        binding.seekBar.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                    mapboxReplayer.playbackSpeed(progress.toDouble())
                    binding.seekBarText.text = getString(
                        R.string.replay_playback_speed_seekbar,
                        progress
                    )
                }

                override fun onStartTrackingTouch(seekBar: SeekBar) {}
                override fun onStopTrackingTouch(seekBar: SeekBar) {}
            }
        )

        binding.playReplay.setOnClickListener {
            mapboxReplayer.play()
            mapboxNavigation.startTripSession()
            binding.playReplay.visibility = View.GONE
        }

        mapboxReplayer.registerObserver(
            object : ReplayEventsObserver {
                override fun replayEvents(events: List<ReplayEventBase>) {
                    updateReplayStatus(events)
                    events.forEach {
                        when (it) {
                            is ReplaySetRoute -> setRoute(it)
                        }
                    }
                }
            }
        )
    }

    private fun setRoute(replaySetRoute: ReplaySetRoute) {
        replaySetRoute.route?.let { directionRoute ->
            mapboxNavigation.setRoutes(Collections.singletonList(directionRoute))
        }
    }

    private class MyLocationEngineCallback constructor(
        activity: ReplayHistoryActivity
    ) : LocationEngineCallback<LocationEngineResult> {

        private val activityRef: WeakReference<ReplayHistoryActivity> = WeakReference(activity)

        override fun onSuccess(result: LocationEngineResult) {
            ifNonNull(result.lastLocation, activityRef.get()) { loc, act ->
                val point = Point.fromLngLat(loc.longitude, loc.latitude)
                val cameraOptions = CameraOptions.Builder()
                    .center(point)
                    .zoom(13.0)
                    .build()
                act.binding.mapView.getMapboxMap().setCamera(cameraOptions)
                act.navigationLocationProvider.changePosition(loc)
                act.updateCamera(loc)
            }
        }

        override fun onFailure(exception: Exception) {}
    }
}
