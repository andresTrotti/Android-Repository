package com.snapcompany.snapsafe.utilities

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow


@Dao
interface AppDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertKey(roomKey: RoomKey)

    @Query("DELETE FROM keys WHERE roomKey = :key")
    suspend fun deleteKey(key: String)

    @Query("SELECT * FROM keys WHERE roomKey = :key")
    suspend fun getKey(key: String): RoomKey?

    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGate(gateData: GateData)

    @Query("SELECT * FROM gates WHERE gateId = :id")
    suspend fun getGateById(id: String): GateData?

    @Update
    suspend fun updateGate(gateData: GateData)

    @Query("DELETE FROM gates WHERE gateId = :id")
    suspend fun deleteGate(id: String)

    @Query("SELECT * FROM gates")
    suspend fun getAllGates(): List<GateData>

    @Query("DELETE FROM gates")
    suspend fun deleteAllGates()


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(userDataAppDb: UserDataAppDb)

    @Query ("SELECT * FROM users WHERE email = :email")
    suspend fun getUserById(email: String): UserDataAppDb?


    @Update
    suspend fun updateUser(userDataAppDb: UserDataAppDb)

    @Query ("DELETE FROM users WHERE email = :email")
    suspend fun deleteUser(email: String)


}