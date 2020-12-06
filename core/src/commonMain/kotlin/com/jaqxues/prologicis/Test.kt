package com.jaqxues.prologicis


/**
 * This file was created by Jacques Hoffmann (jaqxues) in the Project ProLogicis.<br>
 * Date: 04.12.20 - Time 20:10.
 */

fun performTreeAlgorithm(vararg premisses: Sentence, entails: Sentence): Boolean {
    premisses.forEach { println(it.format()) }
    val negatedConclusion = not(entails)
    println()
    println(negatedConclusion.format())

    val top = Node(null, premisses.first())
    for (sentence in mutableListOf(negatedConclusion) + premisses) {
        for (node in top.openLeaves) {
            if (!(sentence is Symbol || (sentence is Not && sentence.sentence is Symbol))) {
                node.decompose(sentence)
            }
        }
    }

    printAsLatexTree(top)
    return false
}

fun Sentence.simplifyOrThis(): Sentence {
    if (this is Not && sentence is Not) {
        return sentence.sentence
    }
    // TODO Xor
    return this
}

fun Node<Sentence>.decompose(sentence: Sentence) {
    when (sentence) {
        is And -> {
            var node = this
            for (s in sentence.sentences) {
                node.addChild(Node(node, s).also { node = it})
            }
        }
        is Not -> {
            when (val notSentence = sentence.sentence) {
                is Or -> {
                    var node = this
                    for (s in notSentence.sentences) {
                        node.addChild(Node(node, not(s)).also { node = it })
                    }
                }
                is Implication -> {
                    val node1 = Node(this, notSentence.s1)
                    val node2 = Node(node1, not(notSentence.s2))
                    this.addChild(node1)
                    node1.addChild(node2)
                }
                is And -> {
                    for (s in notSentence.sentences) {
                        addChild(Node(this, not(s)))
                    }
                }
                is Equality -> {
                    val node11 = Node(this, notSentence.s1)
                    val node12 = Node(node11, Not(notSentence.s2))
                    val node21 = Node(this, Not(notSentence.s1))
                    val node22 = Node(node21, notSentence.s2)
                    this.addChild(node11)
                    this.addChild(node21)
                    node11.addChild(node12)
                    node21.addChild(node22)
                }
                else -> error("No decomposition possible")
            }
        }
        is Or -> {
            for (s in sentence.sentences)
                addChild(Node(this, s))
        }
        is Implication -> {
            addChild(Node(this, Not(sentence.s1)))
            addChild(Node(this, sentence.s2))
        }
        is Equality -> {
            val node11 = Node(this, sentence.s1)
            val node12 = Node(this, sentence.s2)
            val node21 = Node(this, Not(sentence.s1))
            val node22 = Node(this, Not(sentence.s2))
            this.addChild(node11)
            this.addChild(node21)
            node11.addChild(node12)
            node21.addChild(node22)
        }
        else -> error("No decomposition possible")
    }
}

class Node<T>(val parent: Node<T>?, val content: T) {
    val openLeaves get(): Sequence<Node<T>> = sequence {
        if (children.isNotEmpty()) {
            for (child in children)
                yieldAll(child.openLeaves)
        } else {
            yield(this@Node)
        }
    }
    val children: MutableList<Node<T>> = mutableListOf()
    fun addChild(child: Node<T>) = children.add(child)
}


val Node<Sentence>.latexFormat get() =
    content.format()
        .replace("<->", "\$\\leftrightarrow\$")
        .replace("->", "\$\\rightarrow\$")


fun printAsLatexTree(top: Node<Sentence>) {

    val begin = """
        \documentclass[border=10pt]{standalone}
        \usepackage{tikz}
        \begin{document}
        \begin{tikzpicture}[sibling distance=10em,
          every node/.style = {shape=rectangle, rounded corners,
            draw, align=center,
            top color=white, bottom color=blue!20}]]
          \node {${top.latexFormat}}
    """.trimIndent()

    val end = """
        \end{tikzpicture}
        \end{document}
    """.trimIndent()

    println(begin)

    fun printLatexNode(node: Node<Sentence>, offset: Int) {
        print("\t".repeat(offset))
        println("child {")
        print("\t".repeat(offset + 1))
        println("node {${node.latexFormat}}")
        for (n in node.children)
            printLatexNode(n, offset + 1)
        print("\t".repeat(offset + 1))
        println("}")
    }
    for (n in top.children)
        printLatexNode(n, 2)
    println("\t;")
    println(end)
}
