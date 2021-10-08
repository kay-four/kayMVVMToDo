package com.example.kayfourtodo.ui.tasks

import android.content.Context
import androidx.hilt.Assisted
import androidx.lifecycle.*
import com.example.kayfourtodo.data.PreferencesManager
import com.example.kayfourtodo.data.SortOrder
import com.example.kayfourtodo.data.Tasks
import com.example.kayfourtodo.data.TasksDao
import com.example.kayfourtodo.ui.ADD_TASK_RESULT_OK
import com.example.kayfourtodo.ui.EDIT_TASK_RESULT_OK
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TasksViewModel  @Inject constructor (
    private val preferencesManager: PreferencesManager,
    private val tasksDao: TasksDao,
   @Assisted private val state:SavedStateHandle): ViewModel() {

    val searchQuery=state.getLiveData("searchQuery","")

   val preferencesFlow = preferencesManager.preferencesFlow

    private val tasksEventChannel = Channel<TasksEvent>()
    val tasksEvent = tasksEventChannel.receiveAsFlow()


    private val tasksFlow = combine(
        searchQuery.asFlow(),
        preferencesFlow
    ){
        query, filterPreferences ->
        Pair(query, filterPreferences)
    }
         .flatMapLatest {(query,filterPreferences)->
        tasksDao.getTasks(query,filterPreferences.sortOrder, filterPreferences.hideCompleted)
    }

    val tasks = tasksFlow.asLiveData()

    fun onSortOrderSelected(context: Context, sortOrder:SortOrder) = viewModelScope.launch {
        preferencesManager.updateSortOrder(context,sortOrder)
    }

    fun onHideCompletedClick(context: Context,hideCompleted:Boolean) = viewModelScope.launch {
        preferencesManager.updateHideCompleted(context,hideCompleted)
    }

    fun onTaskSelected(task: Tasks) =viewModelScope.launch{
        tasksEventChannel.send(TasksEvent.NavigateToEditTaskScreen(task))
    }

    fun onTaskCheckedChanged(task:Tasks, isChecked:Boolean)=
        viewModelScope.launch {
        tasksDao.updateTask(task.copy(isCompleted = isChecked))
    }
    fun onTaskSwiped(task:Tasks) = viewModelScope.launch {
        tasksDao.deleteTask(task)
        tasksEventChannel.send(TasksEvent.ShowUndoDeleteTaskMessage(task))

    }

    fun onUndoDeleteClick(task:Tasks) = viewModelScope.launch {
        tasksDao.insertTask(task)
    }

    fun onAddNewTaskClick() =viewModelScope.launch {
        tasksEventChannel.send(TasksEvent.NavigateToAddScreen)
    }
    fun onAddEditResult(result: Int) {
        when (result) {
            ADD_TASK_RESULT_OK -> showTaskSavedConfirmationMessage("Task has been added successfully")
            EDIT_TASK_RESULT_OK -> showTaskSavedConfirmationMessage("Task has been updated successfully")
        }
    }

    private fun showTaskSavedConfirmationMessage(text:String) = viewModelScope.launch {
        tasksEventChannel.send(TasksEvent.ShowTaskSavedConfirmationMessage(text))
    }

    fun onDeleteAllCompletedClick() = viewModelScope.launch {
        tasksEventChannel.send(TasksEvent.NavigateToDeleteAllCompletedScreen)
    }

    sealed class TasksEvent{
        object NavigateToAddScreen : TasksEvent()
        data class NavigateToEditTaskScreen(val task:Tasks):TasksEvent()
        data class ShowUndoDeleteTaskMessage(val task: Tasks):TasksEvent()
        data class ShowTaskSavedConfirmationMessage(val msg:String):TasksEvent()
        object NavigateToDeleteAllCompletedScreen : TasksEvent()
    }



}
