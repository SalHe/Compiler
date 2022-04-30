package com.github.salhe.compiler.test

private object Utils

private val classLoader = Utils.javaClass.classLoader

fun getResourceAsStream(path: String) =
    classLoader.getResourceAsStream(path) ?: throw IllegalStateException("请在资源文件中放入$path")