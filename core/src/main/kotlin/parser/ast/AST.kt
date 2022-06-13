package com.github.salhe.compiler.parser.ast

import com.github.salhe.compiler.parser.Qualifier
import com.github.salhe.compiler.parser.Type
import com.github.salhe.compiler.token.Operator

typealias TokenLiteral = com.github.salhe.compiler.token.Literal

interface AST {
    fun description(): String = "#${this.javaClass.simpleName}#"
}

class Program(
    val globals: List<Declaration.Variable>,
    val functions: List<Declaration.Function>
) : AST

sealed interface Expression : AST {
    class Literal(val literal: TokenLiteral) : Expression {
        override fun description(): String = literal.otherAttributes().first().toString()
    }

    /**
     * 运算。用于表示运算。
     *
     * @property operator 操作符。
     * @property operand1 操作数1。
     * @property operand2 操作数2，当仅有一个操作数时设为null。
     */
    class Op(
        val operator: Operator,
        val operand1: Expression,
        val operand2: Expression? = null,
    ) : Expression {
        override fun description(): String = if (operand2 == null) {
            "${operator.operator}${operand1.description()}"
        } else {
            "${operand1.description()}${operator.operator}${operand2.description()}"
        }

    }

    /**
     * 方法调用。
     *
     * @property callee 被调函数。
     * @property arguments 实参列表。
     */
    class Call(
        val callee: Qualifier,
        val arguments: List<Expression>
    ) : Expression
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

