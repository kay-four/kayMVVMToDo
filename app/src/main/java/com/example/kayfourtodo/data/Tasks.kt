package com.example.kayfourtodo.data

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize
import java.text.DateFormat

@Entity(tableName = "tasks_table")
@Parcelize
data class Tasks(
    val name: String,
    val isImportant: Boolean = false,
    val isCompleted: Boolean = false,
    val timeCreated: Long = System.currentTimeMillis(),
    @PrimaryKey(autoGenerate = true) val id: Int = 0
) : Parcelable {
    val createdDateFormatted: String
        get() = DateFormat.getDateTimeInstance().format(timeCreated)
}