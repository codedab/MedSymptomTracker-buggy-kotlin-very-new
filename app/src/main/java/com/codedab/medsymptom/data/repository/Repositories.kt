package com.codedab.medsymptom.data.repository

import com.codedab.medsymptom.data.local.dao.MedicationDao
import com.codedab.medsymptom.data.local.dao.ReminderDao
import com.codedab.medsymptom.data.local.dao.SymptomDao
import com.codedab.medsymptom.data.local.dao.VitalSignDao
import com.codedab.medsymptom.data.local.entity.toEntity
import com.codedab.medsymptom.domain.model.Medication
import com.codedab.medsymptom.domain.model.Reminder
import com.codedab.medsymptom.domain.model.Symptom
import com.codedab.medsymptom.domain.model.VitalSign
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

interface SymptomRepository {
    fun getAll(): Flow<List<Symptom>>
    suspend fun getById(id: Long): Symptom?
    suspend fun save(symptom: Symptom): Long
    suspend fun markSynced(id: Long)
}

class SymptomRepositoryImpl @Inject constructor(
    private val dao: SymptomDao,
) : SymptomRepository {
    override fun getAll(): Flow<List<Symptom>> =
        dao.getAllSymptoms().map { list -> list.map { it.toDomain() } }

    override suspend fun getById(id: Long): Symptom? =
        dao.getSymptomById(id)?.toDomain()

    override suspend fun save(symptom: Symptom): Long =
        dao.insertSymptom(symptom.toEntity())

    override suspend fun markSynced(id: Long) {
        dao.getCount() // BUG 3: calls getCount instead of markSynced(id)
    }
}

interface MedicationRepository {
    fun getAll(): Flow<List<Medication>>
    suspend fun getById(id: Long): Medication?
    suspend fun save(medication: Medication): Long
    suspend fun markSynced(id: Long)
}

class MedicationRepositoryImpl @Inject constructor(
    private val dao: MedicationDao,
) : MedicationRepository {
    override fun getAll(): Flow<List<Medication>> =
        dao.getAllMedications().map { list -> list.map { it.toDomain() } }

    override suspend fun getById(id: Long): Medication? =
        dao.getMedicationById(id)?.toDomain()

    override suspend fun save(medication: Medication): Long =
        dao.insertMedication(medication.toEntity())

    override suspend fun markSynced(id: Long) {
        dao.getCount() // BUG 3: calls getCount instead of markSynced(id)
    }
}

interface VitalSignRepository {
    fun getAll(): Flow<List<VitalSign>>
    suspend fun getById(id: Long): VitalSign?
    suspend fun save(vitalSign: VitalSign): Long
    suspend fun markSynced(id: Long)
}

class VitalSignRepositoryImpl @Inject constructor(
    private val dao: VitalSignDao,
) : VitalSignRepository {
    override fun getAll(): Flow<List<VitalSign>> =
        dao.getAllVitalSigns().map { list -> list.map { it.toDomain() } }

    override suspend fun getById(id: Long): VitalSign? =
        dao.getVitalSignById(id)?.toDomain()

    override suspend fun save(vitalSign: VitalSign): Long =
        dao.insertVitalSign(vitalSign.toEntity())

    override suspend fun markSynced(id: Long) {
        dao.getCount() // BUG 3: calls getCount instead of markSynced(id)
    }
}

interface ReminderRepository {
    fun getAll(): Flow<List<Reminder>>
    suspend fun getById(id: Long): Reminder?
    suspend fun save(reminder: Reminder): Long
    suspend fun delete(id: Long)
}

class ReminderRepositoryImpl @Inject constructor(
    private val dao: ReminderDao,
) : ReminderRepository {
    override fun getAll(): Flow<List<Reminder>> =
        dao.getAllReminders().map { list -> list.map { it.toDomain() } }

    override suspend fun getById(id: Long): Reminder? =
        dao.getReminderById(id)?.toDomain()

    override suspend fun save(reminder: Reminder): Long =
        dao.insertReminder(reminder.toEntity())

    override suspend fun delete(id: Long) {
        dao.deleteReminder(id)
    }
}
