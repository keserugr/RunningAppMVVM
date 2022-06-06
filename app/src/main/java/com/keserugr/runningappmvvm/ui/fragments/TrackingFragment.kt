package com.keserugr.runningappmvvm.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.keserugr.runningappmvvm.R
import com.keserugr.runningappmvvm.databinding.FragmentTrackingBinding
import com.keserugr.runningappmvvm.db.Run
import com.keserugr.runningappmvvm.services.Polyline
import com.keserugr.runningappmvvm.services.TrackingService
import com.keserugr.runningappmvvm.ui.viewmodel.MainViewModel
import com.keserugr.runningappmvvm.util.Constants.ACTION_PAUSE_SERVICE
import com.keserugr.runningappmvvm.util.Constants.ACTION_START_OR_RESUME_SERVICE
import com.keserugr.runningappmvvm.util.Constants.ACTION_STOP_SERVICE
import com.keserugr.runningappmvvm.util.Constants.MAP_ZOOM
import com.keserugr.runningappmvvm.util.Constants.POLYLINE_COLOR
import com.keserugr.runningappmvvm.util.Constants.POLYLINE_WIDTH
import com.keserugr.runningappmvvm.util.TrackingUtility
import com.predicomm.coffee.util.preferences.IMySharedPreferences
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import javax.inject.Inject
import kotlin.math.round

const val CANCEL_TRACKING_DIALOG_TAG = "CancelDialog"

@AndroidEntryPoint
class TrackingFragment : Fragment(R.layout.fragment_tracking) {

    private val viewModel: MainViewModel by viewModels()

    @Inject
    lateinit var sharedPreferences: IMySharedPreferences

    private var isTracking = false
    private var pathPoints = mutableListOf<Polyline>()

    private var fragmentBinding: FragmentTrackingBinding? = null

    private var map: GoogleMap? = null

    private var currentTimeInMillis = 0L

    private var menu: Menu? = null

    var weight = 76f

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentTrackingBinding.bind(view)
        fragmentBinding = binding

        sharedPreferences.getUserDetail()?.let {
            weight = it.weight
        }

        binding.mapView.onCreate(savedInstanceState)

        binding.mapView.getMapAsync {
            map = it
            addAllPolylines()
        }

        binding.btnToggleRun.setOnClickListener {
            toggleRun()
        }

        if (savedInstanceState != null){
            val cancelTrackingDiaolog = parentFragmentManager.findFragmentByTag(
                CANCEL_TRACKING_DIALOG_TAG) as CancelTrackingDialog?
            cancelTrackingDiaolog?.setYesListener {
                stopRun()
            }
        }

        fragmentBinding!!.btnFinishRun.setOnClickListener {
            zoomToSeeWholeTrack()
            endRunAndSaveToDb()
        }

        subscribeToObservers()
    }

    private fun subscribeToObservers() {
        TrackingService.isTracking.observe(viewLifecycleOwner, Observer {
            updateTracking(it)
        })

        TrackingService.pathPoints.observe(viewLifecycleOwner, Observer {
            pathPoints = it
            addLatestPolyline()
            moveCameraToUser()
        })

        TrackingService.timeRunInMillis.observe(viewLifecycleOwner, Observer {
            currentTimeInMillis = it
            val formattedTime = TrackingUtility.getFormattedStopWatchTime(currentTimeInMillis, true)
            fragmentBinding!!.tvTimer.text = formattedTime
        })
    }

    private fun toggleRun() {
        if (isTracking) {
            menu?.getItem(0)?.isVisible = true
            sendCommandToService(ACTION_PAUSE_SERVICE)
        } else {
            sendCommandToService(ACTION_START_OR_RESUME_SERVICE)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.toolbar_tracking_menu, menu)
        this.menu = menu
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        if (currentTimeInMillis > 0L) {
            this.menu?.getItem(0)?.isVisible = true
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.miCancelTracking -> {
                showCancelTrackingDialog()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showCancelTrackingDialog() {
        CancelTrackingDialog().apply {
            setYesListener{
                stopRun()
            }
        }.show(parentFragmentManager, CANCEL_TRACKING_DIALOG_TAG)
    }

    private fun stopRun() {
        fragmentBinding!!.tvTimer.text = "00:00:00:00"
        sendCommandToService(ACTION_STOP_SERVICE)
        findNavController().navigate(R.id.action_trackingFragment_to_runFragment)
    }

    private fun updateTracking(isTracking: Boolean) {
        this.isTracking = isTracking
        if (!isTracking && currentTimeInMillis > 0L) {
            fragmentBinding!!.btnToggleRun.text = "Start"
            fragmentBinding!!.btnFinishRun.visibility = View.VISIBLE
        } else if(isTracking){
            fragmentBinding!!.btnToggleRun.text = "Stop"
            menu?.getItem(0)?.isVisible = true
            fragmentBinding!!.btnFinishRun.visibility = View.GONE
        }
    }

    // move the camera with User last location
    private fun moveCameraToUser() {
        if (pathPoints.isNotEmpty() && pathPoints.last().isNotEmpty()) {
            map?.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    pathPoints.last().last(),
                    MAP_ZOOM
                )
            )
        }
    }

    private fun zoomToSeeWholeTrack() {
        val bounds = LatLngBounds.Builder()
        for (polyline in pathPoints) {
            for (pos in polyline) {
                bounds.include(pos)
            }
        }

        map?.moveCamera(
            CameraUpdateFactory.newLatLngBounds(
                bounds.build(),
                fragmentBinding!!.mapView.width,
                fragmentBinding!!.mapView.height,
                (fragmentBinding!!.mapView.height * 0.05f).toInt()
            )
        )
    }

    private fun endRunAndSaveToDb() {
        map?.snapshot { bitmap ->
            var distanceInMeters = 0
            for (polyline in pathPoints){
                distanceInMeters += TrackingUtility.calculatePolyLineLength(polyline).toInt()
            }
            val avgSpeed = round((distanceInMeters / 1000f) / (currentTimeInMillis / 1000f / 60 / 60) * 10) /10f
            val dateTimestamp = Calendar.getInstance().timeInMillis
            val caloriesBurned = ((distanceInMeters / 1000f) * weight).toInt()
            val run = Run(bitmap, dateTimestamp, avgSpeed, distanceInMeters, currentTimeInMillis, caloriesBurned)
            viewModel.insertRun(run)
            Snackbar.make(
                requireActivity().findViewById(R.id.rootView),
                "Run saved successfully",
                Snackbar.LENGTH_LONG
            ).show()
            stopRun()
        }
    }

    // draw the line again when rotate the device
    private fun addAllPolylines() {
        for (polyline in pathPoints) {
            val polylineOptions = PolylineOptions()
                .color(POLYLINE_COLOR)
                .width(POLYLINE_WIDTH)
                .addAll(polyline)
            map?.addPolyline(polylineOptions)
        }
    }

    // connect two polylines
    private fun addLatestPolyline() {
        if (pathPoints.isNotEmpty() && pathPoints.last().size > 1) {
            val preLastLatLng = pathPoints.last()[pathPoints.last().size - 2]
            val lastLatLng = pathPoints.last().last()
            val polylineOptions = PolylineOptions()
                .color(POLYLINE_COLOR)
                .width(POLYLINE_WIDTH)
                .add(preLastLatLng)
                .add(lastLatLng)

            map?.addPolyline(polylineOptions)
        }
    }

    private fun sendCommandToService(action: String) =
        Intent(requireContext(), TrackingService::class.java).also {
            it.action = action
            requireContext().startService(it)
        }

    override fun onResume() {
        super.onResume()
        fragmentBinding?.mapView?.onResume()
    }

    override fun onStart() {
        super.onStart()
        fragmentBinding?.mapView?.onStart()
    }

    override fun onStop() {
        super.onStop()
        fragmentBinding?.mapView?.onStop()
    }

    override fun onPause() {
        super.onPause()
        fragmentBinding?.mapView?.onPause()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        fragmentBinding?.mapView?.onLowMemory()
    }

    override fun onDestroy() {
        fragmentBinding = null
        super.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        fragmentBinding?.mapView?.onSaveInstanceState(outState)
    }
}