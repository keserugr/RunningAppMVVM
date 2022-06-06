package com.keserugr.runningappmvvm.repositories

import androidx.lifecycle.LiveData
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.keserugr.runningappmvvm.db.Run

interface IRunRepository {

    suspend fun insertRun(run: Run)

    suspend fun deleteRun(run: Run)

    fun getAllRunsSortedByDate(): LiveData<List<Run>>

    fun getAllRunsSortedByTimeInMillis(): LiveData<List<Run>>

    fun getAllRunsSortedByCaloriesBurned(): LiveData<List<Run>>

    fun getAllRunsSortedByAvgSpeed(): LiveData<List<Run>>

    fun getAllRunsSortedByDistance(): LiveData<List<Run>>

    fun getTotalTimeInMillis(): LiveData<Long>

    fun getTotalCaloriesBurned(): LiveData<Int>

    fun getTotalDistance(): LiveData<Int>

    fun getTotalAvgSpeed(): LiveData<Float>
}