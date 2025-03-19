package com.example.todolist.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = arrayOf(TodoEntity::class),version = 1) // ❶ 조건 1
abstract class AppDatabase : RoomDatabase() { // ❷ 조건 2

    abstract fun getTodoDao() : TodoDao // ❸ 조건 3
    // 싱글턴 패턴
    companion object {
        val databaseName = "db_todo"
        var appDatabase : AppDatabase? = null

        fun getInstance(context : Context) : AppDatabase? {
            if(appDatabase == null){
                appDatabase =  Room.databaseBuilder(context,
                    AppDatabase::class.java,
                    databaseName).
                fallbackToDestructiveMigration()
                    .build()
            }
            return  appDatabase
        }
    }

}