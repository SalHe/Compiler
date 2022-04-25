package com.github.salhe.compiler.test.token

import com.github.salhe.compiler.test.getResourceAsStream
import com.github.salhe.compiler.token.*
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class ScannerTest {

    @Test
    fun `A Complicated C Source Test`() {
        val tokens = Scanner(getResourceAsStream("token/hello.c").reader()).scan()
        val expectedTokens = listOf<Token>(

            PrimitiveType.void,
            Identifier("main"),
            Punctuation.LBracket,
            Punctuation.RBracket,
            Punctuation.LCurlyBracket,

            PrimitiveType.int,
            Identifier("a"),
            Operator.Assign,
            Literal.IntegerLiteral("5"),
            Punctuation.Semicolon,

            PrimitiveType.int,
            Identifier("b"),
            Operator.Assign,
            Literal.IntegerLiteral("5"),
            Operator.Plus,
            Literal.IntegerLiteral("10"),
            Operator.Multiply,
            Literal.IntegerLiteral("3"),
            Punctuation.Semicolon,

            Identifier("print"),
            Punctuation.LBracket,
            Literal.StringLiteral("Hello!"),
            Punctuation.RBracket,
            Punctuation.Semicolon,

            Keyword.If,
            Punctuation.LBracket,
            Identifier("a"),
            Operator.Equals,
            Literal.IntegerLiteral("5"),
            Punctuation.RBracket,
            Punctuation.LCurlyBracket,

            Identifier("print"),
            Punctuation.LBracket,
            Literal.StringLiteral("a == 5"),
            Punctuation.RBracket,
            Punctuation.Semicolon,

            Punctuation.RCurlyBracket,

            Keyword.Else,
            Keyword.If,
            Punctuation.LBracket,
            Identifier("a"),
            Operator.Lesser,
            Literal.IntegerLiteral("5"),
            Punctuation.RBracket,
            Punctuation.LCurlyBracket,

            Identifier("print"),
            Punctuation.LBracket,
            Literal.StringLiteral("a<5"),
            Punctuation.RBracket,
            Punctuation.Semicolon,

            Punctuation.RCurlyBracket,

            Keyword.While,
            Punctuation.LBracket,
            Operator.LogicNot,
            Identifier("b"),
            Punctuation.RBracket,
            Punctuation.LCurlyBracket,

            Identifier("print"),
            Punctuation.LBracket,
            Literal.StringLiteral("b!=0"),
            Punctuation.RBracket,
            Punctuation.Semicolon,

            Identifier("b"),
            Operator.MinusMinus,
            Punctuation.Semicolon,

            Punctuation.RCurlyBracket,

            Keyword.If,
            Punctuation.LBracket,
            Identifier("a"),
            Operator.NotEquals,
            Identifier("b"),
            Punctuation.RBracket,
            Punctuation.LCurlyBracket,

            Identifier("print"),
            Punctuation.LBracket,
            Literal.StringLiteral("a!=b"),
            Punctuation.RBracket,
            Punctuation.Semicolon,

            Punctuation.RCurlyBracket,

            Identifier("a"),
            Operator.PlusPlus,
            Punctuation.Semicolon,

            Punctuation.RCurlyBracket
        )
        Assertions.assertIterableEquals(expectedTokens, tokens)
    }

}