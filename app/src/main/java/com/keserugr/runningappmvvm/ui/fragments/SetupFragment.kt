package com.keserugr.runningappmvvm.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textview.MaterialTextView
import com.keserugr.runningappmvvm.R
import com.keserugr.runningappmvvm.databinding.FragmentSetupBinding
import com.keserugr.runningappmvvm.model.User
import com.predicomm.coffee.util.preferences.IMySharedPreferences
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SetupFragment : Fragment(R.layout.fragment_setup) {

    @Inject
    lateinit var sharedPreferences: IMySharedPreferences

    private var fragmentBinding: FragmentSetupBinding? = null
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentSetupBinding.bind(view)
        fragmentBinding = binding

        if (!sharedPreferences.getIsFirstAppOpen()) {
            val navOptions = NavOptions.Builder()
                .setPopUpTo(R.id.setupFragment, true)
                .build()

            findNavController().navigate(
                R.id.action_setupFragment_to_runFragment,
                savedInstanceState,
                navOptions
            )
        }

        binding.tvContinue.setOnClickListener {
            val success = writePersonalDataToSaheredPref()
            if (success) {
                findNavController().navigate(R.id.action_setupFragment_to_runFragment)
            } else {
                Snackbar.make(requireView(), "Please enter all the fields", Snackbar.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun writePersonalDataToSaheredPref(): Boolean {
        val name = fragmentBinding!!.etName.text.toString()
        val weight = fragmentBinding!!.etWeight.text.toString()
        if (name.isEmpty() || weight.isEmpty())
            return false
        val user = User(name, weight.toFloat())
        sharedPreferences.setUserDetail(user)
        sharedPreferences.setIsFirstAppOpen(false)
        return true
    }

    override fun onDestroy() {
        fragmentBinding = null
        super.onDestroy()
    }
}