package com.github.salhe.compiler.test

import org.junit.jupiter.api.Assertions

fun <T> Iterable<T>.assertEquals(expected: Iterable<T>, message: (() -> String)? = null) {
    Assertions.assertIterableEquals(expected, this, message)
}