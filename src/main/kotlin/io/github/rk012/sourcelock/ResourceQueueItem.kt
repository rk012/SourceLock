package io.github.rk012.sourcelock

import kotlin.coroutines.Continuation

/**
 * A class representing a queued request to a resource.
 *
 * @param continuation the continuation to be invoked when the resource is available.
 * @param priority the priority of the request.
 */
internal data class ResourceQueueItem<T>(
    val continuation: Continuation<ResourceWriter<T>>,
    val priority: Int
)
