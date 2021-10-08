package com.example.kayfourtodo.ui.deleteallcompleted

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import com.example.kayfourtodo.data.TasksDao
import com.example.kayfourtodo.di.ApplicationScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch


class DeleteAllCompletedViewModel @ViewModelInject constructor(
    private val tasksDao: TasksDao,
    @ApplicationScope private val applicationScope: CoroutineScope
) :ViewModel() {

    fun onConfirmClick() = applicationScope.launch {
        tasksDao.deleteCompletedTasks()
    }
}