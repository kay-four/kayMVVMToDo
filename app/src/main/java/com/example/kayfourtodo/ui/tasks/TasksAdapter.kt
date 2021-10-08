package com.example.kayfourtodo.ui.tasks

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.kayfourtodo.data.Tasks
import com.example.kayfourtodo.databinding.ItemTaskBinding


class TasksAdapter(private val listener: OnItemClickListener): ListAdapter<Tasks,TasksAdapter.TasksViewHolder>(DiffCallback()){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TasksViewHolder {
        val binding = ItemTaskBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return TasksViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TasksViewHolder, position: Int) {

        val currentItem = getItem(position)
        holder.bind(currentItem)


    }

    inner class TasksViewHolder(private val binding:ItemTaskBinding): RecyclerView.ViewHolder(binding.root){

         init {
             binding.apply {
                 root.setOnClickListener {
                     val position = adapterPosition
                     if (position != RecyclerView.NO_POSITION) {
                         val task = getItem(position)
                         listener.onItemClick(task)
                     }
                 }
                 checkBoxCompleted.setOnClickListener {
                     val position = adapterPosition
                     if (position != RecyclerView.NO_POSITION) {
                         val task = getItem(position)
                         listener.onCheckBoxClick(task, checkBoxCompleted.isChecked)
                     }
                 }
             }

         }



        fun bind(task:Tasks){
            binding.apply {
                checkBoxCompleted.isChecked = task.isCompleted
                textView.text = task.name
                textView.paint.isStrikeThruText = task.isCompleted
                priorityIcon.isVisible = task.isImportant
            }
        }


    }


    interface OnItemClickListener {
        fun onItemClick(task: Tasks)
        fun onCheckBoxClick(task: Tasks, isChecked: Boolean)
    }

    class DiffCallback : DiffUtil.ItemCallback<Tasks>(){
        override fun areItemsTheSame(oldItem: Tasks, newItem: Tasks) =
            oldItem.id == newItem.id


        override fun areContentsTheSame(oldItem: Tasks, newItem: Tasks) =
            oldItem == newItem
    }


}