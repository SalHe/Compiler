package com.github.salhe.compiler.parser.ast

import com.github.salhe.compiler.parser.Qualifier
import com.github.salhe.compiler.parser.Type

typealias TokenLiteral = com.github.salhe.compiler.token.Literal

interface AST {
    fun description(): String = "#${this.javaClass.simpleName}#"
}

sealed interface Expression : AST {
    class Literal(val literal: TokenLiteral) : Expression {
        override fun description(): String = literal.otherAttributes().first().toString()
    }
}

sealed interface Statement : AST
class Statements(val statements: List<Statement>) : Statement {
    override fun description(): String =
        statements.joinToString("\n") { it.description() }
}

class Block(val statements: Statements) : Statement {
    override fun description(): String =
        "{\n" + statements.description().lines().joinToString("\n") { "    $it" } + "\n}"
}

sealed interface Declaration : Statement {

    class Variable(
        val type: Type,
        val name: String,
        val initializer: Expression
    ) : Declaration {
        override fun description(): String = "$type $name = ${initializer.description()};"
    }

    /**
     * 参数
     *
     * TODO 考虑和[Variable]整合
     *
     * @property type
     * @property qualifier
     */
    class Arugment(
        val type: Type,
        val name: String,
    ) : Declaration {
        override fun description(): String = "$type $name"
    }

    class Function(
        val returnType: Type,
        val qualifier: Qualifier,
        val arguments: List<Arugment>,
        val block: Block
    ) : Declaration {
        override fun description(): String =
            "$returnType $qualifier(${arguments.joinToString()})${block.description()}"
    }

}

