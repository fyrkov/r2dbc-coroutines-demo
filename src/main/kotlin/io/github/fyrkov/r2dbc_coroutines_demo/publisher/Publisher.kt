package io.github.fyrkov.r2dbc_coroutines_demo.publisher

import io.github.fyrkov.r2dbc_coroutines_demo.domain.OutboxRecord
import io.github.fyrkov.r2dbc_coroutines_demo.repository.OutboxRepository
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class Publisher(
    private val outboxRepository: OutboxRepository
) {
    private val log = LoggerFactory.getLogger(javaClass)

    // TODO clarify suspend + @Scheduled
    @Scheduled(fixedRateString = "\${outbox.publish.interval}")
    @Transactional
    suspend fun publish() {
        val records: List<OutboxRecord> = outboxRepository.selectUnpublished(100)
        // FIXME: Dont do publication inside a transaction!
        log.info("Published {} records", records.size)
    }
}