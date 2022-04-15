package io.github.rk012.sourcelock

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.test.*

class ResourceWriterTest {
    private suspend fun coroutineA(r: ResourceDescriptor<String>) {
        delay(500)
        r.open {
            delay(1000)
            it.content += " A"
        }

        assertTrue(r.isAvailable)
    }

    private suspend fun coroutineB(r: ResourceDescriptor<String>) {
        r.open {
            delay(2000)
            it.content += " B"
        }

        assertFalse(r.isAvailable)
    }

    @Test
    fun resourceWriterTest() = runBlocking {
        val r = ResourceDescriptor("test")

        coroutineScope {
            launch { coroutineA(r) }
            launch { coroutineB(r) }
        }

        r.open {
            assertEquals("test B A", it.content)
        }
    }
}