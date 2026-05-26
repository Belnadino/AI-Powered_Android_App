package com.example.healthcare.planner

import androidx.lifecycle.*
import com.example.healthcare.database.Dao.*
import kotlinx.coroutines.launch
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.healthcare.database.entities.*
import androidx.lifecycle.asLiveData


class CaregiverViewModel(
    private val dao: CaregiverDao
) : ViewModel() {

    // Live list for RecyclerView
    val caregivers: LiveData<List<CaregiverEntity>> =
        dao.getAllCaregivers().asLiveData()

    fun saveCaregiver(caregiver: CaregiverEntity) = viewModelScope.launch {
        if (caregiver.id == 0) {
            dao.insertCaregiver(caregiver)
        } else {
            dao.updateCaregiver(caregiver)
        }
    }

    fun deleteCaregiver(caregiver: CaregiverEntity) = viewModelScope.launch {
        dao.deleteCaregiver(caregiver)
    }

    suspend fun getCaregiver(id: Int): CaregiverEntity? {
        return dao.getCaregiverById(id)
    }
}
