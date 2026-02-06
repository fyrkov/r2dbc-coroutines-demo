package io.github.fyrkov.r2dbc_coroutines_demo.repository

import io.github.fyrkov.r2dbc_coroutines_demo.AbstractIntegrationTest
import io.github.fyrkov.r2dbc_coroutines_demo.TestTxHelper.runTestInTxAndRollback
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.awaitSingle
import org.jooq.DSLContext
import org.jooq.impl.DSL.field
import org.jooq.impl.DSL.table
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import reactor.core.publisher.Flux
import java.time.Instant

class OutboxRepositoryIntegrationTest @Autowired constructor(
    private val outboxRepository: OutboxRepository,
    private val dsl: DSLContext,
) : AbstractIntegrationTest() {

    @Test
    fun `should insert a record in the outbox`() = runTestInTxAndRollback(dsl) { dsl ->
        // given
        val aggregateType = "account"
        val aggregateId = "123"
        val payload = """{"balance":100}"""

        // when
        val id = OutboxRepository(dsl).insert(aggregateType, aggregateId, payload)
        assertNotNull(id)
    }

    @Test
    fun `should select unpublished records`() = runTestInTxAndRollback(dsl) {
        // when
        val records = outboxRepository.selectUnpublished(10)

        // then
        assertEquals(10, records.size)

        records.forEach { record ->
            assertNotNull(record.id)
            assertEquals("test_type", record.aggregateType)
            assertTrue(record.aggregateId.startsWith("test_id_"))
            assertEquals("{}", record.payload)
            assertNotNull(record.createdAt)
            assertNull(record.publishedAt)
        }
    }

    @Test
    fun `should select unpublished records as a flow`() = runTestInTxAndRollback(dsl) {
        // when
        val records = outboxRepository.selectUnpublishedAsFlow(10).toList()

        // then
        assertEquals(10, records.count())

        records.forEach { record ->
            assertNotNull(record.id)
            assertEquals("test_type", record.aggregateType)
            assertTrue(record.aggregateId.startsWith("test_id_"))
            assertEquals("{}", record.payload)
            assertNotNull(record.createdAt)
            assertNull(record.publishedAt)
        }
    }

    @Test
    fun `should update published_at`() = runTestInTxAndRollback(dsl) {
        // given
        val records = outboxRepository.selectUnpublished(1)
        val record = records.first()

        // when
        outboxRepository.updatePublishedAt(records.map { it.id })

        // then
        val updatedRecords = outboxRepository.selectUnpublished(10)
        assertFalse(updatedRecords.any { it.id == record.id })

        val updatedRecordQuery = dsl.selectFrom(table("outbox"))
            .where(field("id", Long::class.java).eq(record.id))
        val updatedRecord = Flux.from(updatedRecordQuery).awaitSingle()
        assertNotNull(updatedRecord.get(field("published_at", Instant::class.java)))
    }
}