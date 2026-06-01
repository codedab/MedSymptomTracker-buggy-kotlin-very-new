package com.codedab.medsymptom.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.codedab.medsymptom.data.local.entity.MedicationEntity
import com.codedab.medsymptom.data.local.entity.ReminderEntity
import com.codedab.medsymptom.data.local.entity.SymptomEntity
import com.codedab.medsymptom.data.local.entity.VitalSignEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SymptomDao {
    @Query("SELECT * FROM symptoms ORDER BY recordedAt DESC")
    fun getAllSymptoms(): Flow<List<SymptomEntity>>

    @Query("SELECT * FROM symptoms WHERE id = :id")
    suspend fun getSymptomById(id: Long): SymptomEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSymptom(entity: SymptomEntity): Long

    @Query("UPDATE symptoms SET isSynced = 1 WHERE id = :id")
    suspend fun markSynced(id: Long)

    @Query("SELECT COUNT(*) FROM symptoms")
    suspend fun getCount(): Int

    @Query("SELECT * FROM symptoms WHERE isSynced = 0")
    suspend fun getUnsyncedSymptoms(): List<SymptomEntity>
}

@Dao
interface MedicationDao {
    @Query("SELECT * FROM medications ORDER BY startDate DESC")
    fun getAllMedications(): Flow<List<MedicationEntity>>

    @Query("SELECT * FROM medications WHERE id = :id")
    suspend fun getMedicationById(id: Long): MedicationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedication(entity: MedicationEntity): Long

    @Query("UPDATE medications SET isSynced = 1 WHERE id = :id")
    suspend fun markSynced(id: Long)

    @Query("SELECT * FROM medications WHERE isSynced = 0")
    suspend fun getUnsyncedMedications(): List<MedicationEntity>
}

@Dao
interface VitalSignDao {
    @Query("SELECT * FROM vital_signs ORDER BY recordedAt DESC")
    fun getAllVitalSigns(): Flow<List<VitalSignEntity>>

    @Query("SELECT * FROM vital_signs WHERE id = :id")
    suspend fun getVitalSignById(id: Long): VitalSignEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVitalSign(entity: VitalSignEntity): Long

    @Query("UPDATE vital_signs SET isSynced = 1 WHERE id = :id")
    suspend fun markSynced(id: Long)

    @Query("SELECT * FROM vital_signs WHERE isSynced = 0")
    suspend fun getUnsyncedVitals(): List<VitalSignEntity>
}

@Dao
interface ReminderDao {
    @Query("SELECT * FROM reminders ORDER BY scheduledAt ASC")
    fun getAllReminders(): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders WHERE id = :id")
    suspend fun getReminderById(id: Long): ReminderEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(entity: ReminderEntity): Long

    @Query("DELETE FROM reminders WHERE id = :id")
    suspend fun deleteReminder(id: Long)
}
