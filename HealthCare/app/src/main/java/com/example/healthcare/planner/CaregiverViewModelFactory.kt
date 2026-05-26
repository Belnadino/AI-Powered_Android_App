package com.example.healthcare.planner

import androidx.lifecycle.*
import com.example.healthcare.database.Dao.*
import androidx.lifecycle.ViewModel

class CaregiverViewModelFactory(
    private val dao: CaregiverDao
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CaregiverViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CaregiverViewModel(dao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
