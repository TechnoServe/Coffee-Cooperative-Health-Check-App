package com.technoserve.cooptrac.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface CooperativeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCooperative(cooperative: Cooperative)

    @Query("SELECT * FROM cooperative")
    suspend fun getAllCooperative(): List<Cooperative>

    @Query("SELECT id FROM cooperative WHERE name = :cooperativeName LIMIT 1")
    suspend fun getCooperativeIdByName(cooperativeName: String): Int
}
