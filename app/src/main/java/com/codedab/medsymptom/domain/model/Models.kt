package com.codedab.medsymptom.domain.model

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime

enum class Severity(val label: String, val value: Int) {
    MILD("Mild", 1),
    MODERATE("Moderate", 2),
    SEVERE("Severe", 3),
    CRITICAL("Critical", 4),
}

data class Symptom(
    val id: Long = 0,
    val name: String,
    val severity: Severity,
    val notes: String = "",
    val recordedAt: LocalDateTime,
    val isSynced: Boolean = false,
)

enum class FrequencyUnit { HOURS, DAYS }

data class Medication(
    val id: Long = 0,
    val name: String,
    val dosage: String,
    val frequencyValue: Int,
    val frequencyUnit: FrequencyUnit,
    val startDate: LocalDate,
    val endDate: LocalDate?,
    val notes: String = "",
    val isSynced: Boolean = false,
)

enum class VitalType(val displayName: String, val unit: String) {
    BLOOD_PRESSURE("Blood Pressure", "mmHg"),
    HEART_RATE("Heart Rate", "bpm"),
    TEMPERATURE("Temperature", "°C"),
    OXYGEN_SATURATION("O₂ Saturation", "%"),
    BLOOD_GLUCOSE("Blood Glucose", "mg/dL"),
    WEIGHT("Weight", "kg"),
}

data class VitalSign(
    val id: Long = 0,
    val type: VitalType,
    val value: String,
    val recordedAt: LocalDateTime,
    val notes: String = "",
    val isSynced: Boolean = false,
)

enum class ReminderType { MEDICATION, VITAL_CHECK, SYMPTOM_LOG }

data class Reminder(
    val id: Long = 0,
    val type: ReminderType,
    val title: String,
    val message: String,
    val scheduledAt: LocalDateTime,
    val isRecurring: Boolean = false,
    val intervalHours: Int = 24,
    val isActive: Boolean = true,
    val workManagerId: String = "",
)
