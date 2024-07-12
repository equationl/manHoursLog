package com.equationl.manhourslog.database

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        DBManHoursTable::class,
    ],
    version = 5,
    exportSchema = true,
    autoMigrations = [
        AutoMigration(from = 1, to = 2),
        AutoMigration(from = 2, to = 3),
        AutoMigration(from = 3, to = 4),
        AutoMigration(from = 4, to = 5)
    ]
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
                //.fallbackToDestructiveMigration()
                .build()
        }
    }

    abstract fun manHoursDB(): ManHoursDao
}