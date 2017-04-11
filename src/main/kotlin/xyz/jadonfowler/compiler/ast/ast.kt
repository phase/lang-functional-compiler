package xyz.jadonfowler.compiler.ast

import java.util.*

interface Node

sealed class Expression : Node

class IntegerLiteral(val value: Int) : Expression() {
    override fun toString(): String = value.toString()
}

class BooleanLiteral(val value: Boolean) : Expression() {
    override fun toString(): String = value.toString()
}

class Reference(val name: String) : Expression() {
    override fun toString(): String = name
}

sealed class Statement : Node
class StatementList(vararg statements: Statement) : ArrayList<Statement>(statements.toMutableList()), Node

class Noop : Statement() // TODO: Remove

class VariableDeclarationStatement(val constant: Boolean, val name: String, val type: Type, val expression: Expression) {
    override fun toString(): String = "${if (constant) "let" else "var"} $name : $type = $expression"
}

class ReturnStatement(val expression: Expression) {
    override fun toString(): String = "return $expression"
}

class Argument(val name: String, val type: Type) : Node {
    override fun toString(): String = "$name : $type"
}

open class Function(val name: String, val returnType: Type, val arguments: List<Argument>, val statements: StatementList) : Node {
    override fun toString(): String = "$name (${arguments.joinToString(", ")}) : $returnType"
}

class Operator(name: String, val precedence: Int, val leftAssociative: Boolean,
               returnType: Type, arguments: List<Argument>, statements: StatementList)
    : Function(name, returnType, arguments, statements) {
    override fun toString(): String = "($name) (${arguments.joinToString(", ")}) : $returnType"
}

class Module(val name: String, val types: List<Type>, val functions: List<Function>) : Node {
    override fun toString(): String = "module $name"
}
