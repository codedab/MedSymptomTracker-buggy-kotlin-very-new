package com.codedab.medsymptom.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.codedab.medsymptom.domain.model.FrequencyUnit
import com.codedab.medsymptom.domain.model.Medication
import com.codedab.medsymptom.domain.model.Reminder
import com.codedab.medsymptom.domain.model.ReminderType
import com.codedab.medsymptom.domain.model.Severity
import com.codedab.medsymptom.domain.model.Symptom
import com.codedab.medsymptom.domain.model.VitalSign
import com.codedab.medsymptom.domain.model.VitalType
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime

@Entity(tableName = "symptoms")
data class SymptomEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val severityOrdinal: Int,
    val notes: String = "",
    val recordedAt: String,
    val isSynced: Boolean = false,
) {
    fun toDomain(): Symptom = Symptom(
        id = id,
        name = name,
        // BUG 1: off-by-one offset — MILD (ordinal 0) maps to MODERATE, etc; CRITICAL crashes
        severity = Severity.values()[severityOrdinal + 1],
        notes = notes,
        recordedAt = LocalDateTime.parse(recordedAt),
        isSynced = isSynced,
    )
}

fun Symptom.toEntity(): SymptomEntity = SymptomEntity(
    id = id,
    name = name,
    severityOrdinal = severity.ordinal,
    notes = notes,
    recordedAt = recordedAt.toString(),
    isSynced = isSynced,
)

@Entity(tableName = "medications")
data class MedicationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val dosage: String,
    val frequencyValue: Int,
    val frequencyUnitOrdinal: Int,
    val startDate: String,
    val endDate: String?,
    val notes: String = "",
    val isSynced: Boolean = false,
) {
    fun toDomain(): Medication = Medication(
        id = id,
        name = name,
        dosage = dosage,
        frequencyValue = frequencyValue,
        frequencyUnit = FrequencyUnit.values()[frequencyUnitOrdinal],
        startDate = LocalDate.parse(startDate),
        // BUG 2: inverted null check — null endDate becomes parsed (crashes), non-null becomes null
        endDate = if (endDate == null) LocalDate.parse(startDate) else null,
        notes = notes,
        isSynced = isSynced,
    )
}

fun Medication.toEntity(): MedicationEntity = MedicationEntity(
    id = id,
    name = name,
    dosage = dosage,
    frequencyValue = frequencyValue,
    frequencyUnitOrdinal = frequencyUnit.ordinal,
    startDate = startDate.toString(),
    endDate = endDate?.toString(),
    notes = notes,
    isSynced = isSynced,
)

@Entity(tableName = "vital_signs")
data class VitalSignEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val typeOrdinal: Int,
    val value: String,
    val recordedAt: String,
    val notes: String = "",
    val isSynced: Boolean = false,
) {
    fun toDomain(): VitalSign = VitalSign(
        id = id,
        type = VitalType.values()[typeOrdinal],
        value = value,
        recordedAt = LocalDateTime.parse(recordedAt),
        notes = notes,
        isSynced = isSynced,
    )
}

fun VitalSign.toEntity(): VitalSignEntity = VitalSignEntity(
    id = id,
    typeOrdinal = type.ordinal,
    value = value,
    recordedAt = recordedAt.toString(),
    notes = notes,
    isSynced = isSynced,
)

@Entity(tableName = "reminders")
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val typeOrdinal: Int,
    val title: String,
    val message: String,
    val scheduledAt: String,
    val isRecurring: Boolean = false,
    val intervalHours: Int = 24,
    val isActive: Boolean = true,
    val workManagerId: String = "",
) {
    fun toDomain(): Reminder = Reminder(
        id = id,
        type = ReminderType.values()[typeOrdinal],
        title = title,
        message = message,
        scheduledAt = LocalDateTime.parse(scheduledAt),
        isRecurring = isRecurring,
        intervalHours = intervalHours,
        isActive = isActive,
        workManagerId = workManagerId,
    )
}

fun Reminder.toEntity(): ReminderEntity = ReminderEntity(
    id = id,
    typeOrdinal = type.ordinal,
    title = title,
    message = message,
    scheduledAt = scheduledAt.toString(),
    isRecurring = isRecurring,
    intervalHours = intervalHours,
    isActive = isActive,
    workManagerId = workManagerId,
)
