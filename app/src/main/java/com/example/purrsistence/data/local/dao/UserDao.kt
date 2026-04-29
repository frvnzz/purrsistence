package com.example.purrsistence.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.purrsistence.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {

    @Insert
    suspend fun insertUser(userEntity: UserEntity)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("SELECT * FROM UserEntity WHERE userId = :userId LIMIT 1")
    fun getUser(userId: Int): Flow<UserEntity?>

    @Query("UPDATE UserEntity SET balance = balance + :amount WHERE userId = :userId")
    suspend fun addCurrency(userId: Int, amount: Int)

    @Query(
        """
    UPDATE UserEntity 
    SET balance = balance - :price,
        collectedCatsIds = :cats
    WHERE userId = :userId
    """
    )
    suspend fun buyCat(userId: Int, price: Int, cats: List<String>)
}