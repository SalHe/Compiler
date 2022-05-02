package com.github.salhe.compiler.parser

import com.github.salhe.compiler.token.AbstractStream
import com.github.salhe.compiler.token.ReversibleStream
import com.github.salhe.compiler.token.Token
import java.util.*

class TokenStream(val tokens: List<Token>) : AbstractStream<Token>(),
    ReversibleStream<Token> {

    private var cur: Int = 0
    private val curStack = Stack<Int>()

    override fun read(): Token {
        return tokens[cur++]
    }

    override fun save() {
        curStack.push(cur)
    }

    override fun restore(): Int {
        val old = cur
        cur = curStack.pop()
        top = tokens[cur - 1]
        return old
    }

    override fun drop(): Int {
        return curStack.pop()
    }

    override val eof: Boolean
        get() = cur >= tokens.size

    override fun tell(): Int = cur - 1

}