package com.github.salhe.compiler.token.scanner

import com.github.salhe.compiler.token.*
import java.io.InputStreamReader

class Scanner(
    private val charStream: CharStream
) {

    constructor(stream: InputStreamReader) : this(CharStream(stream))

    private val tokens = mutableListOf<Token>()
    private val identifiers = mutableListOf<Identifier>()

    private fun fetchIdentifier(id: String): Identifier {
        var identifier = identifiers.find { it.id == id }
        if (identifier == null) {
            identifier = Identifier(id)
            identifiers.add(identifier)
        }
        return identifier
    }

    fun scan(lineSeparator: Boolean = false): List<Token> {
        while (!charStream.eof) {
            var token: Token? = null
            val cur = charStream.top()
            when {
                // 数字字面量
                cur.isDigit() -> token = scanNumber(charStream).getOrThrow()

                // 字符串字面量
                cur.isQuote() -> token = scanStringLiteral(charStream).getOrThrow()

                // 关键字/标识符/基本类型（其实我这里想把基本类型归到标识符里，作为一种类型来处理）
                cur.isValidWordStart() -> token = scanWord(charStream, ::fetchIdentifier).getOrThrow()

                // 注释/也可能是除号
                // * 不可与[scanOperator]交换顺序（除非对[scanOperator]也做特殊处理）
                cur == '/' ->
                    // 依照现在的[CharStream]仅支持读顶、消耗的操作来说，不太适合单独写一个子程序解析注释：
                    // 因为第一个'/'需要被消耗才能读下一个字符，假设读到下一个字符，发现不是注释，而是除号，
                    // 那就需要退出子程序，但是这样就没法简单的依靠[CharStream]来判断顶部是'/'了。
                    // 可以考虑的解决方法有：
                    // 1. 直接在扫描注释的子程序里考虑出现除号的情况，但是这样这个子程序就不存粹了。
                    // 2. 存储下这个'/'，让[scanOperator]做特殊处理，但是这搞特殊了。
                    // 3. 让[CharStream]支持回退，或新建一个支持回退的CharStream。
                    // 我个人经过折衷还是选择了第二种方法，第三种方法其实还是不错的，而且后期很可能用上。
                    token = scanComment(charStream).getOrElse { exception ->
                        if (exception !is NotCommentException) {
                            throw exception
                        }
                        scanOperator(charStream, '/').getOrThrow()
                    }

                // 操作符
                // * 不可与注释处理交换顺序
                cur.isOperator() -> token = scanOperator(charStream).getOrThrow()

                // 扫描换行符，为不需要以分号作为语句分隔符做准备
                lineSeparator && cur == '\r' -> {
                    charStream.consume()
                    token = if (charStream.top() == '\n') {
                        charStream.consume()
                        LineSeparator.CRLF
                    } else {
                        LineSeparator.CR
                    }
                }
                lineSeparator && cur == '\n' -> {
                    charStream.consume()
                    token = LineSeparator.LF
                }

                cur.isWhitespace() -> charStream.consume()
                else -> {
                    // 边界符较为简单，放到此处
                    val punctuation = Punctuation.lookup(charStream.top().toString())
                    if (punctuation != null) {
                        charStream.consume()
                        token = punctuation
                    } else {
                        throw UnexpectedCharException(
                            charStream.row,
                            charStream.col,
                            charStream.top()
                        )
                    }
                }
            }
            if (token != null) {
                tokens.add(token)
            }
        }
        return tokens.toList()
    }

}

fun Char.isDigit(): Boolean = this in '0'..'9'
fun Char.isValidWordStart(): Boolean = this == '_' || this in 'A'..'Z' || this in 'a'..'z'
fun Char.isValidWordChar(): Boolean = this.isValidWordStart() || this.isDigit()
fun Char.isOperator(): Boolean = this.isComputingSign() || this.isComparator() || this == '='
fun Char.isComputingSign(): Boolean = this == '+' || this == '-' || this == '*' || this == '/' || this == '!'
fun Char.isComparator(): Boolean = this == '<' || this == '>'
fun Char.isQuote(): Boolean = this == '"'

private val escapeCharTable = mapOf(
    "\\b" to '\u0007',
    "\\b" to '\b',
    "\\f" to '\u000c',
    "\\n" to '\n',
    "\\r" to '\r',
    "\\t" to '\t',
    "\\v" to '\u000B',
    "\\\\" to '\\',
    "\\\"" to '\"',
    "\\0" to '\u0000',
)

private fun scanComment(charStream: CharStream): Result<Comment> {
    val startRow = charStream.row
    val startCol = charStream.col
    charStream.consume()

    if (charStream.top() == '/') {
        // 注释
        charStream.consume()
        val comment = buildString {
            while (!charStream.eof && charStream.top() != '\n') {
                append(charStream.consume())
            }
            // 不消耗换行符
        }
        return Result.success(Comment.SingleLine(comment))
    } else if (charStream.top() == '*') {
        // 多行注释
        charStream.consume()
        val commentContent = buildString {
            while (!charStream.eof) {
                if (charStream.top() == '*') {
                    val star = charStream.consume()
                    if (charStream.top() == '/') {
                        charStream.consume() // /
                        return@buildString
                    } else {
                        append(star)
                    }
                } else {
                    append(charStream.consume())
                }
            }
            throw NonTerminalMultilineComment(startRow, startCol)
        }
        return Result.success(Comment.Multiline(commentContent))
    }
    return Result.failure(NotCommentException(startRow, startCol))
}

private fun scanStringLiteral(charStream: CharStream): Result<Token> {
    val original = StringBuilder() // 暂时记录，但不放到token中
    val actual = StringBuilder()
    if (charStream.top() != '"') {
        return Result.failure(ExpectQuoteException(charStream.row, charStream.col))
    }
    charStream.consume() // 消耗起始的引号

    fun consumeChar(): Char {
        val consume = charStream.consume()
        original.append(consume)
        return consume
    }
    while (!charStream.eof && charStream.top() != '"') {
        if (charStream.top() == '\r' || charStream.top() == '\n')
            return Result.failure(ExpectQuoteException(charStream.row, charStream.col))
        if (charStream.top() == '\\') {
            val escapeExpression = consumeChar().toString() + charStream.top()
            val actualChar = escapeCharTable[escapeExpression]
                ?: return Result.failure(
                    InvalidEscapeCharException(
                        charStream.row,
                        charStream.col - 1,
                        escapeExpression
                    )
                )

            consumeChar()
            actual.append(actualChar)
        } else {
            actual.append(consumeChar())
        }
    }
    if (charStream.top().isQuote())
        charStream.consume() // 消耗结束的引号
    else return Result.failure(ExpectQuoteException(charStream.row, charStream.col))
    return Result.success(Literal.StringLiteral(actual.toString()))
}

private fun scanOperator(charStream: CharStream, consumed: Char? = null): Result<Operator> {
    val start = consumed ?: charStream.consume()
    val token = Operator.lookup("${start}${charStream.top()}")
    if (token != null) {
        charStream.consume()
        return Result.success(token)
    }
    return Result.success(Operator.lookup(start.toString())!!)
}

private fun scanWord(
    charStream: CharStream,
    fetchIdentifier: (String) -> Identifier
): Result<Token> {
    val word = buildString {
        while (!charStream.eof && charStream.top().isValidWordChar()) {
            append(charStream.consume())
        }
    }

    var token: Token? = null
    @Suppress("KotlinConstantConditions", "ControlFlowWithEmptyBody", "SENSELESS_COMPARISON")
    if (token == null) token = Keyword.lookup(word)
    if (token == null) token = Literal.BooleanLiteral.lookup(word)
    if (token == null) token = PrimitiveType.lookup(word)
    if (token == null) token = fetchIdentifier(word)
    return Result.success(token)
}

private fun scanNumber(charStream: CharStream): Result<Literal.NumberLiteral<*>> {
    val integral = scanIntegerString(charStream).getOrThrow()
    if (charStream.top() == '.') {
        charStream.consume()
        // 是一个小数
        val fractional = scanIntegerString(charStream, true).getOrThrow()
        return if (charStream.top().lowercase() == "f") {
            charStream.consume()
            Result.success(Literal.FloatLiteral("${integral}.${fractional}"))
        } else if (charStream.top().toString().isBlank()) {
            charStream.consume() // 因为是空白符也会被忽略掉，可以不消耗
            Result.success(Literal.DoubleLiteral("${integral}.${fractional}"))
        } else {
            Result.failure(
                UnsupportedNumberPostfixException(
                    charStream.row,
                    charStream.col,
                    charStream.top().toString()
                )
            )
        }
    }
    return Result.success(Literal.IntegerLiteral(integral))
}

private fun scanIntegerString(charStream: CharStream, zeroStart: Boolean = false): Result<String> = buildString {
    if (!zeroStart && charStream.top() == '0') {
        append(charStream.consume())
        if (charStream.top() == '0') {
            return Result.failure(UnsupportedNumberPrefixException(charStream.row, charStream.col - 1, "0"))
        }
    } else while (!charStream.eof && charStream.top() in '0'..'9') {
        append(charStream.consume())
    }
}.let { Result.success(it) }