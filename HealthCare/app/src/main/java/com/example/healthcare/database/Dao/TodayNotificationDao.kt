package com.example.healthcare.database.Dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.healthcare.database.entities.TodayNotification

@Dao
interface TodayNotificationDao {
    @Insert
    suspend fun insert(notification: TodayNotification)

    @Query("SELECT * FROM today_notifications WHERE userId = :userId ORDER BY date DESC, time DESC")
    suspend fun getAllNotifications(userId: Int): List<TodayNotification>

    @Query("DELETE FROM today_notifications WHERE userId = :userId")
    suspend fun clearNotifications(userId: Int)

}
