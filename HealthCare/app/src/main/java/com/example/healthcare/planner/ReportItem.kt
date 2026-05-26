package com.example.healthcare.planner

import com.example.healthcare.database.entities.MedicineLog

sealed class ReportItem {
    data class DateHeader(val date: String) : ReportItem()
    data class MedicineEntry(val log: MedicineLog) : ReportItem()
}
