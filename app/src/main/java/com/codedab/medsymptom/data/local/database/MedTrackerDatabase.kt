package com.codedab.medsymptom.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.codedab.medsymptom.data.local.dao.MedicationDao
import com.codedab.medsymptom.data.local.dao.ReminderDao
import com.codedab.medsymptom.data.local.dao.SymptomDao
import com.codedab.medsymptom.data.local.dao.VitalSignDao
import com.codedab.medsymptom.data.local.entity.MedicationEntity
import com.codedab.medsymptom.data.local.entity.ReminderEntity
import com.codedab.medsymptom.data.local.entity.SymptomEntity
import com.codedab.medsymptom.data.local.entity.VitalSignEntity
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory
import java.security.SecureRandom

@Database(
    entities = [SymptomEntity::class, MedicationEntity::class, VitalSignEntity::class, ReminderEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class MedTrackerDatabase : RoomDatabase() {
    abstract fun symptomDao(): SymptomDao
    abstract fun medicationDao(): MedicationDao
    abstract fun vitalSignDao(): VitalSignDao
    abstract fun reminderDao(): ReminderDao

    companion object {
        private const val DB_NAME = "med_tracker.db"
        private const val PREFS_NAME = "med_tracker_prefs"
        private const val KEY_PASSPHRASE = "db_passphrase"

        @Volatile
        private var instance: MedTrackerDatabase? = null

        fun getInstance(context: Context): MedTrackerDatabase {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }
        }

        private fun buildDatabase(context: Context): MedTrackerDatabase {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            val prefs = EncryptedSharedPreferences.create(
                context,
                PREFS_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
            )

            val passphrase: ByteArray = if (prefs.contains(KEY_PASSPHRASE)) {
                android.util.Base64.decode(prefs.getString(KEY_PASSPHRASE, null)!!, android.util.Base64.DEFAULT)
            } else {
                val bytes = ByteArray(32)
                SecureRandom().nextBytes(bytes)
                prefs.edit().putString(KEY_PASSPHRASE, android.util.Base64.encodeToString(bytes, android.util.Base64.DEFAULT)).apply()
                bytes
            }

            val factory = SupportFactory(SQLiteDatabase.getBytes(
                String(passphrase, Charsets.UTF_8).toCharArray(),
            ))

            return Room.databaseBuilder(context, MedTrackerDatabase::class.java, DB_NAME)
                .openHelperFactory(factory)
                .build()
        }
    }
}
