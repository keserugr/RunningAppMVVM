package com.keserugr.runningappmvvm.di

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import androidx.room.Room
import com.bumptech.glide.Glide
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.gson.Gson
import com.keserugr.runningappmvvm.db.RunDao
import com.keserugr.runningappmvvm.db.RunningDatabase
import com.keserugr.runningappmvvm.repositories.IRunRepository
import com.keserugr.runningappmvvm.repositories.RunRepository
import com.keserugr.runningappmvvm.util.Constants.RUNNING_DATABASE_NAME
import com.keserugr.runningappmvvm.util.Constants.SHARED_PREFERENCES_NAME
import com.keserugr.runningappmvvm.util.preferences.MySharedPrefImp
import com.predicomm.coffee.util.preferences.IMySharedPreferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideRunningDatabase(@ApplicationContext context: Context) = Room.databaseBuilder(
        context, RunningDatabase::class.java, RUNNING_DATABASE_NAME
    ).build()

    @Singleton
    @Provides
    fun provideRunningDao(db: RunningDatabase) = db.runDao()

    @Singleton
    @Provides
    fun provideRunningRepository(dao: RunDao) = RunRepository(dao) as IRunRepository

    @Singleton
    @Provides
    fun provideGlide(@ApplicationContext context: Context) = Glide.with(context)

    @Singleton
    @Provides
    fun provideGson() = Gson()

    @Singleton
    @Provides
    fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences =
        context.getSharedPreferences(SHARED_PREFERENCES_NAME,MODE_PRIVATE)

    @Singleton
    @Provides
    fun provideMySharedPreferences(preferences: SharedPreferences, gson: Gson) =
        MySharedPrefImp(preferences,gson) as IMySharedPreferences

}