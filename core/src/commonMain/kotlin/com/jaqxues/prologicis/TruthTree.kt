package com.jaqxues.prologicis


/**
 * This file was created by Jacques Hoffmann (jaqxues) in the Project ProLogicis.<br>
 * Date: 04.12.20 - Time 20:10.
 */
typealias SentenceNode = Node<NodeContent>

const val DECOMPOSABLE = -1
const val PRIMITIVE = -2

data class NodeContent(val sentence: Sentence, var decomposedAt: Int)

data class TruthTreeResult(
    val premisses: List<Sentence>,
    val topNode: SentenceNode,
    val conclusionNode: SentenceNode,
    val valid: Boolean
) {
    val latexFormat get() = formatTreeAsLatex(*premisses.toTypedArray(), conclusionNode = conclusionNode)
    val graphVizDotFormat get() = topNode.graphVizDotFormat
}

fun performTreeAlgorithm(vararg premisses: Sentence, entails: Sentence): TruthTreeResult {
    val top = unexploredNode(premisses.first())
    var current = top
    (premisses.drop(1) + not(entails)).forEach { s ->
        current.addChild(unexploredNode(s, parent = current).also { current = it })
    }
    val conclusionNode = current

    var currentStep = 1

    var openLeaves: List<SentenceNode>
    while (conclusionNode.openLeaves.toList().also {
            openLeaves = it
        }.isNotEmpty()) {
        val toDecompose =
            openLeaves.first().withAllParents.toList().filter { it.content.decomposedAt == -1 }.minByOrNull {
                val s = it.content.sentence
                if (s is And || (s is Not && (s.sentence is Or || s.sentence is Implication))) 0 else 1
            } ?: return TruthTreeResult(premisses.toList(), top, conclusionNode, false)
        toDecompose.content.decomposedAt = currentStep++
        for (subLeaf in toDecompose.openLeaves) {
            toDecompose.content.sentence.decomposeTo(subLeaf)
        }
    }
    return TruthTreeResult(premisses.toList(), top, conclusionNode, true)
}

private fun Sentence.decomposeTo(node: SentenceNode) {
    when (this) {
        is And -> {
            var n = node
            for (s in sentences) {
                n.addChild(unexploredNode(s, n).also { n = it })
            }
        }
        is Not -> {
            when (val notSentence = sentence) {
                is Or -> {
                    var n = node
                    for (s in notSentence.sentences) {
                        n.addChild(unexploredNode(not(s), n).also { n = it })
                    }
                }
                is Implication -> {
                    val node1 = unexploredNode(notSentence.s1, node)
                    val node2 = unexploredNode(not(notSentence.s2), node1)
                    node.addChild(node1)
                    node1.addChild(node2)
                }
                is And -> {
                    for (s in notSentence.sentences) {
                        node.addChild(unexploredNode(not(s), node))
                    }
                }
                is Equality -> {
                    val node11 = unexploredNode(notSentence.s1, node)
                    val node12 = unexploredNode(not(notSentence.s2), node11)
                    val node21 = unexploredNode(not(notSentence.s1), node)
                    val node22 = unexploredNode(notSentence.s2, node21)
                    node.addChild(node11)
                    node.addChild(node21)
                    node11.addChild(node12)
                    node21.addChild(node22)
                }
                else -> error("No decomposition possible")
            }
        }
        is Or -> {
            for (s in sentences)
                node.addChild(unexploredNode(s, node))
        }
        is Implication -> {
            node.addChild(unexploredNode(not(s1), node))
            node.addChild(unexploredNode(s2, node))
        }
        is Equality -> {
            val node11 = unexploredNode(s1, node)
            val node12 = unexploredNode(s2, node11)
            val node21 = unexploredNode(not(s1), node)
            val node22 = unexploredNode(not(s2), node21)
            node.addChild(node11)
            node.addChild(node21)
            node11.addChild(node12)
            node21.addChild(node22)
        }
        else -> error("No decomposition possible")
    }
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

private fun unexploredNode(sentence: Sentence, parent: SentenceNode? = null): SentenceNode {
    val s = sentence.deepSimplified
    return Node(NodeContent(s, (if (s.isPrimitive) PRIMITIVE else DECOMPOSABLE)), parent)
}