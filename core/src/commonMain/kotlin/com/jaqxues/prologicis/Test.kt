package com.jaqxues.prologicis


/**
 * This file was created by Jacques Hoffmann (jaqxues) in the Project ProLogicis.<br>
 * Date: 04.12.20 - Time 20:10.
 */
typealias SentenceNode = Node<NodeContent>

const val DECOMPOSABLE = -1
const val PRIMITIVE = -2

data class NodeContent(val sentence: Sentence, var decomposedAt: Int)

fun performTreeAlgorithm(vararg premisses: Sentence, entails: Sentence) = buildString {
    val top = unexploredNode(premisses.first())
    var current = top
    (premisses.drop(1) + not(entails)).forEach { s ->
        current.addChild(unexploredNode(s, parent = current).also { current = it })
    }
    val conclusionNode = current

    var currentStep = 1

    println(formatTreeAsLatex(*premisses, conclusionNode = conclusionNode))

    var openLeaves: List<SentenceNode>
    while (conclusionNode.openLeaves.toList().also {
            openLeaves = it
        }.isNotEmpty()) {
        val toDecompose =
            openLeaves.first().withAllParents.toList().filter { it.content.decomposedAt == -1 }.minByOrNull {
                val s = it.content.sentence
                if (s is And || (s is Not && (s.sentence is Or || s.sentence is Implication))) 0 else 1
            }
        if (toDecompose == null) {
            println("Unclosed Leaf")
            break
        }
        toDecompose.content.decomposedAt = currentStep++
        for (subLeaf in toDecompose.openLeaves) {
            subLeaf.decompose(toDecompose.content.sentence)
        }
    }
//    val (premisses, conclusionNode) = formExampleTree()
    println(formatTreeAsLatex(*premisses, conclusionNode = conclusionNode))
}

private fun formExampleTree(): Pair<Array<Sentence>, SentenceNode> {
    val A = Symbol("A")
    val B = Symbol("B")
    val C = Symbol("C")
    val D = Symbol("D")


    val s1 = (not(A implies B)) or (C iif D)
    val s2 = (not(A) and not(B)) implies not(A and B)
    val conclusion = not(C) or D

    val conclusionNode = Node(NodeContent(not(conclusion), 0))
    var node = Node(NodeContent(C, 0), conclusionNode)
    conclusionNode.addChild(node)
    node.addChild(Node(NodeContent(not(D), 0), node).also { node = it })
    val n1 = Node(NodeContent(not(A implies B), 0), node)
    node.addChild(n1)
    val n2 = Node(NodeContent(C iif D, 0), node)
    node.addChild(n2)
    val na = Node(NodeContent(A, 0), n1)
    n1.addChild(na)
    val n2c = Node(NodeContent(C, 0), n2)
    n2.addChild(n2c)
    val n2nc = Node(NodeContent(not(C), 0), n2)
    n2.addChild(n2nc)
    n2c.addChild(Node(NodeContent(D, 0), n2c))
    n2nc.addChild(Node(NodeContent(not(D), 0), n2nc))

    val nnb = Node(NodeContent(not(B), 0), na)
    na.addChild(nnb)
    val nnb1 = Node(NodeContent(not(not(A) or not(B)), 0), nnb)
    nnb.addChild(nnb1)
    val nnb2 = Node(NodeContent(not(A or B), 0), nnb)
    nnb.addChild(nnb2)
    val nnb1a = Node(NodeContent(A, 0), nnb1)
    nnb1.addChild(nnb1a)
    nnb1a.addChild(Node(NodeContent(B, 0), nnb1a))
    val nnb2na = Node(NodeContent(not(A), 0), nnb2)
    nnb2.addChild(nnb2na)
    nnb2na.addChild(Node(NodeContent(not(B), 0), nnb2na))

    return arrayOf(s1, s2) to conclusionNode
}

val Sentence.isPrimitive get() = this is Symbol || (this is Not && this.sentence is Symbol)

fun unexploredNode(sentence: Sentence, parent: SentenceNode? = null): SentenceNode {
    val s = sentence.deepSimplified
    return Node(NodeContent(s, (if (s.isPrimitive) PRIMITIVE else DECOMPOSABLE)), parent)
}

fun SentenceNode.decompose(sentence: Sentence) {
    when (sentence) {
        is And -> {
            var node = this
            for (s in sentence.sentences) {
                node.addChild(unexploredNode(s, node).also { node = it })
            }
        }
        is Not -> {
            when (val notSentence = sentence.sentence) {
                is Or -> {
                    var node = this
                    for (s in notSentence.sentences) {
                        node.addChild(unexploredNode(not(s), node).also { node = it })
                    }
                }
                is Implication -> {
                    val node1 = unexploredNode(notSentence.s1, this)
                    val node2 = unexploredNode(not(notSentence.s2), node1)
                    this.addChild(node1)
                    node1.addChild(node2)
                }
                is And -> {
                    for (s in notSentence.sentences) {
                        addChild(unexploredNode(not(s), this))
                    }
                }
                is Equality -> {
                    val node11 = unexploredNode(notSentence.s1, this)
                    val node12 = unexploredNode(not(notSentence.s2), node11)
                    val node21 = unexploredNode(not(notSentence.s1), this)
                    val node22 = unexploredNode(notSentence.s2, node21)
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
                addChild(unexploredNode(s, this))
        }
        is Implication -> {
            addChild(unexploredNode(not(sentence.s1), this))
            addChild(unexploredNode(sentence.s2, this))
        }
        is Equality -> {
            val node11 = unexploredNode(sentence.s1, this)
            val node12 = unexploredNode(sentence.s2, node11)
            val node21 = unexploredNode(not(sentence.s1), this)
            val node22 = unexploredNode(not(sentence.s2), node21)
            this.addChild(node11)
            this.addChild(node21)
            node11.addChild(node12)
            node21.addChild(node22)
        }
        else -> error("No decomposition possible")
    }
}

val Sentence.deepSimplified: Sentence
    get() {
        var current = this
        while (true) {
            if (current is Not && current.sentence is Not) {
                current = (current.sentence as Not).sentence
            } else break
        }
        return current
    }

fun formatTreeAsLatex(vararg premisses: Sentence, conclusionNode: SentenceNode) = buildString {
    append("[.{")
    (premisses.toList() + conclusionNode.content.sentence)
        .joinTo(this, separator = "\\\\") { "\${${it.latexFormat}}\$" }
    append("}")
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

class Node<T>(val content: T, val parent: Node<T>? = null, val children: MutableList<Node<T>> = mutableListOf()) {
    fun addChild(child: Node<T>) {
        children.add(child)
    }

    val leaves
        get() = sequence {
            val q = ArrayDeque(listOf(this@Node))
            while (q.isNotEmpty()) {
                val it = q.iterator()
                while (it.hasNext()) {
                    val node = it.next()
                    if (node.children.size == 0)
                        yield(node)
                    else
                        q.addAll(node.children)
                    it.remove()
                }
            }
        }

    val withAllParents
        get() = sequence {
            var parent: Node<T>? = this@Node
            while (parent != null) {
                yield(parent)
                parent = parent.parent
            }
        }

    val withAllChildren
        get() = sequence {
            var q = ArrayDeque(listOf(this@Node))
            while (q.isNotEmpty()) {
                yieldAll(q)
                q = q.flatMapTo(ArrayDeque()) { it.children }
            }
        }
}


@Suppress("UNCHECKED_CAST")
val SentenceNode.openLeaves
    get() = leaves.filter { leaf ->
        val allSentences = leaf.withAllParents.mapTo(mutableSetOf()) { it.content.sentence }
        val negated: Set<Not> = allSentences.filterTo(mutableSetOf()) { it is Not } as Set<Not>
        val truthy = allSentences - negated
        (negated.mapTo(mutableSetOf()) { it.sentence } intersect truthy).isEmpty()
    }