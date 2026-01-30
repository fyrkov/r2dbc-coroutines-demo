package io.github.fyrkov.r2dbc_coroutines_demo

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class R2dbcCoroutinesDemoApplication

fun main(args: Array<String>) {
	runApplication<R2dbcCoroutinesDemoApplication>(*args)
}
