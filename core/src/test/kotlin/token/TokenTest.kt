package com.github.salhe.compiler.test.token

import com.github.salhe.compiler.getResourceAsStream
import com.github.salhe.compiler.token.Literal
import com.github.salhe.compiler.token.PrimitiveType
import com.github.salhe.compiler.token.Punctuation
import com.github.salhe.compiler.token.Token
import com.github.salhe.compiler.token.scanner.Scanner
import org.junit.jupiter.api.*
import kotlin.test.assertNotEquals

class TokenTest {

    @Test
    fun equalsTest() {
        // 正常来说两次扫描token序列一样，但是内存中不是同一对象
        val tokens = Scanner(getResourceAsStream("token/hello.c").reader()).scan()
        val tokens2 = Scanner(getResourceAsStream("token/hello.c").reader()).scan()
        Assertions.assertIterableEquals(tokens, tokens2)
    }

    @Test
    fun `Same class test`() {
        assertNotEquals<Token>(PrimitiveType.int, PrimitiveType.void)
        assertNotEquals<Token>(Literal.StringLiteral("str1"), Literal.StringLiteral("str2"))

        // 有个很奇怪的问题
        // 如果单纯用代码 assertNotEquals<Token>(Punctuation.Semicolon, Punctuation.Comma)
        // 那么 Punctuation.all里Punctuation.Semicolon元素会变为null，但是交换的话，没有任何影响
        // 不懂为什么。。。
        assertNotEquals<Token>(Punctuation.Comma, Punctuation.Semicolon)
        assertNotEquals<Token>(Punctuation.Semicolon, Punctuation.Comma)
    }

    @Test
    fun `Different class test`() {
        assertNotEquals<Token>(Punctuation.Semicolon, PrimitiveType.void)
        assertNotEquals<Token>(Punctuation.Semicolon, Literal.StringLiteral("str1"))
    }
}