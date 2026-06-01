package com.codedab.medsymptom

import app.cash.turbine.test
import com.codedab.medsymptom.data.local.dao.SymptomDao
import com.codedab.medsymptom.data.local.entity.SymptomEntity
import com.codedab.medsymptom.data.local.entity.MedicationEntity
import com.codedab.medsymptom.data.local.entity.toDomain
import com.codedab.medsymptom.data.local.entity.toEntity
import com.codedab.medsymptom.data.repository.SymptomRepositoryImpl
import com.codedab.medsymptom.domain.model.FrequencyUnit
import com.codedab.medsymptom.domain.model.Medication
import com.codedab.medsymptom.domain.model.Reminder
import com.codedab.medsymptom.domain.model.ReminderType
import com.codedab.medsymptom.domain.model.Severity
import com.codedab.medsymptom.domain.model.Symptom
import com.codedab.medsymptom.domain.model.VitalSign
import com.codedab.medsymptom.domain.model.VitalType
import com.codedab.medsymptom.worker.SyncWorker
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import org.junit.Test
import org.junit.Assert.fail
import androidx.work.NetworkType

class EntityMappingTest {

    private val baseDateTime = LocalDateTime.parse("2024-01-15T10:30:00")
    private val baseDate = LocalDate.parse("2024-01-15")

    // ── Bug 1 targets: severity off-by-one (severityOrdinal + 1) ─────────────

    @Test
    fun `symptom round-trip preserves MILD severity`() {
        // Bug 1: ordinal=0, [0+1]=MODERATE → fails
        val symptom = Symptom(
            name = "Headache",
            severity = Severity.MILD,
            recordedAt = baseDateTime,
        )
        val restored = symptom.toEntity().toDomain()
        assertThat(restored.severity).isEqualTo(Severity.MILD)
    }

    @Test
    fun `symptom round-trip preserves MODERATE severity`() {
        // Bug 1: ordinal=1, [1+1]=SEVERE → fails
        val symptom = Symptom(
            name = "Back Pain",
            severity = Severity.MODERATE,
            recordedAt = baseDateTime,
        )
        val restored = symptom.toEntity().toDomain()
        assertThat(restored.severity).isEqualTo(Severity.MODERATE)
    }

    @Test
    fun `symptom round-trip preserves SEVERE severity`() {
        // Bug 1: ordinal=2, [2+1]=CRITICAL → fails
        val symptom = Symptom(
            name = "Chest Pain",
            severity = Severity.SEVERE,
            recordedAt = baseDateTime,
        )
        val restored = symptom.toEntity().toDomain()
        assertThat(restored.severity).isEqualTo(Severity.SEVERE)
    }

    @Test
    fun `symptom round-trip with CRITICAL severity does not throw`() {
        // Bug 1 nuclear: ordinal=3, [3+1]=index 4 → ArrayIndexOutOfBoundsException
        // Caught here so it registers as a test failure not a suite crash
        val symptom = Symptom(
            name = "Cardiac Event",
            severity = Severity.CRITICAL,
            recordedAt = baseDateTime,
        )
        try {
            val restored = symptom.toEntity().toDomain()
            assertThat(restored.severity).isEqualTo(Severity.CRITICAL)
        } catch (e: ArrayIndexOutOfBoundsException) {
            fail("toDomain() threw ArrayIndexOutOfBoundsException for CRITICAL severity — " +
                 "severityOrdinal offset is wrong (off-by-one bug): ${e.message}")
        }
    }

    @Test
    fun `SymptomEntity with ordinal 0 maps to MILD not MODERATE`() {
        // Direct entity test — bypasses toEntity(), hits toDomain() directly
        // Bug 1: [0+1]=MODERATE, expected MILD → fails
        val entity = SymptomEntity(
            name = "Test",
            severityOrdinal = 0,
            recordedAt = baseDateTime.toString(),
        )
        val domain = entity.toDomain()
        assertThat(domain.severity).isEqualTo(Severity.MILD)
    }

    // ── Bug 2 targets: inverted null check on endDate ─────────────────────────

    @Test
    fun `medication round-trip preserves non-null endDate`() {
        // Bug 2: endDate != null → returns null → fails
        val endDate = LocalDate(2025, 6, 30)
        val medication = Medication(
            name = "Aspirin",
            dosage = "100mg",
            frequencyValue = 1,
            frequencyUnit = FrequencyUnit.DAYS,
            startDate = baseDate,
            endDate = endDate,
        )
        val restored = medication.toEntity().toDomain()
        assertThat(restored.endDate).isEqualTo(LocalDate(2025, 6, 30))
    }

    @Test
    fun `medication round-trip preserves null endDate`() {
        // Bug 2: endDate == null → returns LocalDate.parse(startDate) not null → fails
        val medication = Medication(
            name = "Aspirin",
            dosage = "100mg",
            frequencyValue = 1,
            frequencyUnit = FrequencyUnit.DAYS,
            startDate = baseDate,
            endDate = null,
        )
        val restored = medication.toEntity().toDomain()
        assertThat(restored.endDate).isNull()
    }

    @Test
    fun `MedicationEntity with null endDate maps to null not startDate`() {
        // Direct entity test — hits toDomain() directly
        // Bug 2: endDate==null → LocalDate.parse(startDate) returned instead of null → fails
        val entity = MedicationEntity(
            name = "Ibuprofen",
            dosage = "200mg",
            frequencyValue = 2,
            frequencyUnitOrdinal = FrequencyUnit.DAYS.ordinal,
            startDate = "2024-01-15",
            endDate = null,
        )
        val domain = entity.toDomain()
        assertThat(domain.endDate).isNull()
    }

    @Test
    fun `MedicationEntity with non-null endDate preserves it`() {
        // Bug 2: endDate != null → null returned instead of parsed date → fails
        val entity = MedicationEntity(
            name = "Ibuprofen",
            dosage = "200mg",
            frequencyValue = 2,
            frequencyUnitOrdinal = FrequencyUnit.DAYS.ordinal,
            startDate = "2024-01-15",
            endDate = "2025-06-30",
        )
        val domain = entity.toDomain()
        assertThat(domain.endDate).isEqualTo(LocalDate(2025, 6, 30))
    }

    // ── Stable tests (pass on both working and buggy) ─────────────────────────

    @Test
    fun `vital sign round-trip preserves compound value`() {
        val vital = VitalSign(
            type = VitalType.BLOOD_PRESSURE,
            value = "120/80",
            recordedAt = baseDateTime,
        )
        val restored = vital.toEntity().toDomain()
        assertThat(restored.value).isEqualTo("120/80")
    }

    @Test
    fun `all four severity ordinals are correct`() {
        assertThat(Severity.MILD.ordinal).isEqualTo(0)
        assertThat(Severity.MODERATE.ordinal).isEqualTo(1)
        assertThat(Severity.SEVERE.ordinal).isEqualTo(2)
        assertThat(Severity.CRITICAL.ordinal).isEqualTo(3)
    }

    @Test
    fun `isSynced defaults to false on new SymptomEntity`() {
        val entity = SymptomEntity(
            name = "Test",
            severityOrdinal = 0,
            recordedAt = baseDateTime.toString(),
        )
        assertThat(entity.isSynced).isFalse()
    }
}

class SymptomRepositoryTest {

    private val dao = mockk<SymptomDao>()
    private val repo = SymptomRepositoryImpl(dao)

    private val baseDateTime = LocalDateTime.parse("2024-01-15T10:30:00")

    @Test
    fun `getAll maps MILD entity to MILD domain`() = runTest {
        // Bug 1: entity with ordinal=0 should map to MILD, not MODERATE
        val entity = SymptomEntity(
            id = 1L,
            name = "Headache",
            severityOrdinal = Severity.MILD.ordinal,
            recordedAt = baseDateTime.toString(),
        )
        coEvery { dao.getAllSymptoms() } returns flowOf(listOf(entity))

        repo.getAll().test {
            val result = awaitItem()
            assertThat(result).hasSize(1)
            assertThat(result[0].severity).isEqualTo(Severity.MILD)
            awaitComplete()
        }
    }

    @Test
    fun `save calls dao insertSymptom and returns id`() = runTest {
        val symptom = Symptom(
            name = "Nausea",
            severity = Severity.MODERATE,
            recordedAt = baseDateTime,
        )
        coEvery { dao.insertSymptom(any()) } returns 99L

        val id = repo.save(symptom)
        assertThat(id).isEqualTo(99L)
        coVerify(exactly = 1) { dao.insertSymptom(any()) }
    }

    @Test
    fun `markSynced calls dao markSynced exactly once`() = runTest {
        coEvery { dao.markSynced(42L) } returns Unit
        coEvery { dao.getCount() } returns 0

        repo.markSynced(42L)

        coVerify(exactly = 1) { dao.markSynced(42L) }
        coVerify(exactly = 0) { dao.getCount() }
    }

    @Test
    fun `getById returns null when dao returns null`() = runTest {
        coEvery { dao.getSymptomById(99L) } returns null

        val result = repo.getById(99L)
        assertThat(result).isNull()
    }
}

class DomainModelTest {

    @Test
    fun `severity values increase in order`() {
        assertThat(Severity.MILD.value).isLessThan(Severity.MODERATE.value)
        assertThat(Severity.MODERATE.value).isLessThan(Severity.SEVERE.value)
        assertThat(Severity.SEVERE.value).isLessThan(Severity.CRITICAL.value)
    }

    @Test
    fun `all VitalType entries have non-empty displayName and unit`() {
        for (type in VitalType.values()) {
            assertThat(type.displayName).isNotEmpty()
            assertThat(type.unit).isNotEmpty()
        }
    }

    @Test
    fun `Reminder default intervalHours is 24`() {
        val reminder = Reminder(
            type = ReminderType.MEDICATION,
            title = "Take pill",
            message = "Time to take your medication",
            scheduledAt = LocalDateTime.parse("2024-01-15T08:00:00"),
        )
        assertThat(reminder.intervalHours).isEqualTo(24)
    }

    @Test
    fun `Reminder default isActive is true`() {
        val reminder = Reminder(
            type = ReminderType.VITAL_CHECK,
            title = "Check BP",
            message = "Time to check blood pressure",
            scheduledAt = LocalDateTime.parse("2024-01-15T09:00:00"),
        )
        assertThat(reminder.isActive).isTrue()
    }
}

class WorkerConstraintTest {

    @Test
    fun `SyncWorker periodic request requires connected network`() {
        val workRequest = SyncWorker.buildPeriodicRequest()
        val constraints = workRequest.workSpec.constraints
        assertThat(constraints.requiredNetworkType).isEqualTo(NetworkType.CONNECTED)
    }
}