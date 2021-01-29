package com.jaqxues.prologicis


/**
 * This file was created by Jacques Hoffmann (jaqxues) in the Project ProLogicis.<br>
 * Date: 22.12.20 - Time 13:51.
 */
fun formatTreeAsLatex(nbPremisses: Int, topNode: TruthTreeNode) = buildString {
    append("[.{")
    val openLeaves = topNode.getOpenWith().toSet()

    topNode.withAllChildren.take(nbPremisses + 1)
        .joinTo(this, separator = "\\\\") {
            val f = "\${${it.content.sentence.latexFormat}}\$"
            if (it.content.decomposedAt > 0) f + " ^" + it.content.decomposedAt else f
        }
    append("} ")
    topNode.getNthChild(nbPremisses).children.forEach {
        append(it.latexFormat(openLeaves))
    }
    append(" ]")
}

val Sentence.latexFormat: String
    get() = buildString {
        when (this@latexFormat) {
            is Symbol -> {
                append(this@latexFormat.proposition)
                return@buildString
            }
            is Not -> {
                append("\\non{")
                append(sentence.latexFormat)
                append("}")
            }
            is And -> {
                append(sentences.joinToString(separator = " \\et ") { it.withParentheses(Sentence::latexFormat) })
            }
            is Or -> {
                append(sentences.joinToString(separator = " \\ou ") { it.withParentheses(Sentence::latexFormat) })
            }
            is Implication -> {
                append(s1.withParentheses(Sentence::latexFormat))
                append(" \\si ")
                append(s2.withParentheses(Sentence::latexFormat))
            }
            is Equality -> {
                append(s1.withParentheses(Sentence::latexFormat))
                append(" \\eq ")
                append(s2.withParentheses(Sentence::latexFormat))
            }
        }
    }

fun <C: INodeContent> Node<C>.latexFormat(openLeaves: Set<Node<C>>) : String
     = buildString {
        if (withAllChildren.any { it.children.size > 1 }) {
            append(" [.")
            append("{")
            var current = this@latexFormat
            while (current.parent!!.children.size <= 1 || current == this@latexFormat) {
                current.formatLatexSingleNode(openLeaves, builder = this)
                append("\\\\")
                current = current.children.first()
            }
            current = current.parent!!
            append("} ")
            current.children.forEach {
                append(it.latexFormat(openLeaves))
                append(' ')
            }
            append(']')
            return@buildString
        }
        append('{')
        withAllChildren.joinTo(this, separator = "\\\\") {
            it.formatLatexSingleNode(openLeaves).toString()
        }
        append('}')
    }

private fun <C: INodeContent> Node<C>.formatLatexSingleNode(
    openLeaves: Set<Node<C>>,
    builder: StringBuilder = StringBuilder()
) = builder.apply {
    append("{\$")
    append(content.sentence.latexFormat)
    append("\$}")
    if (content.decomposedAt > 0) {
        append(" ^")
        val needsParens = content.decomposedAt > 9
        if (needsParens)
            append("{")
        append(content.decomposedAt)
        if (needsParens)
        append("}")
    } else if (isLeaf) {
        append("\\\\")
        if (this@formatLatexSingleNode in openLeaves)
            append("...")
        else
            append("\$\\times\$")
    }
}

fun <C: INodeContent> Node<C>.graphVizDotFormat(directed: Boolean) = buildString {
    if (directed)
        append("di")
    append("graph {\n")
    val multiNodes = withAllChildren.toList().let { list ->
        list.filter { el -> list.count { el.content.sentence == it.content.sentence } > 1 }
    }
    multiNodes.forEachIndexed { index, n ->
        val s = n.content.sentence
        append("\t")
        append(index)
        append(" [label=\"")
        append(s.format())
        append("\"];\n")
    }
    fun getCorrectFormatFor(node: Node<C>): Any {
        if (node in multiNodes) return multiNodes.indexOf(node)
        val s = node.content.sentence
        if ("[a-zA-Z_]+".toRegex() matches s.format()) return s.format()
        return "\"${s.format()}\""
    }
    val edgeOp = if (directed) " -> " else " -- "
    for (child in withAllChildren.drop(1)) {
        append("\t")
        append(getCorrectFormatFor(child.parent ?: error("No parent for non-top Node")))
        append(edgeOp)
        append(getCorrectFormatFor(child))
        append(";\n")
    }
    append("}")
}
