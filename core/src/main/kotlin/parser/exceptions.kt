package com.github.salhe.compiler.parser

import com.github.salhe.compiler.token.PrimitiveType
import com.github.salhe.compiler.token.Token

open class ParserException(val tokenId: Int, message: String?) : Exception("$message (at $tokenId)")

class UndefinedPrimitiveException(tokenId: Int, primitiveType: PrimitiveType) :
    ParserException(tokenId, "未定义的基本类型：${primitiveType.spell}")

class ExpectTokenException(tokenId: Int, token: Token) : ParserException(tokenId, "期待一个 $token")
class ExpectDefiningTypeException(tokenId: Int) : ParserException(tokenId, "期待定义类型说明")
class ExpectIdentifierException(tokenId: Int) : ParserException(tokenId, "期待为目标对象指定一个标识符")
class ExpectStatementSeparatorException(tokenId: Int) : ParserException(tokenId, "请使用分号';'或另起一行分隔代码")
class ShouldBeInitializedException(tokenId: Int) : ParserException(tokenId, "请初始化变量")

class UnexpectedParsingException(tokenId: Int) : ParserException(tokenId, "预料之外的分析分支")

class RedundantIdentifierException(tokenId: Int, name: String) : ParserException(tokenId, "重定义的标识符：$name")