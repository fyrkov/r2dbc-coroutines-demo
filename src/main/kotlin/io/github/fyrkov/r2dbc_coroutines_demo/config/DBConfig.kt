package io.github.fyrkov.r2dbc_coroutines_demo.config

import org.flywaydb.core.Flyway
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import javax.sql.DataSource

@Configuration
class DBConfig {

    @Bean
    fun migrate(dataSource: DataSource) = ApplicationRunner {
        Flyway.configure()
            .dataSource(dataSource)
            .baselineOnMigrate(true)
            .load()
            .migrate()
    }
}