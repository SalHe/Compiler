package com.github.salhe.compiler.parser

import com.github.salhe.compiler.parser.ast.AST

sealed interface Symbol

sealed class Type(val qualifier: Qualifier) : Symbol {
    override fun toString(): String = qualifier.full

    @Suppress("ClassName", "unused")
    sealed class Primitive(name: String) : Type(Qualifier(name)) {
        object void : Primitive("void")
        object boolean : Primitive("boolean")
        object char : Primitive("char")
        object byte : Primitive("byte")
        object short : Primitive("short")
        object int : Primitive("int")
        object long : Primitive("long")
        object float : Primitive("float")
        object double : Primitive("double")
    }

    class ToBeResolved(qualifier: Qualifier) : Type(qualifier)
}

class ASTSymbol(val ast: AST) : Symbol