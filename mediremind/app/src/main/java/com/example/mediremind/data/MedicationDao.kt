package com.example.mediremind.data

import androidx.room.*

//@Dao
//interface MedicationDao {
//    @Insert
//    fun insert(med: Medication)
//
//    @Update
//    fun update(med: Medication)
//
//    @Delete
//    fun delete(med: Medication)
//
//    @Query("SELECT * FROM medications")
//    fun getAll(): List<Medication>
//}
@Dao
interface MedicationDao {
    //Main thread를 막지 않도록 suspend fun으로 변경
    @Insert
    suspend fun insert(med: Medication)

    @Update
    suspend fun update(med: Medication)

    @Delete
    suspend fun delete(med: Medication)

    @Query("SELECT * FROM medications")
    suspend fun getAll(): List<Medication>

    @Query("""
        SELECT * FROM medications
        WHERE startDate <= :today
        AND (endDate IS NULL OR endDate >= :today)
    """)
    suspend fun getMedicationsForToday(today: String): List<Medication>
}