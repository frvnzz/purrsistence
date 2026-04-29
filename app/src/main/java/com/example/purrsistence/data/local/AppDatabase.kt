package com.example.purrsistence.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.purrsistence.data.local.entity.GoalEntity
import com.example.purrsistence.data.local.entity.TrackingSessionEntity
import com.example.purrsistence.data.local.entity.UserEntity
import androidx.room.TypeConverters
import com.example.purrsistence.data.local.converter.StringListConverter
import com.example.purrsistence.data.local.dao.GoalsDao
import com.example.purrsistence.data.local.dao.TrackingDao
import com.example.purrsistence.data.local.dao.UserDao

@Database(
    entities = [
        UserEntity::class,
        GoalEntity::class,
        TrackingSessionEntity::class
    ],
    // increment version if the following error occurs:
    // java.lang.IllegalStateException: Room cannot verify the data integrity.
    version = 2,
    exportSchema = false
)
@TypeConverters(StringListConverter::class)
abstract class AppDatabase : RoomDatabase() {

    // TODO: Split Dao and add all of them
    abstract fun goalsDao(): GoalsDao
    abstract fun trackingDao(): TrackingDao
    abstract fun userDao(): UserDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_db"
                ).fallbackToDestructiveMigration(false)
                    .build().also { INSTANCE = it }
            }
    }
}
