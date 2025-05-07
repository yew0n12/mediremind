package com.example.todolist.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface TodoDao{
    //get ALl
    @Query("SELECT * FROM TODOENTITY ORDER BY importance ASC")
    fun getAllTodo(): List<TodoEntity>

    // insert todo
    @Insert
    fun insertTodo(todo : TodoEntity)

    // delete todo
    @Delete
    fun deleteTodo(todo : TodoEntity)
}