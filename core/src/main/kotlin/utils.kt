package com.github.salhe.compiler

import com.github.salhe.compiler.parser.Parser
import com.github.salhe.compiler.token.Comment
import com.github.salhe.compiler.token.Token
import com.github.salhe.compiler.token.scanner.Scanner
import java.io.ByteArrayInputStream
import java.io.InputStreamReader

const val EOF = (-1).toChar()

fun Char.isEOF() = this == EOF

fun String.inputStreamReader() = InputStreamReader(ByteArrayInputStream(this.toByteArray()))
fun String.scan(lineSeparator: Boolean = false) = Scanner(this.inputStreamReader()).scan(lineSeparator)

fun String.parse(lineSeparator: Boolean = false) = Parser(this.scan(lineSeparator)).parse()

fun getResourceAsStream(path: String) =
    ClassLoader.getSystemResourceAsStream(path) ?: throw IllegalStateException("请在资源文件中放入$path")

fun Iterable<Token>.filterComment() = this.filter { it !is Comment }