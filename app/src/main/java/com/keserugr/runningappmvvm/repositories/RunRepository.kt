package com.keserugr.runningappmvvm.repositories

import androidx.lifecycle.LiveData
import com.keserugr.runningappmvvm.db.Run
import com.keserugr.runningappmvvm.db.RunDao
import javax.inject.Inject

class RunRepository @Inject constructor(
    val runDao: RunDao
): IRunRepository{
    override suspend fun insertRun(run: Run) = runDao.insertRun(run)

    override suspend fun deleteRun(run: Run) = runDao.deleteRun(run)

    override fun getAllRunsSortedByDate() = runDao.getAllRunsSortedByDate()

    override fun getAllRunsSortedByTimeInMillis(): LiveData<List<Run>> =runDao.getAllRunsSortedByTimeInMillis()

    override fun getAllRunsSortedByCaloriesBurned() = runDao.getAllRunsSortedByCaloriesBurned()

    override fun getAllRunsSortedByAvgSpeed() = runDao.getAllRunsSortedByAvgSpeed()

    override fun getAllRunsSortedByDistance() = runDao.getAllRunsSortedByDistance()

    override fun getTotalTimeInMillis() = runDao.getTotalTimeInMillis()

    override fun getTotalCaloriesBurned() = runDao.getTotalCaloriesBurned()

    override fun getTotalDistance() = runDao.getTotalDistance()

    override fun getTotalAvgSpeed() = runDao.getTotalAvgSpeed()
}