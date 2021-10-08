package com.example.kayfourtodo.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TasksDao {

    fun getTasks(query: String, sortOrder: SortOrder, hideCompletedTasks: Boolean):Flow<List<Tasks>> =
    when(sortOrder) {
        SortOrder.BY_DATE -> getTasksSortedByDateCreated(query, hideCompletedTasks)
        SortOrder.BY_NAME -> getTasksSortedByName(query, hideCompletedTasks)
    }


    //to view all tasks we have to define a query outside the default provisions of room
    @Query("SELECT * from tasks_table WHERE(isCompleted != :hideCompletedTasks OR isCompleted = 0) AND name LIKE '%'||:searchQuery||'%'ORDER BY isImportant DESC, name")
    fun getTasksSortedByName(searchQuery:String, hideCompletedTasks:Boolean):Flow<List<Tasks>>

    @Query("SELECT * from tasks_table WHERE(isCompleted != :hideCompletedTasks OR isCompleted = 0) AND name LIKE '%'||:searchQuery||'%'ORDER BY isImportant DESC, timeCreated")
    fun getTasksSortedByDateCreated(searchQuery:String, hideCompletedTasks:Boolean):Flow<List<Tasks>>


    //function to add a new task(s)
   @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Tasks){
    }


    //function to add edit tasks
    @Update
    suspend fun updateTask(task: Tasks){
    }


    //function to add delete a task(s)
    @Delete
    suspend fun deleteTask(task: Tasks){
    }

    @Query("DELETE FROM tasks_table WHERE isCompleted = 1")
    suspend fun deleteCompletedTasks()



}