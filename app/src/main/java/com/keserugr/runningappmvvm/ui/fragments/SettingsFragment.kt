package com.keserugr.runningappmvvm.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textview.MaterialTextView
import com.keserugr.runningappmvvm.R
import com.keserugr.runningappmvvm.databinding.FragmentSettingsBinding
import com.keserugr.runningappmvvm.model.User
import com.predicomm.coffee.util.preferences.IMySharedPreferences
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SettingsFragment : Fragment(R.layout.fragment_settings) {

    @Inject
    lateinit var sharedPref: IMySharedPreferences

    private lateinit var binding: FragmentSettingsBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val fragmentBinging = FragmentSettingsBinding.bind(view)
        binding = fragmentBinging

        loadFieldsFromSharedPref()

        binding.btnApplyChanges.setOnClickListener {
            val success = applyChangesToSharedPref()
            if (success) {
                Snackbar.make(view, "Saved changes", Snackbar.LENGTH_LONG).show()
            } else {
                Snackbar.make(view, "Please fill out all the fields", Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun loadFieldsFromSharedPref() {
        val name = sharedPref.getUserDetail()?.name
        val weight = sharedPref.getUserDetail()?.weight.toString()
        binding.etName.setText(name)
        binding.etWeight.setText(weight)
    }

    private fun applyChangesToSharedPref(): Boolean {
        val nameText = binding.etName.text.toString()
        val weightText = binding.etWeight.text.toString()
        if (nameText.isEmpty() || weightText.isEmpty()) {
            return false
        } else {
            val user = User(nameText, weightText.toFloat())
            sharedPref.setUserDetail(user)
            setToolbarText(nameText)
            return true
        }
    }

    private fun setToolbarText(name: String) {
        val toolbarText = "Let's $name!"
        val tvToolbarTitle = requireActivity().findViewById<MaterialTextView>(R.id.tvToolbarTitle)
        tvToolbarTitle.text = toolbarText
    }
}