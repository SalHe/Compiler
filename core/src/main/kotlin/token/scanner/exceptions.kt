package com.github.salhe.compiler.token.scanner

sealed class ScannerException(val row: Int, val col: Int, message: String?) : Exception(message)

class ExpectQuoteException(row: Int, col: Int) : ScannerException(row, col, "字符串字面量应被\"包裹")
class InvalidEscapeCharException(row: Int, col: Int, val expression: String) :
    ScannerException(row, col, "错误的转义字符：$expression")

class UnexpectedCharException(row: Int, col: Int, val char: Char) : ScannerException(row, col, "出现了不应出现的字符：'${char}'")

sealed class UnsupportedNumberLiteralException(row: Int, col: Int, message: String?) :
    ScannerException(row, col, message)

class UnsupportedNumberPrefixException(row: Int, col: Int, val prefix: String) :
    UnsupportedNumberLiteralException(row, col, "不支持字面量中使用前缀: $prefix")

class UnsupportedNumberPostfixException(row: Int, col: Int, val postfix: String) :
    UnsupportedNumberLiteralException(row, col, "不支持字面量中使用后缀: $postfix")