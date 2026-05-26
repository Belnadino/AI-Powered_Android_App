package com.example.healthcare.utils

import android.content.Context
import com.example.healthcare.database.AppDatabase
import com.example.healthcare.database.entities.UserEntity

object UserSessionManager {

    suspend fun getOrCreateGuestUser(context: Context): UserEntity {
        val db = AppDatabase.getDatabase(context)

        //  Try to get existing guest
        val existingGuest = db.userDao().getGuestUser()
        if (existingGuest != null) {
            return existingGuest
        }

        //  Create ONLY if none exists
        val guest = UserEntity(
            name = "Guest",
            isGuest = true
        )

        val id = db.userDao().insertUser(guest)
        return guest.copy(userId = id.toInt())
    }
}
