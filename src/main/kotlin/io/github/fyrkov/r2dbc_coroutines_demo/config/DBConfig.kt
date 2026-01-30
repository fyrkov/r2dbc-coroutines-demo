package io.github.fyrkov.r2dbc_coroutines_demo.config

import io.r2dbc.spi.ConnectionFactory
import org.jooq.DSLContext
import org.jooq.SQLDialect
import org.jooq.impl.DSL
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class DBConfig {

    @Bean
    fun dslContext(cf: ConnectionFactory): DSLContext =
        DSL.using(cf, SQLDialect.POSTGRES)
}