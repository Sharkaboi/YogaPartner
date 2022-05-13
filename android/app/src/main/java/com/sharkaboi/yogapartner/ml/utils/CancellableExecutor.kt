package com.sharkaboi.yogapartner.ml.utils

import java.util.concurrent.Executor
import java.util.concurrent.atomic.AtomicBoolean

class CancellableExecutor(private val executor: Executor) : Executor {
    private val shutdown = AtomicBoolean() // false

    override fun execute(command: Runnable) {
        // Return early if this object has been shut down.
        if (shutdown.get()) {
            return
        }

        val block = Runnable {
            // Check again in case it has been shut down in the mean time.
            if (shutdown.get()) {
                return@Runnable
            }
            command.run()
        }

        executor.execute(block)
    }

    fun shutdown() {
        shutdown.set(true)
    }
}
