package com.example.musclepump.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.musclepump.R
import com.example.musclepump.model.Train

class UserAdapter(
    private val trainList: List<Train>,
    private val onItemClick: (Train) -> Unit,
    private val onDatePickerClick: (Train, Int) -> Unit
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_train, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val train = trainList[position]
        holder.bind(train, position)
        holder.itemView.setOnClickListener { onItemClick(train) }
        holder.btnDatePicker.setOnClickListener { onDatePickerClick(train, position) }
    }

    override fun getItemCount(): Int {
        return trainList.size
    }

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvName: TextView = itemView.findViewById(R.id.tv_item_title)
        private val tvDescription: TextView = itemView.findViewById(R.id.tv_item_subtitle)
        val btnDatePicker: Button = itemView.findViewById(R.id.btn_date_picker)
        private val tvDate: TextView = itemView.findViewById(R.id.tv_item_date)

        fun bind(train: Train, position: Int) {
            tvName.text = train.title
            tvDescription.text = train.description
            if (train.date.isNullOrEmpty()) {
                tvDate.visibility = View.GONE
                btnDatePicker.visibility = View.VISIBLE
            } else {
                tvDate.visibility = View.VISIBLE
                btnDatePicker.visibility = View.GONE
                tvDate.text = train.date
            }
            btnDatePicker.setOnClickListener { onDatePickerClick(train, position) }
        }
    }
}
