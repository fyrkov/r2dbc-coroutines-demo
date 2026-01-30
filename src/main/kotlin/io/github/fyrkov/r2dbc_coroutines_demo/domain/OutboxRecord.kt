package io.github.fyrkov.r2dbc_coroutines_demo.domain

import java.time.Instant

data class OutboxRecord(
    val id: Long,
    val aggregateType: String,
    val aggregateId: String,
    val payload: String,
    val createdAt: Instant,
    val publishedAt: Instant? = null
)