package com.example.kayfourtodo.ui.addedittask

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kayfourtodo.data.Tasks
import com.example.kayfourtodo.data.TasksDao
import com.example.kayfourtodo.ui.ADD_TASK_RESULT_OK
import com.example.kayfourtodo.ui.EDIT_TASK_RESULT_OK
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddEditTaskViewModel @Inject constructor(
    private val tasksDao: TasksDao,
    private val state:SavedStateHandle) :ViewModel() {

    val task = state.get<Tasks>("task")

    var taskName = state.get<String>("taskName")?: task?.name?:""
    set(value){
        field = value
        state.set("taskName",value)
    }

    var taskImportance = state.get<Boolean>("taskImportance")?: task?.isImportant?:false
        set(value){
            field = value
            state.set("taskImportance",value)
        }

    private val addEditTaskEventChannel = Channel<AddEditTaskEvent>()
    val addEditTaskEvent = addEditTaskEventChannel.receiveAsFlow()

    fun onSaveClick(){
        if (taskName.isBlank()) {
            //we want to show "Invalid Input" message
            showInvalidInputMessage("Name cannot be empty")
            return
        }
        if (task != null) {
            val updatedTask = task.copy(name = taskName, isImportant = taskImportance)
            updateTask(updatedTask)
        } else {
            val newTask = Tasks(taskName,taskImportance)
            createTask(newTask)
        }
    }

    private fun createTask(task:Tasks) = viewModelScope.launch {
        tasksDao.insertTask(task)
        addEditTaskEventChannel.send(AddEditTaskEvent.NavigateBackWithResult(ADD_TASK_RESULT_OK))

    }

    private fun updateTask(task:Tasks) = viewModelScope.launch {
        tasksDao.updateTask(task)
        addEditTaskEventChannel.send(AddEditTaskEvent.NavigateBackWithResult(EDIT_TASK_RESULT_OK))


    }

    private fun showInvalidInputMessage(text:String) = viewModelScope.launch {
        addEditTaskEventChannel.send(AddEditTaskEvent.ShowInvalidInputMessage(text))
    }

    sealed class AddEditTaskEvent{
        data class ShowInvalidInputMessage(val msg:String): AddEditTaskEvent()
        data class NavigateBackWithResult(val result:Int):AddEditTaskEvent()
    }

}