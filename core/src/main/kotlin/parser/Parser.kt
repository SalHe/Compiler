package com.github.salhe.compiler.parser

import com.github.salhe.compiler.filterComment
import com.github.salhe.compiler.parser.ast.*
import com.github.salhe.compiler.token.*


class Parser(private val tokenStream: TokenStream) {

    constructor(tokens: List<Token>) : this(TokenStream(tokens.filterComment()))

    private val globalSymbols: Scope<Qualifier, Symbol> = MapScope<Qualifier, Symbol>().apply {
        Type.Primitive::class.sealedSubclasses.forEach {
            // 使用kotlin反射有一定的性能的损耗
            val primitive = it.objectInstance!!
            set(primitive.qualifier, primitive)
        }
    }
    private val globalQualifierResolver = QualifierResolver<Qualifier>().apply {
        globalSymbols.allQualifiers().forEach { set(it.full, it) }
    }

    fun parse(): AST {
        val scope = LooseScope(globalSymbols, globalQualifierResolver) { Qualifier(it) }

        // 解析工作：
        //  1. 确定语法树
        //  2. 解决符号引用问题（边解析边填充符号表）
        //  3. 有错误及时报告
        // 约定：状态保存、恢复由被调解析函数完成
        return parseStatements(tokenStream, scope)
    }

    private fun parseStatements(stream: TokenStream, scope: Scope<String, Symbol>): Statements {
        stream.save()
        val statements = mutableListOf<Statement>()
        while (!stream.eof && stream.top() != Punctuation.RCurlyBracket) {
            if (stream.top() != Punctuation.Semicolon && stream.top() !is LineSeparator && stream.top() != Punctuation.RCurlyBracket) {
                statements.add(parseDeclaration(stream, scope))
            }

            if (stream.eof) break

            if (statements.last() !is Declaration.Function
                && stream.top() != Punctuation.Semicolon && stream.top() !is LineSeparator
            ) {
                val errorPos = stream.tell() - 1
                stream.restore()
                throw ExpectStatementSeparatorException(errorPos)
            }

            if (statements.last() !is Declaration.Function)
                stream.consume()
        }
        stream.drop()
        return Statements(statements)
    }

    private fun parseStatementsBlock(stream: TokenStream, scope: Scope<String, Symbol>): Block {
        stream.expect(Punctuation.LCurlyBracket)
        val statements = parseStatements(stream, scope)
        stream.expect(Punctuation.RCurlyBracket)
        return Block(statements)
    }

    private fun parseDeclaration(
        stream: TokenStream,
        scope: Scope<String, Symbol>
    ): Declaration {
        // 变量、函数声明
        kotlin.runCatching {
            return parseVariableDeclaration(stream, scope)
        }.onFailure {
            return parseFunctionDeclaration(stream, scope)
        }

        throw UnexpectedParsingException(stream.tell())
    }

    private fun parseVariableDeclaration(
        stream: TokenStream,
        scope: Scope<String, Symbol>
    ): Declaration.Variable {
        stream.save()

        val type: Type = scope.resolveType(stream.consume(), stream.tell() - 1)
        val identifier = stream.expectNewIdentifier(scope)

        if (stream.consume() != Operator.Assign) {
            throw ShouldBeInitializedException(stream.restore() - 1)
        }

        val expression = parseExpression(stream, scope)
        stream.drop()
        val variable = Declaration.Variable(type, identifier.id, expression)
        scope[identifier.id] = ASTSymbol(variable)
        return variable
    }

    private fun parseExpression(
        stream: TokenStream,
        parentScope: Scope<String, Symbol>
    ): Expression {
        stream.save()
        // 先支持简单的字面量
        val literal = stream.consume()
        if (literal !is Literal) {
            stream.restore()
            throw IllegalStateException("目前只支持字面量表达式")
        }
        stream.drop()
        return Expression.Literal(literal)
    }

    private fun parseFunctionDeclaration(
        stream: TokenStream,
        parentScope: Scope<String, Symbol>
    ): Declaration.Function {
        stream.save()

        val scope = parentScope.new()

        val returnType = scope.resolveType(stream.consume(), stream.tell() - 1)
        val identifier = stream.expectNewIdentifier(scope)
        stream.expect(Punctuation.LBracket)
        // TODO 解析参数列表
        stream.expect(Punctuation.RBracket)

        val block = parseStatementsBlock(stream, scope)
        stream.drop()

        // TODO 考虑如何解决全限定名（后期希望考虑处理类方法、命名空间下的方法）
        return Declaration.Function(returnType, Qualifier(identifier.id), listOf(), block)
    }

    private inline fun TokenStream.expect(
        token: Token,
        thrower: () -> ParserException = { ExpectTokenException(this.tell(), token) }
    ): Token {
        if (this.top() != token) {
            throw thrower()
        }
        return consume()
    }

    private fun AbstractStream<Token>.expectNewIdentifier(scope: Scope<String, Symbol>): Identifier {
        val identifier = consume()
        if (identifier !is Identifier) {
            throw ExpectIdentifierException(tell() - 1)
        }
        if (scope[identifier.id] != null) {
            throw RedundantIdentifierException(tell() - 1, identifier.id)
        }
        return identifier
    }

    private fun Scope<String, Symbol>.resolveType(token: Token, tokenId: Int): Type {
        var type: Type?
        if (token is PrimitiveType) {
            type = this[token.spell] as? Type
            if (type == null) {
                throw UndefinedPrimitiveException(tokenId, token)
            }
        } else if (token is Identifier) { // 自定义类型（比如类，结构等）
            type = this[token.id] as? Type
            if (type == null) {
                type = Type.ToBeResolved(Qualifier(token.id))
                this[token.id] = type
            }
        } else {
            throw ExpectDefiningTypeException(tokenId)
        }
        return type
    }

    private fun Scope<String, Symbol>.new(childScope: Scope<String, Symbol> = MapScope()): Scope<String, Symbol> =
        InheritScope(this, childScope)

}
