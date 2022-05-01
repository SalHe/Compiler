package com.github.salhe.compiler.test.token.scanner

import com.github.salhe.compiler.scan
import com.github.salhe.compiler.token.scanner.*
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

/**
 * 与词法分析错误报告相关的测试。
 *
 */
class ScannerReportTest {

    /**
     * 测试能否正确给出错误类型、位置、以及详细信息。
     *
     */
    @Test
    fun `Accurate error test`() {
        mapOf(
            "\"123" to ExpectQuoteException(1, 4),
            "\"\\s\"" to InvalidEscapeCharException(1, 2, "\\s"),
            "$" to UnexpectedCharException(1, 1, '$'),
            "1.5s" to UnsupportedNumberPostfixException(1, 4, "s"),
            "00" to UnsupportedNumberPrefixException(1, 1, "0")
        ).forEach { (src, exception) ->
            val actualException = Assertions.assertThrows(exception.javaClass) { src.scan() }
            assertEquals(exception.row, actualException.row)
            assertEquals(exception.col, actualException.col)
            assertScannerExceptionEquals(exception, actualException)
        }
    }

    private fun assertScannerExceptionEquals(exception: ScannerException, actualException: ScannerException) {
        when (exception) {
            is ExpectQuoteException -> {}
            is InvalidEscapeCharException -> assertEquals(
                exception.expression,
                (actualException as InvalidEscapeCharException).expression
            )
            is UnexpectedCharException -> assertEquals(
                exception.char,
                (actualException as UnexpectedCharException).char
            )
            is UnsupportedNumberLiteralException -> when (exception) {
                is UnsupportedNumberPostfixException -> assertEquals(
                    exception.postfix,
                    (actualException as UnsupportedNumberPostfixException).postfix
                )
                is UnsupportedNumberPrefixException -> assertEquals(
                    exception.prefix,
                    (actualException as UnsupportedNumberPrefixException).prefix
                )
            }
            is NonTerminalMultilineComment -> {}
            is NotCommentException -> throw IllegalStateException("不应该能够收到[NotCommentException]。")
        }
    }

    @Test
    fun `Non terminal multiline comment test`() {
        assertThrows<NonTerminalMultilineComment> {
            """
            /*
                咱们就是说，这个多行注释缺个结尾
            """.trimIndent()
                .scan()
        }
    }

}
