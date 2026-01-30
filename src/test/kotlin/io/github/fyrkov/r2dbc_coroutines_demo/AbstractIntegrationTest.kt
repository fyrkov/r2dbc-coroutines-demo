package io.github.fyrkov.r2dbc_coroutines_demo

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.testcontainers.postgresql.PostgreSQLContainer

@SpringBootTest
abstract class AbstractIntegrationTest {

    companion object {
        @JvmStatic
        @ServiceConnection
        val postgresContainer: PostgreSQLContainer = PostgreSQLContainer("postgres:17")
            .withDatabaseName("postgres")
            .withUsername("postgres")
            .withPassword("secret")
            .apply { start() }
    }
}
