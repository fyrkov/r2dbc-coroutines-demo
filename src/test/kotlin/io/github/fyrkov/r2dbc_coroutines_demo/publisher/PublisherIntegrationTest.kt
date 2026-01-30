package io.github.fyrkov.r2dbc_coroutines_demo.publisher

import io.github.fyrkov.r2dbc_coroutines_demo.AbstractIntegrationTest
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class PublisherIntegrationTest @Autowired constructor(
    private val publisher: Publisher,
) : AbstractIntegrationTest() {

    @Test
    fun publish() = runTest {
        publisher.publish()
    }
}