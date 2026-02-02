package io.github.fyrkov.r2dbc_coroutines_demo

import kotlinx.coroutines.test.runTest
import org.jooq.DSLContext
import org.jooq.impl.DSL
import org.jooq.kotlin.coroutines.transactionCoroutine

object TestTxHelper {

    fun runTestInTxAndRollback(
        dsl: DSLContext,
        block: suspend (DSLContext) -> Unit,
    ) = runTest {
        try {
            dsl.transactionCoroutine { cfg ->
                val txDsl = DSL.using(cfg)

                block(txDsl)

                // force rollback
                throw Rollback()
            }
        } catch (_: Rollback) {
            // expected
        }
    }

    private class Rollback : RuntimeException()
}