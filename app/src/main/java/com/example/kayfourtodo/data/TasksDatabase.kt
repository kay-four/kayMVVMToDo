package com.example.kayfourtodo.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.kayfourtodo.di.ApplicationScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Provider


@Database(entities = [Tasks::class],version = 1,exportSchema = false)
abstract class TasksDatabase :RoomDatabase(){

    abstract fun tasksDao():TasksDao


    class Callback @Inject constructor(
        private val database: Provider<TasksDatabase>,
        @ApplicationScope private val applicationScope:CoroutineScope
    ) : RoomDatabase.Callback() {

        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)

            val dao = database.get().tasksDao()

            applicationScope.launch {
                dao.insertTask(Tasks("Clean the car"))
                dao.insertTask(Tasks("Sweep fallen leaves"))
                dao.insertTask(Tasks("Buy groceries", isImportant = true))
                dao.insertTask(Tasks("Prepare food", isCompleted = true))
                dao.insertTask(Tasks("Call mom",isImportant = true))
                dao.insertTask(Tasks("Go for a run", isCompleted = true))
                dao.insertTask(Tasks("Call bae"))

            }
        }
    }

}