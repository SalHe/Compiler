package com.github.salhe.compiler.token

import java.io.InputStreamReader


enum class ScanType {
    NoScanning, Integer, String, Word, Operator, Punctuation
}

class Scanner(
    val stream: InputStreamReader
) {

    private val tokens = mutableListOf<Token>()
    private val identifiers = mutableListOf<Identifier>()
    private val sb = StringBuilder()
    private var scanType = ScanType.NoScanning
    private var row = 0
    private var col = 0

    private fun fetchIdentifier(id: String): Identifier {
        var identifier = identifiers.find { it.id == id }
        if (identifier == null) {
            identifier = Identifier(id)
            identifiers.add(identifier)
        }
        return identifier
    }

    private fun saveToken() {
        val string = sb.toString()
        when (scanType) {
            ScanType.NoScanning -> {}
            ScanType.Integer -> tokens.add(Literal.IntegerLiteral(string.toInt(), string))
            ScanType.String -> tokens.add(Literal.StringLiteral(string))
            ScanType.Word -> {
                var token: Token? = PrimitiveType.lookup(string)
                if (token == null) token = fetchIdentifier(string)
                tokens.add(token)
            }
            ScanType.Operator -> Operator.lookup(string)?.let { tokens.add(it) }
            ScanType.Punctuation -> Punctuation.lookup(string)?.let { tokens.add(it) }
        }
        scanType = ScanType.NoScanning
    }

    fun scan(): List<Token> {
        var preCh: Char? = null
        var ch = stream.read().toChar()

        row = if (ch == (-1).toChar()) 0 else 1

        while (true) {
            if (scanType == ScanType.String && ch != '"') {
                sb.append(ch)
            } else {
                when (ch) {
                    '"' -> {
                        if (scanType != ScanType.String) {
                            scanType = ScanType.String
                            sb.clear()
                        } else {
                            saveToken()
                        }
                    }
                    in 'A'..'Z', in 'a'..'z' -> {
                        if (scanType == ScanType.NoScanning) {
                            scanType = ScanType.Word
                            sb.clear()
                        }
                        sb.append(ch)
                    }
                    in '0'..'9' -> {
                        // 如果不是作为单词的一部分，当前也不是在扫描整数，则认为现在应该开始扫描整数
                        if (scanType != ScanType.Word && scanType != ScanType.Integer) {
                            scanType = ScanType.Integer
                            sb.clear()
                        }
                        sb.append(ch)
                    }
                    '+', '-', '*', '/', '=' -> {
                        if (scanType != ScanType.Operator) {
                            saveToken()
                            scanType = ScanType.Operator
                            sb.clear()
                        }
                        sb.append(ch)
                    }
                    '(', ')', '[', ']', '{', '}', ',', ';' -> {
                        saveToken()

                        scanType = ScanType.Punctuation
                        sb.clear()
                        sb.append(ch)
                        saveToken()
                    }
                    ' ', '\r', '\n', (-1).toChar() -> {
                        if (scanType != ScanType.NoScanning) {
                            saveToken()
                            scanType = ScanType.NoScanning
                            if ((ch == '\n' && preCh != '\r') || ch == '\r') {
                                row++
                                col = 0
                            }
                        }
                    }
                }
            }

            // 文件结束
            if (ch == (-1).toChar()) break

            // 保存上一个字符
            preCh = ch
            ch = stream.read().toChar()

            // 统计所在列
            col++
        }

        return tokens.toList()
    }

}