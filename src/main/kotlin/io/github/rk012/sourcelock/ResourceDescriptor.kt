package io.github.rk012.sourcelock

import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * A handler for a resource.
 *
 * This class allows for other coroutines to wait for the resource to be available and manages a lock for the resource
 * such that only one coroutine can access the resource at a time. There should be only one instance of this class for
 * each resource.
 *
 * @param T the type of the resource.
 * @property resource initial value of the resource.
 * @constructor Creates a new resource handler.
 */
open class ResourceDescriptor<T>(private var resource: T) {
    private val queue = mutableListOf<ResourceQueueItem<T>>()
    private var locked = false

    open val isAvailable: Boolean
        get() = !locked && queue.isEmpty()

    /**
     * Returns a [ResourceWriter] for accessing the resource.
     *
     * Should never be called directly when accessing a resource, use [open] instead.
     *
     * @return A [ResourceWriter] instance.
     *
     * @see open
     */
    protected open fun getResourceWriter(): ResourceWriter<T> {
        return ResourceWriter(this, resource)
    }

    /**
     * Opens the resource for reading.
     *
     * This method will suspend until the resource is available. Automatically closes the resource when finished.
     *
     * @param priority The priority of the coroutine requesting the resource. Higher priorities are executed sooner.
     * Default is 0.
     * @param action The action to perform when the resource is available.
     *
     * @see ResourceWriter
     */
    suspend fun open(priority: Int = 0, action: suspend (ResourceWriter<T>) -> Unit) {
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
                    it,
                    priority
                )
            )
        }

        action(writer)
        writer.close()
    }

    /**
     * Closes the [ResourceWriter] and updates the resource.
     *
     * This method will resume the next coroutine in the queue if there is one.
     *
     * @param writer The writer to close.
     *
     * @throws IllegalStateException if the [ResourceWriter] is already closed.
     */
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
        queue.sortByDescending { it.priority }
        val item = queue.removeAt(0)
        item.continuation.resume(getResourceWriter())
    }
}