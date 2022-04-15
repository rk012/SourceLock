package io.github.rk012.sourcelock

import kotlin.coroutines.Continuation

internal data class ResourceQueueItem<T>(
    val continuation: Continuation<ResourceWriter<T>>
)
