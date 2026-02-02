package io.github.fyrkov.r2dbc_coroutines_demo.repository

import io.github.fyrkov.r2dbc_coroutines_demo.AbstractIntegrationTest
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.jooq.DSLContext
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

//@Transactional
//@Rollback
class OutboxRepositoryIntegrationTest @Autowired constructor(
    private val outboxRepository: OutboxRepository,
    private val dsl: DSLContext,
) : AbstractIntegrationTest() {


    @Test
    fun `should insert a record in the outbox`() = runTest {
        // given
        val aggregateType = "account"
        val aggregateId = "123"
        val payload = """{"balance":100}"""

        // when
        outboxRepository.inTx {
            try {
                val id = outboxRepository.insert(aggregateType, aggregateId, payload)
                assertNotNull(id)
                throw RuntimeException()
            } catch (_: RuntimeException) {}
        }
    }

    @Test
    fun `should select unpublished records`() = runTest {
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
    fun `should select unpublished records as a flow`() = runTest {
        // when
        val records = outboxRepository.selectUnpublished2(10).toList()

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
}