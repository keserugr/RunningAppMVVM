package com.keserugr.runningappmvvm.ui.fragments

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.textview.MaterialTextView
import com.keserugr.runningappmvvm.R
import com.keserugr.runningappmvvm.adapter.RunAdapter
import com.keserugr.runningappmvvm.databinding.FragmentRunBinding
import com.keserugr.runningappmvvm.ui.viewmodel.MainViewModel
import com.keserugr.runningappmvvm.util.Constants.REQUEST_CODE_LOCATION_PERMISSION
import com.keserugr.runningappmvvm.util.SortType
import com.keserugr.runningappmvvm.util.TrackingUtility
import com.predicomm.coffee.util.preferences.IMySharedPreferences
import dagger.hilt.android.AndroidEntryPoint
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import javax.inject.Inject

@AndroidEntryPoint
class RunFragment @Inject constructor(
    val runAdapter: RunAdapter
): Fragment(R.layout.fragment_run), EasyPermissions.PermissionCallbacks {

    private val viewModel: MainViewModel by viewModels()
    private  var fragmentBinding: FragmentRunBinding? = null

    @Inject
    lateinit var sharedPreferences: IMySharedPreferences

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentRunBinding.bind(view)
        fragmentBinding = binding

        //setToolbarText(sharedPreferences.getUserDetail()!!.name)

        requestPermissions()
        setupRecyclerView()

        when(viewModel.sortType){
            SortType.DATE -> fragmentBinding!!.spFilter.setSelection(0)
            SortType.RUNNING_TIME -> fragmentBinding!!.spFilter.setSelection(1)
            SortType.DISTANCE -> fragmentBinding!!.spFilter.setSelection(2)
            SortType.AVG_SPEED -> fragmentBinding!!.spFilter.setSelection(3)
            SortType.CALORIES_BURNED -> fragmentBinding!!.spFilter.setSelection(4)
        }

        fragmentBinding!!.spFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(adapterView: AdapterView<*>?, view: View?, position: Int, id: Long) {
                when(position){
                    0 -> viewModel.sortRuns(SortType.DATE)
                    1 -> viewModel.sortRuns(SortType.RUNNING_TIME)
                    2 -> viewModel.sortRuns(SortType.DISTANCE)
                    3 -> viewModel.sortRuns(SortType.AVG_SPEED)
                    4 -> viewModel.sortRuns(SortType.CALORIES_BURNED)
                }
            }

            override fun onNothingSelected(adapterView: AdapterView<*>?) {
            }
        }

        viewModel.runs.observe(viewLifecycleOwner, Observer {
            runAdapter.submitList(it)
        })

        binding.fab.setOnClickListener{
            findNavController().navigate(R.id.action_runFragment_to_trackingFragment)
        }
    }

    private fun setToolbarText(name: String) {
        val toolbarText = "Let's $name!"
        val tvToolbarTitle = requireActivity().findViewById<MaterialTextView>(R.id.tvToolbarTitle)
        tvToolbarTitle.text = toolbarText
    }

    private fun setupRecyclerView() = fragmentBinding!!.rvRuns.apply {
        adapter = runAdapter
        layoutManager = LinearLayoutManager(requireContext())
    }

    private fun requestPermissions() {
        if(TrackingUtility.hasLocationPermissions(requireContext())) {
            return
        }
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            EasyPermissions.requestPermissions(
                this,
                "You need to accept location permissions to use this app.",
                REQUEST_CODE_LOCATION_PERMISSION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        } else {
            EasyPermissions.requestPermissions(
                this,
                "You need to accept location permissions to use this app.",
                REQUEST_CODE_LOCATION_PERMISSION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )
        }
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        if(EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            AppSettingsDialog.Builder(this).build().show()
        } else {
            requestPermissions()
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {}

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onDestroy() {
        fragmentBinding = null
        super.onDestroy()
    }
}