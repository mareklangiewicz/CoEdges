package pl.mareklangiewicz.coedges

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import java.util.Locale

infix fun <T> Collection<T>.has(element: T) { assertTrue(element in this) { "Does not have $element" } }

infix fun <T> Collection<T>.hasNot(element: T) { assertTrue(element !in this) { "Does have $element" } }

infix fun <T> Collection<T>.has(predicate: (T) -> Boolean) { assertTrue(this.any(predicate)) }

infix fun <T> Collection<T>.hasNot(predicate: (T) -> Boolean) { assertFalse(this.any(predicate)) }

val now get() = System.currentTimeMillis().let { String.format(Locale.US, "%tT:%tL", it, it) }

/**
 * Logs given data to console with additional info and returns the same data (kinda like the tee unix command)
 */
@Suppress("unused")
val <T> T.tee get() = also { println("tee [${Thread.currentThread().name.padEnd(40).substring(0, 40)}] [$now] $it") }

@Suppress("unused")
val Any?.unit get() = Unit
