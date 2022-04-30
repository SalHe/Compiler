package com.github.salhe.compiler.token

sealed interface Token

abstract class AbstractToken : Token {

    protected abstract fun otherAttributes(): List<Any>
    protected open fun tokenTypeDescription(): String = this.javaClass.simpleName

    override fun toString(): String {
        val otherAttributes = otherAttributes()
        if (otherAttributes.isEmpty())
            return "<${tokenTypeDescription()}>"
        return "<${tokenTypeDescription()}, ${otherAttributes.joinToString()}>"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other || this.toString() == other.toString()) return true
        if (javaClass != other?.javaClass) return false
        return true
    }

    override fun hashCode(): Int = toString().hashCode()

}

abstract class Literal : AbstractToken() {
    open class StringLiteral(val string: String) : Literal() {
        override fun otherAttributes(): List<Any> = listOf("\"$string\"")
    }

    open class NumberLiteral<T>(val value: T, val rawString: String) : Literal() {
        override fun otherAttributes(): List<Any> = listOf(value as Any, "\"$rawString\"")
    }

    class IntegerLiteral(rawString: String) : NumberLiteral<Int>(rawString.toInt(), rawString)
    class FloatLiteral(rawString: String) : NumberLiteral<Float>(rawString.toFloat(), rawString)
    class DoubleLiteral(rawString: String) : NumberLiteral<Double>(rawString.toDouble(), rawString)
}

class Identifier(val id: String) : AbstractToken() {
    override fun otherAttributes(): List<Any> = listOf(id)

    override fun equals(other: Any?): Boolean {
        return other is Identifier && other.id == this.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}

sealed class Punctuation(val value: String) : AbstractToken() {

    override fun otherAttributes(): List<Any> = listOf(value)
    override fun tokenTypeDescription(): String = "Punctuation"

    companion object {
        private val all =
            listOf(LBracket, RBracket, LRectBracket, RRectBracket, LCurlyBracket, RCurlyBracket, Semicolon, Comma)

        fun lookup(punctuation: String) = all.firstOrNull { it.value == punctuation }
    }

    object LBracket : Punctuation("(")
    object RBracket : Punctuation(")")
    object LRectBracket : Punctuation("[")
    object RRectBracket : Punctuation("]")
    object LCurlyBracket : Punctuation("{")
    object RCurlyBracket : Punctuation("}")
    object Semicolon : Punctuation(";")
    object Comma : Punctuation(",")
}

sealed class Keyword(val value: String) : AbstractToken() {
    override fun otherAttributes(): List<Any> = listOf(value)

    override fun tokenTypeDescription(): String = "Keyword"

    object While : Keyword("while")
    object When : Keyword("when")
    object Do : Keyword("do")
    object If : Keyword("if")
    object Else : Keyword("else")

    companion object {
        private val all = setOf(While, When, Do, If, Else)

        fun lookup(value: String) =
            all.firstOrNull { it?.value == value }
    }
}

@Suppress("ClassName")
sealed class PrimitiveType(val spell: String) : AbstractToken() {

    override fun otherAttributes(): List<Any> = listOf(spell)
    override fun tokenTypeDescription(): String = "Primitive"

    companion object {
        // private val all = PrimitiveType::class.sealedSubclasses.map { it.objectInstance }
        private val all = setOf(void, int)

        fun lookup(spell: String) =
            all.firstOrNull { it?.spell == spell }
    }

    object void : PrimitiveType("void")
    object int : PrimitiveType("int")
}

sealed class Operator(val operator: String) : AbstractToken() {

    override fun otherAttributes(): List<Any> = listOf(operator)
    override fun tokenTypeDescription(): String = "Operator"

    object LogicNot : Operator("!")
    object Plus : Operator("+")
    object PlusPlus : Operator("++")
    object Minus : Operator("-")
    object MinusMinus : Operator("--")
    object Multiply : Operator("*")
    object Divide : Operator("/")
    object Assign : Operator("=")
    object Equals : Operator("==")
    object NotEquals : Operator("!=")
    object Greater : Operator(">")
    object Lesser : Operator("<")

    companion object {
        // private val all = Operator::class.sealedSubclasses.map { it.objectInstance }
        private val all = setOf(
            LogicNot,
            Plus,
            Minus,
            Multiply,
            Divide,
            Assign,
            Equals,
            NotEquals,
            PlusPlus,
            MinusMinus,
            Greater,
            Lesser
        )

        fun lookup(operator: String) = all.firstOrNull { it?.operator == operator }
    }
}