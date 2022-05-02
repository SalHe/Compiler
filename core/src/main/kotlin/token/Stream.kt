package com.github.salhe.compiler.token

import com.github.salhe.compiler.EOF
import com.github.salhe.compiler.isEOF
import java.io.InputStreamReader

interface Stream<T> {

    val eof: Boolean
    fun top(): T
    fun consume(): T
    fun tell(): Int

}

interface ReversibleStream<T> : Stream<T> {
    fun save()
    fun restore(): Int
    fun drop(): Int
}

abstract class AbstractStream<T> : Stream<T> {

    protected var top: T? = null
    private var init: Boolean = false

    override fun consume(): T {
        val old = top()
        if (!eof) top = read()
        return old
    }

    override fun top(): T {
        if (!init) {
            top = read()
            init = true
        }
        return top!!
    }

    abstract fun read(): T
}

interface CharStream : Stream<Char> {
    val row: Int
    val col: Int
}

@Suppress("FunctionName")
fun CharStream(sr: InputStreamReader): CharStream = object : CharStream {

    override val eof: Boolean
        get() = currentChar.isEOF()
    override val row: Int
        get() = _row
    override val col: Int
        get() = _col

    private var currentChar: Char = '\u0000' // 一定会被初始化输入流中首个字符
    private var pos: Int = 0

    @Suppress("ObjectPropertyName")
    private var _row: Int = 0

    @Suppress("ObjectPropertyName")
    private var _col: Int = 0


    init {
        read()
        if (!eof) _row = 1
    }

    private fun read() {
        currentChar = sr.read().toChar()
        pos++
        if (!eof) {
            if (currentChar == '\n') {
                _row++
                _col = -1
            }
            _col++
        }
    }

    override fun top(): Char {
        return currentChar
    }

    override fun consume(): Char {
        val old = currentChar
        if (currentChar != EOF)
            read()
        return old
    }

    override fun tell(): Int = pos

}
