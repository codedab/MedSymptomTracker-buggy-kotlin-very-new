package com.codedab.medsymptom.di

import android.content.Context
import com.codedab.medsymptom.data.local.dao.MedicationDao
import com.codedab.medsymptom.data.local.dao.ReminderDao
import com.codedab.medsymptom.data.local.dao.SymptomDao
import com.codedab.medsymptom.data.local.dao.VitalSignDao
import com.codedab.medsymptom.data.local.database.MedTrackerDatabase
import com.codedab.medsymptom.data.repository.MedicationRepository
import com.codedab.medsymptom.data.repository.MedicationRepositoryImpl
import com.codedab.medsymptom.data.repository.ReminderRepository
import com.codedab.medsymptom.data.repository.ReminderRepositoryImpl
import com.codedab.medsymptom.data.repository.SymptomRepository
import com.codedab.medsymptom.data.repository.SymptomRepositoryImpl
import com.codedab.medsymptom.data.repository.VitalSignRepository
import com.codedab.medsymptom.data.repository.VitalSignRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): MedTrackerDatabase =
        MedTrackerDatabase.getInstance(context)

    @Provides
    fun provideSymptomDao(db: MedTrackerDatabase): SymptomDao = db.symptomDao()

    @Provides
    fun provideMedicationDao(db: MedTrackerDatabase): MedicationDao = db.medicationDao()

    @Provides
    fun provideVitalSignDao(db: MedTrackerDatabase): VitalSignDao = db.vitalSignDao()

    @Provides
    fun provideReminderDao(db: MedTrackerDatabase): ReminderDao = db.reminderDao()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    abstract fun bindSymptomRepository(impl: SymptomRepositoryImpl): SymptomRepository

    @Binds
    abstract fun bindMedicationRepository(impl: MedicationRepositoryImpl): MedicationRepository

    @Binds
    abstract fun bindVitalSignRepository(impl: VitalSignRepositoryImpl): VitalSignRepository

    @Binds
    abstract fun bindReminderRepository(impl: ReminderRepositoryImpl): ReminderRepository
}
