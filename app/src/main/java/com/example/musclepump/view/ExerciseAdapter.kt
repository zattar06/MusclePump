package com.example.musclepump.view

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.musclepump.R
import com.example.musclepump.model.Exercise

class ExerciseAdapter(
    private val exerciseList: List<Exercise>,
    private val onSelectImageClick: (Int) -> Unit
) : RecyclerView.Adapter<ExerciseAdapter.ExerciseViewHolder>() {

    private var selectedPosition: Int = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExerciseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_exercise, parent, false)
        return ExerciseViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExerciseViewHolder, position: Int) {
        val exercise = exerciseList[position]
        holder.bind(exercise)
        holder.itemView.setOnClickListener { onSelectImageClick(position) }
        holder.btnSelectImage.setOnClickListener { onSelectImageClick(position) }
    }

    override fun getItemCount(): Int {
        return exerciseList.size
    }

    inner class ExerciseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvExerciseName: TextView = itemView.findViewById(R.id.tv_exercise_name)
        private val tvObservation: TextView = itemView.findViewById(R.id.tv_observation)
        val btnSelectImage: Button = itemView.findViewById(R.id.btn_select_image)
        private val ivBackground: ImageView = itemView.findViewById(R.id.iv_background)

        fun bind(exercise: Exercise) {
            tvExerciseName.text = exercise.name
            tvObservation.text = exercise.obs

            if (!exercise.imageUri.isNullOrEmpty()) {
                Glide.with(itemView)
                    .load(Uri.parse(exercise.imageUri))
                    .into(ivBackground)
            }
        }
    }
}
