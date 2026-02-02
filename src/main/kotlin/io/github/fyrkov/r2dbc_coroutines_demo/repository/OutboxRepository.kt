package io.github.fyrkov.r2dbc_coroutines_demo.repository

import io.github.fyrkov.r2dbc_coroutines_demo.domain.OutboxRecord
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitSingle
import org.jooq.DSLContext
import org.jooq.JSONB
import org.jooq.Record
import org.jooq.impl.DSL.*
import org.jooq.kotlin.coroutines.transactionCoroutine
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import java.time.Instant

@Repository
class OutboxRepository(
    private val dsl: DSLContext,
) {

    suspend fun <T> inTx(block: suspend (OutboxRepository) -> T): T =
        dsl.transactionCoroutine { cfg ->
            val txRepo = OutboxRepository(using(cfg))
            block(txRepo)
        }

    suspend fun insert(aggregateType: String, aggregateId: String, payload: String): Long {
        return dsl.insertInto(table("outbox"))
            .set(field("aggregate_type", String::class.java), aggregateType)
            .set(field("aggregate_id", String::class.java), aggregateId)
            .set(field("payload", JSONB::class.java), JSONB.valueOf(payload))
            .returning(field("id"))
            .awaitFirst()
            .get(field("id"), Long::class.java)
    }

    suspend fun selectUnpublished(limit: Int): List<OutboxRecord> {
        val query = dsl.selectFrom(table("outbox"))
            .where(field("published_at").isNull())
            .orderBy(field("id"))
            .limit(limit)
        return Flux.from(query)
            .map { deser(it) }
            .collectList()
            .awaitSingle()
    }

    fun selectUnpublished2(limit: Int): Flow<OutboxRecord> {
        val query = dsl.selectFrom(table("outbox"))
            .where(field("published_at").isNull())
            .orderBy(field("id"))
            .limit(limit)
        return Flux.from(query)
            .asFlow()
            .map { deser(it) }
    }

    private fun deser(record: Record): OutboxRecord = OutboxRecord(
        id = record.get(field("id", Long::class.java)),
        aggregateType = record.get(field("aggregate_type", String::class.java)),
        aggregateId = record.get(field("aggregate_id", String::class.java)),
        payload = record.get(field("payload", JSONB::class.java))?.data() ?: "{}",
        createdAt = record.get(field("created_at"), Instant::class.java),
        publishedAt = record.get(field("published_at"), Instant::class.java)
    )
}