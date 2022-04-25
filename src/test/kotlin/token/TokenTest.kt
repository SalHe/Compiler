package com.github.salhe.compiler.test.token

import com.github.salhe.compiler.test.getResourceAsStream
import com.github.salhe.compiler.token.Scanner
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class TokenTest {

    @Test
    fun equalsTest() {
        // 正常来说两次扫描token序列一样，但是内存中不是同一对象
        val tokens = Scanner(getResourceAsStream("token/hello.c").reader()).scan()
        val tokens2 = Scanner(getResourceAsStream("token/hello.c").reader()).scan()
        Assertions.assertIterableEquals(tokens, tokens2)
    }

}