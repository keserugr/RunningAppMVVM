package com.keserugr.runningappmvvm

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import com.keserugr.runningappmvvm.adapter.RunAdapter
import com.keserugr.runningappmvvm.ui.fragments.RunFragment
import javax.inject.Inject

class RunningFragmentFactory @Inject constructor(
    private val runAdapter: RunAdapter
): FragmentFactory(){

    override fun instantiate(classLoader: ClassLoader, className: String): Fragment {
        return when(className){
            RunFragment::class.java.name -> RunFragment(runAdapter)
            else -> super.instantiate(classLoader, className)
        }
    }
}