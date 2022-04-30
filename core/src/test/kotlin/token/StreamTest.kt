package com.github.salhe.compiler.test.token

import com.github.salhe.compiler.inputStreamReader
import com.github.salhe.compiler.token.CharStream
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class StreamTest {

    /**
     * 测试[CharStream]读顶字符、消耗字符功能。
     *
     */
    @Test
    fun `Read and consume test`() {
        val testString = "asdagjSFDavgsdajysgdBHSgquye"
        val cs = CharStream(testString.inputStreamReader())
        testString.forEach {
            assertEquals(it, cs.top())
            assertEquals(it, cs.consume())
        }
    }

    /**
     * 测试[CharStream]能否正常统计行、列，以便为报错时指定详细位置做准备。
     *
     */
    @Test
    fun `Row and col counter test`() {
        val cs = CharStream("123\n123\n123".inputStreamReader())

        assertEquals(1, cs.row)
        assertEquals(1, cs.col)
        cs.consume() // 1

        assertEquals(1, cs.row)
        assertEquals(2, cs.col)
        cs.consume() // 2

        assertEquals(1, cs.row)
        assertEquals(3, cs.col)
        cs.consume() // 3

        assertEquals(2, cs.row)
        assertEquals(0, cs.col)
        cs.consume() // \n

        assertEquals(2, cs.row)
        assertEquals(1, cs.col)
        cs.consume() // 1

        assertEquals(2, cs.row)
        assertEquals(2, cs.col)
        cs.consume() // 2

        assertEquals(2, cs.row)
        assertEquals(3, cs.col)
        cs.consume() // 3

        assertEquals(3, cs.row)
        assertEquals(0, cs.col)
        cs.consume() // \n

        assertEquals(3, cs.row)
        assertEquals(1, cs.col)
        cs.consume() // 1

        assertEquals(3, cs.row)
        assertEquals(2, cs.col)
        cs.consume() // 2

        assertEquals(3, cs.row)
        assertEquals(3, cs.col)
        cs.consume() // 3
    }

}