package xyz.jadonfowler.compiler

import org.antlr.v4.runtime.ANTLRInputStream
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.RuleContext
import xyz.jadonfowler.compiler.ast.PrimitiveType
import xyz.jadonfowler.compiler.parser.LangLexer
import xyz.jadonfowler.compiler.parser.LangParser
import xyz.jadonfowler.compiler.pass.stage0.ASTBuilder
import xyz.jadonfowler.compiler.pass.stage0.TypeRetriever

fun main(args: Array<String>) {
    compileString("""
# Type Definitions
type P = a : Int, b : Int, c : Int
type S = A Int Int | B Int

# Functions
f (a : Int, b : Int) : Int
    let c = 7,
    let t : Int = 9,
    if c == 8
        let d = 9,
        let e = 7 * 9 + 7,
        return d + e
    ;
    (a + b + c).
""")
}

fun compileString(s: String) {
    val stream = ANTLRInputStream(s)
    val lexer = LangLexer(stream)
    val tokens = CommonTokenStream(lexer)
    val parser = LangParser(tokens)
    val result = parser.module()
    explore(result)
    val primitiveTypes = listOf(PrimitiveType("Int"), PrimitiveType("Bool"))
    val types = TypeRetriever(result, primitiveTypes).types
    types.addAll(primitiveTypes)
    val module = ASTBuilder("name", result, types).module
    print(module)
}

fun explore(ctx: RuleContext, indentation: Int = 0) {
    val ignore = ctx.childCount == 1 && ctx.getChild(0) is ParserRuleContext
    if (!ignore) {
        val ruleName = LangParser.ruleNames[ctx.ruleIndex]
        println("    ".repeat(indentation)
                + ctx.javaClass.name.split(".").last()
                + " " + ruleName + ":\n"
                + "    ".repeat(indentation)
                + ctx.text
                + "\n")
    }

    for (i in 0..ctx.childCount - 1) {
        val element = ctx.getChild(i)
        if (element is RuleContext) {
            explore(element, indentation + (if (ignore) 0 else 1))
        }
    }
}
