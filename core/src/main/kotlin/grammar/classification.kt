package com.github.salhe.compiler.grammar

import com.github.salhe.compiler.filterComment
import com.github.salhe.compiler.grammar.GrammarClassification.*
import com.github.salhe.compiler.token.*
import com.github.salhe.compiler.token.scanner.Scanner
import java.io.ByteArrayInputStream

enum class GrammarClassification {
    /**
     * 无限制文法
     *
     */
    PSG,

    /**
     * 上下文相关文法
     *
     */
    CSG,

    /**
     * 上下文无关文法
     *
     */
    CFG,

    /**
     * 正规文法
     *
     */
    RG
}

sealed class Letter(val exp: String) {

    override fun toString(): String {
        return "<${this::class.java.simpleName}, $exp>"
    }

    override fun equals(other: Any?): Boolean {
        return other is Letter  // 得是字母
                && other.javaClass == this.javaClass // 是同类（都是终结符火非终结符）
                && other.exp == this.exp // 是同样的表达式
    }

    override fun hashCode(): Int {
        return "${this.javaClass.simpleName}:${exp}".hashCode()
    }

    class Terminal(exp: String) : Letter(exp)
    class NonTerminal(exp: String) : Letter(exp)
}

data class Producer(
    val left: List<Letter>,
    val right: List<Letter>
)

data class Grammar(
    val nonTerminals: List<Letter.NonTerminal>,
    val terminals: List<Letter.Terminal>,
    val producers: List<Producer>,
    val start: Letter
)

/**
 * 分析语法
 *
 * TODO 本来想做成先分析成语法树的，但是暂时先不考虑了。
 *
 * @param grammar
 * @return
 */
fun analyseGrammar(grammar: String): Grammar {
    val tokens = Scanner(ByteArrayInputStream(grammar.toByteArray()).reader()).scan()
    return analyseGrammar(tokens)
}

fun analyseGrammar(tokens: List<Token>): Grammar {
    val iterator = tokens.filterComment().iterator()

    if (!iterator.hasNext() || iterator.next() != Punctuation.LBracket) throw MissingBracketException("左括号", "(")
    val nonTerminals = listLetterSet(iterator) { Letter.NonTerminal(it) }
    if (!iterator.hasNext() || iterator.next() != Punctuation.Comma) throw MissingCommaException("分隔两集合")
    val terminals = listLetterSet(iterator) { Letter.Terminal(it) }

    val letters = nonTerminals + terminals
    fun findLetter(exp: String) = letters.find { it.exp == exp } ?: throw UndefinedLetterException(exp)

    if (!iterator.hasNext() || iterator.next() != Punctuation.Comma) throw MissingCommaException("分隔两集合")
    val producers = listProducers(iterator, ::findLetter)

    if (!iterator.hasNext() || iterator.next() != Punctuation.Comma) throw MissingCommaException("分隔产生式集合与起始产生式")
    val start = findLetter((iterator.next() as Identifier).id)

    return Grammar(nonTerminals, terminals, producers, start)
}

fun classifyGrammar(grammar: Grammar): GrammarClassification {

    for (p in grammar.producers) {
        // 如果任何一个产生式左部长度为0或只包含终结符（不含非终结符），则产生式错误
        if (p.left.isEmpty() || p.left.all { it is Letter.Terminal }) {
            throw NoNonTerminalException(p)
        }
    }

    // 产生式左部长度为1，则左部只包含一个非终结符（前面已经保证至少含有一个非终结符）
    // 可能是上下文相关文法、正规文法
    val isCsgOrRg = grammar.producers.all { it.left.size == 1 }
    if (isCsgOrRg) {
        return if (grammar.producers.all { it.right.size == 1 || (it.right.size == 2 && it.right[0] is Letter.Terminal) }) {
            RG // 左线性正规文法
        } else if (grammar.producers.all { it.right.size == 1 || (it.right.size == 2 && it.right[1] is Letter.Terminal) }) {
            RG // 右线性正则文法
        } else {
            CFG // 上下文相关文法
        }
    } else {
        // 右部长度至少为1
        if (grammar.producers.all { it.right.isNotEmpty() })
            return CSG

        return PSG
    }
}

private fun Token.toLetterExp(): String {
    if (this is Identifier) return this.id
    if (this is Literal.StringLiteral) return "\"${this.string}\""
    if (this is Literal.NumberLiteral<*>) return "" + this.value
    throw IllegalStateException("此处不应该出现其他类型的Token: $this")
}

/**
 * 解析产生式集合。
 *
 * @param iterator [Token]迭代器。
 * @param findLetter [Letter]提供者，用于根据符号名提供对应的符号。
 * @return
 */
private fun listProducers(iterator: Iterator<Token>, findLetter: (String) -> Letter) = buildList {

    // 缺少左花括号
    if (!iterator.hasNext() || iterator.next() != Punctuation.LCurlyBracket) throw GrammarException("字母集是一个花括号包裹、逗号分隔的集合")
    if (!iterator.hasNext()) throw GrammarException("字母集合描述不完整")

    var next = iterator.next()
    while (next != Punctuation.RCurlyBracket) {

        val left = buildList {
            while (next != Operator.Greater) {
                add(findLetter(next.toLetterExp()))
                next = iterator.next()
            }
        }
        next = iterator.next()
        val right = buildList {
            while (next != Punctuation.Comma && next != Punctuation.RCurlyBracket) {
                add(findLetter(next.toLetterExp()))
                next = iterator.next()
            }
        }
        add(Producer(left, right))

        if (!iterator.hasNext()) throw GrammarException("不完整的集合")
        if (next == Punctuation.RCurlyBracket) break
        if (next != Punctuation.Comma) throw IllegalStateException("请用逗号','分隔字母")
        next = iterator.next()
    }
}

/**
 * 解析符号集合
 *
 * @param R 符号类型，参见[Letter]。
 * @param iterator [Token]的迭代器。
 * @param builder 符号构建者，接受一个参数：符号名。
 * @return
 */
private fun <R> listLetterSet(iterator: Iterator<Token>, builder: (exp: String) -> R) = buildList {

    // 缺少左花括号
    if (!iterator.hasNext() || iterator.next() != Punctuation.LCurlyBracket) throw GrammarException("字母集是一个花括号包裹、逗号分隔的集合")
    if (!iterator.hasNext()) throw GrammarException("字母集合描述不完整")

    var next = iterator.next()
    while (next != Punctuation.RCurlyBracket) {

        add(builder(next.toLetterExp()))

        if (!iterator.hasNext()) throw GrammarException("不完整的集合")
        next = iterator.next()
        if (next == Punctuation.RCurlyBracket) break
        if (next != Punctuation.Comma) throw GrammarException("请用逗号','分隔字母")
        next = iterator.next()
    }
}