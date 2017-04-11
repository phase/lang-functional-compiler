package xyz.jadonfowler.compiler.pass.stage0

import xyz.jadonfowler.compiler.ast.ProductType
import xyz.jadonfowler.compiler.ast.SumType
import xyz.jadonfowler.compiler.ast.Type
import xyz.jadonfowler.compiler.ast.from
import xyz.jadonfowler.compiler.parser.LangBaseVisitor
import xyz.jadonfowler.compiler.parser.LangParser

class TypeRetriever(ctx: LangParser.ModuleContext?, val knownTypes: List<Type>) : LangBaseVisitor<Type?>() {

    val types = mutableListOf<Type>()

    init {
        visitModule(ctx)
    }

    override fun visitModule(ctx: LangParser.ModuleContext?): Type? {
        ctx?.externalDeclaration()?.filter { it.typeDeclaration() != null }?.forEach { visitTypeDeclaration(it.typeDeclaration()) }
        return null
    }

    override fun visitTypeDeclaration(ctx: LangParser.TypeDeclarationContext?): Type? {
        val typeName = ctx?.ID()?.symbol?.text.orEmpty()

        ctx?.productType()?.let {
            types.add(createProductType(typeName, it))
        }
        ctx?.sumType()?.let {
            types.add(createSumType(typeName, it))
        }

        return null
    }

    fun createProductType(typeName: String, ctx: LangParser.ProductTypeContext): ProductType {
        val fields = mutableMapOf<String, Type>()
        val fieldCount = ctx.ID().size / 2
        // Go through the IDs in twos
        (0..fieldCount - 1).forEach {
            val fieldName = ctx.ID(it * 2).symbol.text
            val fieldTypeName = ctx.ID(it * 2 + 1).symbol.text
            val fieldType = if (types.map(Type::name).contains(fieldTypeName)) {
                types.from(fieldTypeName)
            } else {
                knownTypes.from(fieldTypeName)
            }
            fields.put(fieldName, fieldType)
        }
        return ProductType(typeName, fields)
    }

    fun createSumType(typeName: String, ctx: LangParser.SumTypeContext): SumType {
        val variants = mutableMapOf<String, List<Type>>()

        var variantName = ""
        var variantTypes = mutableListOf<Type>()
        ctx.children.forEach {
            if (it.text == "|") {
                variants.put(variantName, variantTypes)

                // Reset
                variantName = ""
                variantTypes = mutableListOf()
            } else if (variantName == "") {
                variantName = it.text
            } else {
                val variantType = if (types.map(Type::name).contains(it.text)) {
                    types.from(it.text)
                } else {
                    knownTypes.from(it.text)
                }
                variantTypes.add(variantType)
            }
        }
        // Final one isn't caught
        variants.put(variantName, variantTypes)

        return SumType(typeName, variants)
    }

}
