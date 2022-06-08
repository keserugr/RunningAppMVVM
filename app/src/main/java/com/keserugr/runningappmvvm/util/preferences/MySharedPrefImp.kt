package com.keserugr.runningappmvvm.util.preferences

import android.content.SharedPreferences
import com.google.gson.Gson
import com.keserugr.runningappmvvm.model.User
import com.keserugr.runningappmvvm.util.Constants.KEY_FIRST_TIME_TOGGLE
import com.keserugr.runningappmvvm.util.Constants.KEY_USER
import com.predicomm.coffee.util.preferences.IMySharedPreferences
import javax.inject.Inject

class MySharedPrefImp @Inject constructor(
    private val sharedPreferences: SharedPreferences,
    private val gson: Gson
): IMySharedPreferences {
    override fun getUserDetail(): User? {
        val userString = sharedPreferences.getString(KEY_USER,"")
        userString?.let {
            return gson.fromJson(it,User::class.java)
        }
        return null
    }

    override fun setUserDetail(user: User) {
        sharedPreferences.edit().remove(KEY_USER).apply()
        val gsonString = gson.toJson(user)
        sharedPreferences.edit().putString(KEY_USER,gsonString).apply()
    }

    override fun getIsFirstAppOpen(): Boolean {
        return sharedPreferences.getBoolean(KEY_FIRST_TIME_TOGGLE, true)
    }

    override fun setIsFirstAppOpen(isFirstAppOpen: Boolean) {
        sharedPreferences.edit().remove(KEY_FIRST_TIME_TOGGLE).apply()
        sharedPreferences.edit().putBoolean(KEY_FIRST_TIME_TOGGLE, isFirstAppOpen).apply()
    }


}