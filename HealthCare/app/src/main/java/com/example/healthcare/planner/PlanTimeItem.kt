package com.example.healthcare.planner

import com.example.healthcare.database.entities.MedicinePlan

data class PlanTimeItem(
    val plan: MedicinePlan,
    val time: String // e.g., "09:00"
)
