package xyz.jadonfowler.compiler.pass.stage0

import xyz.jadonfowler.compiler.ast.*
import xyz.jadonfowler.compiler.ast.Function
import xyz.jadonfowler.compiler.parser.LangBaseVisitor
import xyz.jadonfowler.compiler.parser.LangParser

class ASTBuilder(val moduleName: String, ctx: LangParser.ModuleContext?, val knownTypes: List<Type>) : LangBaseVisitor<Node?>() {

    val module = visitModule(ctx)

    override fun visitModule(ctx: LangParser.ModuleContext?): Module {
        val functions = ctx?.externalDeclaration()
                ?.filter { it.functionDeclaration() != null }
                ?.map { visitFunctionDeclaration(it.functionDeclaration()) }
                ?.filterNotNull()
                .orEmpty()
        return Module(moduleName, knownTypes, functions)
    }

    override fun visitFunctionDeclaration(ctx: LangParser.FunctionDeclarationContext?): Function? {
        val arguments = ctx?.argumentList()?.variableSignature()?.map {
            Argument(it.ID().symbol.text,
                    knownTypes.from(it.typeAnnotation().ID().symbol.text.orEmpty()))
        }.orEmpty()
        val statements = visitStatementList(ctx?.statementList())
        statements.add(ReturnStatement(visitExpression(ctx?.expression())))

        val returnType = knownTypes.from(ctx?.typeAnnotation()?.ID()?.symbol?.text.orEmpty())

        if (ctx?.functionName()?.ID() != null) {
            // Normal Function
            val functionName = ctx.functionName().ID().symbol.text
            return Function(functionName, returnType, arguments, statements)
        } else if (ctx?.functionName()?.OPERATOR() != null) {
            // Defined Operator
            val operator = ctx.functionName().OPERATOR().symbol.text
            // TODO: Parse Precedence and Associativity
            return Operator(operator, 10, true, returnType, arguments, statements)
        }

        return null // Unreachable
    }

    override fun visitStatementList(ctx: LangParser.StatementListContext?): StatementList {
        val statements = mutableListOf<Statement>()
        if (ctx?.statement() != null)
            statements.add(visitStatement(ctx.statement()))
        else if (ctx?.blockStatement() != null)
            statements.add(visitBlockStatement(ctx.blockStatement()))
        if (ctx?.statementList() != null)
            statements.addAll(visitStatementList(ctx.statementList()))
        return StatementList(*statements.toTypedArray())
    }

    override fun visitBlockStatement(ctx: LangParser.BlockStatementContext?): Statement {
        val firstToken = ctx?.getChild(0)?.text.orEmpty()
        when (firstToken) {
            "if" -> {
                val expression = visitExpression(ctx?.expression())
                val statements = visitStatementList(ctx?.statementList())
                return IfStatement(expression, statements)
            }
        }
        return Noop()
    }

    override fun visitStatement(ctx: LangParser.StatementContext?): Statement {
        val firstToken = ctx?.getChild(0)?.text.orEmpty()
        when (firstToken) {
            "let" -> {
                val name = ctx?.variableSignature()?.ID()?.symbol?.text.orEmpty()
                val type = knownTypes.from(ctx?.variableSignature()?.typeAnnotation()?.ID()?.symbol?.text.orEmpty())
                val expression = visitExpression(ctx?.expression())
                return VariableDeclarationStatement(true, name, type, expression)
            }
            "return" -> {
                val expression = visitExpression(ctx?.expression())
                return ReturnStatement(expression)
            }
        }
        return Noop()
    }

    override fun visitExpression(ctx: LangParser.ExpressionContext?): Expression {
        ctx?.INT()?.symbol?.text?.let {
            return IntegerLiteral(it.toInt())
        }
        ctx?.ID()?.symbol?.text?.let {
            return Reference(it)
        }
        ctx?.OPERATOR()?.symbol?.text?.let {
            val a = visitExpression(ctx.expression(0))
            val b = visitExpression(ctx.expression(1))
            return FunctionCallExpression(it, listOf(a, b))
        }
        ctx?.functionCall()?.let {
            val name = it.ID().symbol.text
            val expressions = it.expression().map { visitExpression(it) }
            return FunctionCallExpression(name, expressions)
        }
        ctx?.typeInitialization()?.let {
            val name = it.ID().symbol.text
            val expressions = it.expression().map { visitExpression(it) }
            return TypeInitializationExpression(name, expressions)
        }

        val firstToken = ctx?.getChild(0)?.text.orEmpty()
        when (firstToken) {
            "(" -> return visitExpression(ctx?.expression(0))
            "true", "false" -> return BooleanLiteral(firstToken.toBoolean())
        }

        return IntegerLiteral(0) // TODO: Remove
    }

}
