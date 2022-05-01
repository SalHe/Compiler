package com.github.salhe.compiler.test.token.scanner

import com.github.salhe.compiler.getResourceAsStream
import com.github.salhe.compiler.scan
import com.github.salhe.compiler.token.*
import com.github.salhe.compiler.token.scanner.Scanner
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import kotlin.test.assertNotEquals

fun Iterable<Token>.assertEquals(expected: Iterable<Token>, message: (() -> String)? = null) {
    Assertions.assertIterableEquals(expected, this, message)
}

/**
 * 正常的词法分析测试。
 *
 */
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

    @Test
    fun `Simple Test`() {
        """
            int he;;if(a )b{}while do print; void
        """.trimIndent()
            .scan()
            .assertEquals(
                listOf(
                    PrimitiveType.int,
                    Identifier("he"),
                    Punctuation.Semicolon,
                    Punctuation.Semicolon,
                    Keyword.If,
                    Punctuation.LBracket,
                    Identifier("a"),
                    Punctuation.RBracket,
                    Identifier("b"),
                    Punctuation.LCurlyBracket,
                    Punctuation.RCurlyBracket,
                    Keyword.While,
                    Keyword.Do,
                    Identifier("print"),
                    Punctuation.Semicolon,
                    PrimitiveType.void
                )
            )
    }

    @Test
    fun `Keyword Test`() {
        """
            do while if else
        """.trimIndent()
            .scan()
            .assertEquals(listOf(Keyword.Do, Keyword.While, Keyword.If, Keyword.Else))
    }

    @Test
    fun `Operator Test`() {
        // 目前运算符比较特殊（运算符和其他符合必须由空格分隔，或者以非运算符字符集的字符接壤）
        // 比如 +++++ 会认为是一个运算符（尽管没有这个运算符，该符号不会进入token流中）
        // 比如 ++ +++ 会认为是两个运算符，因为他们以空格分隔
        // 比如 ++i 会认为是一个自增运算符和一个标识符i
        """
            a + b = 5 / 10 * 50 - c * void--;
        """.trimIndent()
            .scan()
            .assertEquals(
                listOf(
                    Identifier("a"),
                    Operator.Plus,
                    Identifier("b"),
                    Operator.Assign,
                    Literal.IntegerLiteral("5"),
                    Operator.Divide,
                    Literal.IntegerLiteral("10"),
                    Operator.Multiply,
                    Literal.IntegerLiteral("50"),
                    Operator.Minus,
                    Identifier("c"),
                    Operator.Multiply,
                    PrimitiveType.void,
                    Operator.MinusMinus,
                    Punctuation.Semicolon
                )
            )
    }

    @Test
    fun `Identifier Test`() {
        """
            hello salhe
        """.trimIndent()
            .scan()
            .assertEquals(
                listOf(
                    Identifier("hello"),
                    Identifier("salhe")
                )
            )
    }

    @Test
    fun `Literal Test`() {
        """
            "Hello, SalHe!"
            10
            "Hello. There are some escapes! \\\" \\\\"
            10.5
            10.78f
        """.trimIndent()
            .scan()
            .assertEquals(
                listOf(
                    Literal.StringLiteral("Hello, SalHe!"),
                    Literal.IntegerLiteral("10"),
                    Literal.StringLiteral("Hello. There are some escapes! \\\" \\\\"),
                    Literal.DoubleLiteral("10.5"),
                    Literal.FloatLiteral("10.78")
                )
            )
    }

    @Test
    fun `Punctuation Test`() {
        """
            [ ] { } ; , ( )
        """.trimIndent()
            .scan()
            .assertEquals(
                listOf(
                    Punctuation.LRectBracket,
                    Punctuation.RRectBracket,
                    Punctuation.LCurlyBracket,
                    Punctuation.RCurlyBracket,
                    Punctuation.Semicolon,
                    Punctuation.Comma,
                    Punctuation.LBracket,
                    Punctuation.RBracket,
                )
            )
    }

    @Test
    fun `Primitive Type Test`() {
        """
            void
            int
        """.trimIndent()
            .scan()
            .assertEquals(
                listOf(
                    PrimitiveType.void,
                    PrimitiveType.int
                )
            )
    }

    @Test
    fun `Comment test`() {
        val lineComment = " hello comment"
        val multilineComment = """
            This is a multiline comment. you can enter any character in it.
            &#$&*2-/~+
        """.trimIndent()
        """
            there is a line comment and multiline comment //$lineComment
            /*$multilineComment*/
        """.trimIndent()
            .scan()
            .assertEquals(
                listOf(
                    Identifier("there"),
                    Identifier("is"),
                    Identifier("a"),
                    Identifier("line"),
                    Identifier("comment"),
                    Identifier("and"),
                    Identifier("multiline"),
                    Identifier("comment"),

                    Comment.SingleLine(lineComment),
                    Comment.Multiline(lineComment),
                )
            )
    }

    @Test
    fun `Confused comment and operator test`() {
        mapOf(
            """
            / *
                this should not recognized as multiline comment
            */
            """.trimIndent() to Comment.Multiline::class.java,
            """
                this should not recognized as single line comment
            """.trimIndent() to Comment.SingleLine::class.java
        ).forEach { (src, clazz) ->
            src.scan()
                .first()
                .let {
                    // 尽量不使用kotlin反射
                    assertNotEquals<Class<*>>(it.javaClass, clazz)
                }
        }
    }

}