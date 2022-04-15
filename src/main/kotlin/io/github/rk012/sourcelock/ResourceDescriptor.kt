package io.github.rk012.sourcelock

import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

open class ResourceDescriptor<T>(private var resource: T) {
    private val queue = mutableListOf<ResourceQueueItem<T>>()
    private var locked = false

    open val isAvailable: Boolean
        get() = !locked && queue.isEmpty()

    protected open fun getResourceWriter(): ResourceWriter<T> {
        return ResourceWriter(this, resource)
    }

    suspend fun open(action: suspend (ResourceWriter<T>) -> Unit) {
        if (isAvailable) {
            locked = true
            val writer = getResourceWriter()
            action(writer)
            writer.close()

            return
        }

        val writer = suspendCoroutine<ResourceWriter<T>> {
            queue.add (
                ResourceQueueItem(
                    it
                )
            )
        }

        action(writer)
        writer.close()
    }

    open fun closeWriter(writer: ResourceWriter<T>) {
        if (!writer.active) {
            throw IllegalStateException("ResourceWriter is not active")
        }

        resource = writer.content
        writer.active = false
        startNext()
    }

    private fun startNext() {
        if (queue.isEmpty()) {
            locked = false
            return
        }

        locked = true
        val item = queue.removeAt(0)
        item.continuation.resume(getResourceWriter())
    }
}