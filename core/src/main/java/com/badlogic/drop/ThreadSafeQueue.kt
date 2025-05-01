package com.badlogic.drop

import java.util.LinkedList
import java.util.Queue
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock


class ThreadSafeQueue<T> {
    private val queue: Queue<T> = LinkedList()
    private val lock = ReentrantLock()

    fun enqueue(item: T) = lock.withLock {
        queue.offer(item)
    }

    fun dequeue(): T? = lock.withLock {
        queue.poll() // Returns null if empty
    }
    fun size(): Int = lock.withLock {
        queue.size
    }
}
