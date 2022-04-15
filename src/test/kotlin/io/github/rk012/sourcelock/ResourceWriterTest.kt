package io.github.rk012.sourcelock

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.test.*

class ResourceWriterTest {
    private suspend fun coroutineA(r: ResourceDescriptor<String>) {
        r.open {
            delay(1000)
            it.content += " A"
        }
    }

    private suspend fun coroutineB(r: ResourceDescriptor<String>) {
        r.open(1) {
            delay(2000)
            it.content += " B"
        }
    }

    private suspend fun coroutineC(r: ResourceDescriptor<String>) {
        r.open(2) {
            delay(2000)
            it.content += " C"
        }
    }

    @Test
    fun resourceWriterTest() = runBlocking {
        val r = ResourceDescriptor("test")

        coroutineScope {
            launch { coroutineB(r) }
            delay(500)
            launch { coroutineA(r) }
        }

        assertTrue(r.isAvailable)

        r.open {
            assertEquals("test B A", it.content)
        }
    }

    @Test
    fun resourceQueueTest() = runBlocking {
        val r = ResourceDescriptor("test")

        coroutineScope {
            launch { coroutineA(r) }
            delay(250)
            launch { coroutineB(r) }
            delay(250)
            launch { coroutineC(r) }
        }

        assertTrue(r.isAvailable)

        r.open {
            assertEquals("test A C B", it.content)
        }
    }
}