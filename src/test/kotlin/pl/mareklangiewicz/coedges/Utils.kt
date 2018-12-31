package pl.mareklangiewicz.coedges

import org.junit.Assert.assertTrue

infix fun <T> Collection<T>.has(element: T) { assertTrue("Does not have $element", element in this) }

infix fun <T> Collection<T>.hasNot(element: T) { assertTrue("Does have $element", element !in this) }
