package com.example.mediremind.data

import androidx.room.*

@Dao
interface MedicationDao {
    @Insert
    fun insert(med: Medication)

    @Update
    fun update(med: Medication)

    @Delete
    fun delete(med: Medication)

    @Query("SELECT * FROM medications")
    fun getAll(): List<Medication>
}
