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
        statements.addAll(visitStatementList(ctx?.statementList()))
        return StatementList(*statements.toTypedArray())
    }

    override fun visitBlockStatement(ctx: LangParser.BlockStatementContext?): Statement {
        return Noop()
    }

    override fun visitStatement(ctx: LangParser.StatementContext?): Statement {
        return Noop()
    }

}