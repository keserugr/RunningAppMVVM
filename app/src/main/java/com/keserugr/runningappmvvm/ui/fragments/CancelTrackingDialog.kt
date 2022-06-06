package com.keserugr.runningappmvvm.ui.fragments

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.keserugr.runningappmvvm.R

class CancelTrackingDialog: DialogFragment() {

    private var yesListener: (() -> Unit)? = null
    fun setYesListener(lister: () -> Unit){
        yesListener = lister
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialAlertDialogBuilder(requireContext(), R.style.AlertDialogTheme)
            .setTitle("Cancel the Run?")
            .setMessage("Are you sure to cancel the current run and delete all its data?")
            .setIcon(R.drawable.ic_delete)
            .setPositiveButton("Yes") { _, _ ->
                yesListener?.let { yes ->
                    yes()
                }
            }
            .setNegativeButton("No") { dialoginterfeace, _ ->
                dialoginterfeace.cancel()
            }
            .create()
    }
}