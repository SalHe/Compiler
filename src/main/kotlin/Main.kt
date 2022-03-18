package com.github.salhe.compiler

import com.github.salhe.compiler.token.Scanner
import java.io.InputStreamReader

object Main

fun main() {
    val stream = InputStreamReader(
        Main.javaClass.classLoader.getResourceAsStream("hello.c") ?: throw IllegalStateException("请在资源文件中放入hello.c")
    )

    val tokens = Scanner(stream).scan()

    println(tokens.joinToString("\n"))
}