package com.equationl.manhourslog.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        DBManHoursTable::class,
    ],
    version = 1,
    exportSchema = true
)
//@TypeConverters(DBConverters::class)
abstract class ManHoursDB : RoomDatabase() {
    companion object {
        fun create(context: Context, useInMemory: Boolean = false): ManHoursDB {
            val databaseBuilder = if (useInMemory) {
                Room.inMemoryDatabaseBuilder(context, ManHoursDB::class.java)
            } else {
                Room.databaseBuilder(context, ManHoursDB::class.java, "man_hours_data.db")
            }
            return databaseBuilder
                .fallbackToDestructiveMigration()
                .build()
        }
    }

    abstract fun manHoursDB(): ManHoursDao
}