package xyz.jadonfowler.compiler.ast

import java.util.*

interface Node

sealed class Expression : Node

class FunctionCallExpression(val functionName: String, val arguments: List<Expression>) : Expression() {
    override fun toString(): String = "$functionName(${arguments.joinToString(", ")})"
}

class TypeInitializationExpression(val typeName: String, val arguments: List<Expression>) : Expression() {
    override fun toString(): String = "$typeName {${arguments.joinToString(", ")}}"
}

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
sealed class BlockStatement : Statement()
class StatementList(vararg statements: Statement) : ArrayList<Statement>(statements.toMutableList()), Node

class Noop : Statement() {  // TODO: Remove
    override fun toString(): String = "#noop"
}

class VariableDeclarationStatement(val constant: Boolean, val name: String, val type: Type, val expression: Expression) : Statement() {
    override fun toString(): String = "${if (constant) "let" else "var"} $name : $type = $expression"
}

class ReturnStatement(val expression: Expression) : Statement() {
    override fun toString(): String = "return $expression"
}

class IfStatement(val expression: Expression, val statements: StatementList) : BlockStatement() {
    override fun toString(): String {
        return """if $expression
        ${statements.joinToString(",\n        ")}
    ;"""
    }
}


class Argument(val name: String, val type: Type) : Node {
    override fun toString(): String = "$name : $type"
}

open class Function(val name: String, val returnType: Type, val arguments: List<Argument>, val statements: StatementList) : Node {
    override fun toString(): String {
        return """$name (${arguments.joinToString(", ")}) : $returnType
    ${statements.subList(0, statements.size - 1).joinToString("\n    ") { it.toString() + if (it is BlockStatement) "" else "," }}
    ${if (statements.last() is ReturnStatement) (statements.last() as ReturnStatement).expression else statements.last()}.
"""
    }
}

class Operator(name: String, val precedence: Int, val leftAssociative: Boolean,
               returnType: Type, arguments: List<Argument>, statements: StatementList)
    : Function(name, returnType, arguments, statements) {
    override fun toString(): String = "($name) (${arguments.joinToString(", ")}) : $returnType"
}

class Module(val name: String, val types: List<Type>, val functions: List<Function>) : Node {
    override fun toString(): String {
        return """#module $name

${types.filter { it !is PrimitiveType }.joinToString("\n")}

${functions.joinToString("\n")}
"""
    }
}
