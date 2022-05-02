package com.github.salhe.compiler.parser

data class Qualifier(
    val full: String,
    val simple: String = full.simpleName()
) {
    override fun toString(): String = full
}

fun String.simpleName() = this.split(".").last()

infix fun Qualifier.sameWith(other: Qualifier) = this.full == other.full

class QualifierResolver<Q> : Scope<String, Q> {

    /**
     * [fullToQualifier] 包含的限定符集合肯定包含于[simpleToQualifier]
     *
     */
    private val simpleToQualifier = mutableMapOf<String, Q>()
    private val fullToQualifier = mutableMapOf<String, Q>()

    override fun get(qualifier: String): Q? {
        val simple = qualifier.simpleName()
        var targetQualifier = fullToQualifier[qualifier]
        if (simple != qualifier) { // qualifier是全限定名
            if (targetQualifier == null) {
                targetQualifier = simpleToQualifier[simple]?.also { fullToQualifier[qualifier] = it }
            }
        }
        return targetQualifier
    }

    override fun set(qualifier: String, value: Q) {
        simpleToQualifier[qualifier.simpleName()] = value
        fullToQualifier[qualifier] = value
    }

    override fun allQualifiers(): Set<String> = simpleToQualifier.keys + fullToQualifier.keys

}
