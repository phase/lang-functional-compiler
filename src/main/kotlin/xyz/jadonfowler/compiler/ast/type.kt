package xyz.jadonfowler.compiler.ast

sealed class Type(val name: String) {
    override fun toString(): String = name
}

class PrimitiveType(name: String) : Type(name)

class ProductType(name: String, val types: Map<String, Type>) : Type(name) {
    override fun toString(): String = "type $name = ${types.map { "${it.key} : ${it.value.name}" }.joinToString(", ")}"
}

class SumType(name: String, val types: Map<String, List<Type>>) : Type(name) {
    override fun toString(): String = "type $name = ${types.map { "${it.key} ${it.value.joinToString(" ")}" }.joinToString(" | ")}"
}

fun List<Type>.from(name: String): Type {
    // TODO: Retrieve Variants in Sum Types
    return filter { it.name == name }.first()
}
