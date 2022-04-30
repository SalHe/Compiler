package com.github.salhe.compiler.grammar

open class GrammarException(message: String?) : Exception(message)
class MissingBracketException(name: String, exp: String) : GrammarException("缺少$name: '$exp'")
class MissingCommaException(details: String? = null) : GrammarException("缺少逗号','$details")
class UndefinedLetterException(exp: String) : GrammarException("找不到对应符号：$exp")

open class ClassifyGrammarException(message: String?) : GrammarException(message)
class NoNonTerminalException(producer: Producer) : ClassifyGrammarException("产生式左部应至少包含一个非终结符：$producer")