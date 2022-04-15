package io.github.rk012.sourcelock

/**
 * A handler for a resource that can be written to.
 *
 * One instance of this class is created for each request to access a resource through a [ResourceDescriptor].
 *
 * @property descriptor The [ResourceDescriptor] for the resource.
 * @property content The content of the resource. Can be modified.
 */
class ResourceWriter<T> internal constructor(private val descriptor: ResourceDescriptor<T>, var content: T) {
    internal var active = true

    /**
     * Convenience method for [ResourceDescriptor.closeWriter]
     */
    internal fun close() {
        if (!active) return

        descriptor.closeWriter(this)
    }
}