package io.github.rk012.sourcelock

class ResourceWriter<T> internal constructor(private val descriptor: ResourceDescriptor<T>, var content: T) {
    internal var active = true

    fun close() {
        if (!active) return

        descriptor.closeWriter(this)
    }
}