package com.keserugr.runningappmvvm.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.keserugr.runningappmvvm.R
import com.keserugr.runningappmvvm.db.Run
import com.keserugr.runningappmvvm.util.TrackingUtility
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class RunAdapter @Inject constructor(
    val glide: RequestManager
) : RecyclerView.Adapter<RunAdapter.RunViewHolder>() {

    class RunViewHolder(var view: View) : RecyclerView.ViewHolder(view)

    private val diffCallBack = object : DiffUtil.ItemCallback<Run>() {
        override fun areItemsTheSame(oldItem: Run, newItem: Run): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Run, newItem: Run): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }
    }

    val differ = AsyncListDiffer(this, diffCallBack)

    fun submitList(list: List<Run>) = differ.submitList(list)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RunViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_run, parent, false)
        return RunViewHolder(view)
    }

    override fun onBindViewHolder(holder: RunViewHolder, position: Int) {
        val ivRunImage = holder.view.findViewById<ImageView>(R.id.ivRunImage)
        val tvDate = holder.view.findViewById<TextView>(R.id.tvDate)
        val tvAvgSpeed = holder.view.findViewById<TextView>(R.id.tvAvgSpeed)
        val tvDistance = holder.view.findViewById<TextView>(R.id.tvDistance)
        val tvTime = holder.view.findViewById<TextView>(R.id.tvTime)
        val tvCalories = holder.view.findViewById<TextView>(R.id.tvCalories)

        val run = differ.currentList[position]
        holder.view.apply {
            glide.load(run.img).into(ivRunImage)

            val calendar = Calendar.getInstance().apply {
                timeInMillis = run.timestamp
            }
            val dateFormat = SimpleDateFormat("dd.MM.yy", Locale.getDefault())
            tvDate.text = dateFormat.format(calendar.time)

            val avgSpeed = "${run.avgSpeedInKMH}km/h"
            tvAvgSpeed.text = avgSpeed

            val distanceInKm = "${run.distanceInMeters / 1000f}km"
            tvDistance.text = distanceInKm

            tvTime.text = TrackingUtility.getFormattedStopWatchTime(run.timeInMillis)

            val caloriesBurned = "${run.caloriesBurned}kcal"
            tvCalories.text = caloriesBurned

        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }


}