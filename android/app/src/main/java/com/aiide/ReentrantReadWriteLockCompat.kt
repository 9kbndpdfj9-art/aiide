package com.aiide

import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.withLock

inline fun <T> ReentrantReadWriteLock.write(action: () -> T): T {
    return writeLock().withLock(action)
}
