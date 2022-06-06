package com.predicomm.coffee.util.preferences

import com.keserugr.runningappmvvm.model.User

interface IMySharedPreferences {

    fun getUserDetail(): User?

    fun setUserDetail(user: User)

    fun getIsFirstAppOpen(): Boolean

    fun setIsFirstAppOpen(isFirstAppOpen: Boolean)
    
}