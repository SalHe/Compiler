package com.github.salhe.compiler.parser

interface Scope<Q, T> {
    operator fun get(qualifier: Q): T?
    operator fun set(qualifier: Q, value: T)
    fun allQualifiers(): Set<Q>
}

class LooseScope<Q, T>(
    private val scope: Scope<Q, T>,
    private val qualifierResolver: QualifierResolver<Q>,
    private val qualifierBuilder: (name: String) -> Q
) : Scope<String, T> {
    override fun get(qualifier: String): T? = realQualifier(qualifier)?.let { scope[it] }
    override fun set(qualifier: String, value: T) {
        var it = qualifierResolver[qualifier]
        if (it == null) {
            it = qualifierBuilder(qualifier)
            qualifierResolver[qualifier] = it
        }
        scope[it!!] = value
    }

    override fun allQualifiers(): Set<String> = qualifierResolver.allQualifiers()

    fun realQualifier(qualifier: String): Q? = qualifierResolver[qualifier]

}

open class MapScope<Q, T> : Scope<Q, T> {

    private val contents: MutableMap<Q, T> = mutableMapOf()
    override fun get(qualifier: Q): T? = contents[qualifier]
    override fun set(qualifier: Q, value: T) {
        contents[qualifier] = value
    }

    override fun allQualifiers(): Set<Q> = contents.keys.toSet()

}

class InheritScope<Q, T>(
    private val parentScope: Scope<Q, T>,
    private val selfScope: Scope<Q, T> = MapScope()
) : Scope<Q, T> {

    override fun get(qualifier: Q): T? = selfScope[qualifier] ?: parentScope[qualifier]
    override fun set(qualifier: Q, value: T) {
        selfScope[qualifier] = value
    }

    override fun allQualifiers(): Set<Q> = selfScope.allQualifiers() + parentScope.allQualifiers()

}