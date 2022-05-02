package com.github.salhe.compiler.test.parser

import com.github.salhe.compiler.parse
import com.github.salhe.compiler.parser.Qualifier
import com.github.salhe.compiler.parser.Type
import com.github.salhe.compiler.parser.ast.*
import com.github.salhe.compiler.token.Literal
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class ParserTest {

    @Test
    fun `Variable declaration test`() {
        """
            void main(){
                int SalHe = 121;
                int nestedFun(){
                    int SalHe2 = "String"; // 还没有处理子域和父域标识符屏蔽问题
                }
                boolean hi = true;
            }
        """.trimIndent()
            .parse()
            .let {
                val nestedFun = Declaration.Function(
                    Type.Primitive.int,
                    Qualifier("nestedFun"),
                    listOf(),
                    Block(
                        Statements(
                            listOf(
                                Declaration.Variable(
                                    Type.Primitive.int,
                                    "SalHe2",
                                    Expression.Literal(Literal.StringLiteral("String"))
                                ),
                            )
                        )
                    )
                )
                val mainFun = Declaration.Function(
                    Type.Primitive.void,
                    Qualifier("main"),
                    listOf(),
                    Block(
                        Statements(
                            listOf(
                                Declaration.Variable(
                                    Type.Primitive.int,
                                    "SalHe",
                                    Expression.Literal(Literal.IntegerLiteral("121"))
                                ),
                                nestedFun,
                                Declaration.Variable(
                                    Type.Primitive.boolean,
                                    "hi",
                                    Expression.Literal(Literal.BooleanLiteral.True)
                                )
                            )
                        )
                    )
                )
                val expected = Statements(listOf(mainFun))
                assertEquals(expected.description(), it.description())
                println(it.description())
            }
    }

}