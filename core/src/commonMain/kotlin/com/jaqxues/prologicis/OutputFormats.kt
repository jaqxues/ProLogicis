package com.jaqxues.prologicis


/**
 * This file was created by Jacques Hoffmann (jaqxues) in the Project ProLogicis.<br>
 * Date: 22.12.20 - Time 13:51.
 */
fun formatTreeAsLatex(vararg premisses: Sentence, conclusionNode: SentenceNode) = buildString {
    append("[.{")
    (premisses.toList() + conclusionNode.content.sentence)
        .joinTo(this, separator = "\\\\") { "\${${it.latexFormat}}\$" }
    append("} ")
    conclusionNode.children.forEach {
        append(it.latexFormat)
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
                append(sentences.joinToString(separator = " \\et ") { it.latexFormat })
            }
            is Or -> {
                append(sentences.joinToString(separator = " \\ou ") { it.latexFormat })
            }
            is Implication -> {
                append(s1.latexFormat)
                append(" \\si ")
                append(s2.latexFormat)
            }
            is Equality -> {
                append(s1.latexFormat)
                append(" \\eq ")
                append(s2.latexFormat)
            }
        }
    }

val SentenceNode.latexFormat: String
    get() = buildString {
        if (withAllChildren.any { it.children.size > 1 }) {
            append(" [.")
            append("{")
            var current = this@latexFormat
            while (current.parent!!.children.size <= 1 || current == this@latexFormat) {
                append("{\$")
                append(current.content.sentence.latexFormat)
                append("\$}")
                append("\\\\")
                current = current.children.first()
            }
            current = current.parent!!
            append("} ")
            current.children.forEach {
                append(it.latexFormat)
                append(' ')
            }
            append(']')
            return@buildString
        }
        append('{')
        withAllChildren.joinTo(this, separator = "\\\\") {
            "{\$${it.content.sentence.latexFormat}\$}"
        }
        append('}')
    }

val SentenceNode.graphVizDotFormat: String
    get() = buildString {
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
        fun getCorrectFormatFor(node: SentenceNode): Any {
            if (node in multiNodes) return multiNodes.indexOf(node)
            val s = node.content.sentence
            if ("[a-zA-Z_]+".toRegex() matches s.format()) return s.format()
            return "\"${s.format()}\""
        }
        for (child in withAllChildren.drop(1)) {
            append("\t")
            append(getCorrectFormatFor(child.parent ?: error("No parent for non-top Node")))
            append(" -- ")
            append(getCorrectFormatFor(child))
            append(";\n")
        }
        append("}")
    }
