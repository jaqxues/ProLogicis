package com.jaqxues.prologicis

/**
 * This file was created by Jacques Hoffmann (jaqxues) in the Project ProLogicis.<br>
 * Date: 04.12.20 - Time 20:10.
 *
 *
 * Find optimal solution for shortest proof tree with logical premisses and conclusion (brute-force)
 *
 * Each Node in the tree contains a logical sentence, and an action space, list of sentences to be
 * decomposed. A Node is 'open' when there are no contradictions in its path to the root node.
 *
 * Decomposing to a node means applying decomposition rules of propositional logic and inheriting
 * the action space of the parent node + sentence of current node if sentence is not a primitive
 * (Symbol or Not(Symbol)). Every other type can be decomposed using the 7 rules.
 *
 * 1. Build tree root and trunk (premisses + conclusion).
 * 2. Select an open leaf-node with non-empty action space (order irrelevant, each node has to be
 *    fully decomposed anyway). Break loop if no such leaves.
 * 3. Execute Subroutine to determine the optimal action in the action space of the current node to
 *    reach optimal solution.
 * 4. Perform computed action: decompose sentence to every node that contains it (identity check).
 * 5. Repeat from step 2.
 * 6. At this point the tree is completely decomposed (meaning empty action space), either with or
 *    without open leaf nodes.
 *
 *
 * Optimal Action Subroutine - Inputs: Conclusion Node and selected node. In order to find the
 * optimal solution in any case, this single subroutine has to establish and crawl over all possible
 * tree structures exhaustively. It returns the "complexity" of each action that can be taken.
 * 1. Map each action to a complexity counter (variable keeping track of size of tree)
 * 2. Select action in action space of current node and search action in all leaf-children of the
 *    conclusion node. Add selected action to map, set complexity variable to 0
 * 3. Generate a copy of the entire tree from conclusion node.
 * 4. Perform selected action, decompose to queried nodes and increase complexity variable by number
 *    of added nodes.
 * 6. Recursively perform subroutine on modified copy of the conclusion node. Use return value to
 *    find the minimum value. Add value to complexity variable.
 * 7. Repeat step 1 for other action.
 * 8. Return map of action - complexity
 */
private typealias SentenceNode = Node<MutableNodeContent>
typealias TruthTreeNode = Node<NodeContent>

class Node<T>(
    val content: T,
    parent: Node<T>?,
    children: List<Node<T>>
) {
    var parent: Node<T>? = parent
        internal set
    var children: List<Node<T>> = children
        internal set


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

    val leaves
        get() = sequence {
            val q = ArrayDeque(listOf(this@Node))
            while (q.isNotEmpty()) {
                val it = q.iterator()
                while (it.hasNext()) {
                    val node = it.next()
                    if (node.children.isEmpty())
                        yield(node)
                    else
                        q.addAll(node.children)
                    it.remove()
                }
            }
        }

    fun getNthChild(n: Int): Node<T> {
        var tmp = n
        var current = this
        while (tmp-- > 0) {
            check(current.children.size == 1)
            current = current.children[0]
        }
        return current
    }

    val isLeaf: Boolean get() = children.isEmpty()

    internal fun addChild(child: Node<T>) = child.also { children = children + it }
}

internal interface INodeContent {
    val sentence: Sentence
    val decomposedAt: Int
}

internal data class MutableNodeContent(
    override val sentence: Sentence,
    val actionSpace: MutableList<Sentence>,
    override var decomposedAt: Int = -1
) : INodeContent

data class NodeContent(
    override val sentence: Sentence,
    override val decomposedAt: Int
) : INodeContent

data class TruthTreeResult(
    val premisses: List<Sentence>,
    val topNode: TruthTreeNode,
    val conclusionNode: TruthTreeNode,
    val valid: Boolean
) {
    val latexFormat
        get() = formatTreeAsLatex(
            premisses.size,
            topNode = topNode
        )

    val graphVizDotFormat get() = topNode.graphVizDotFormat(false)
    val digraphVizDotFormat get() = topNode.graphVizDotFormat(true)
}

private fun SentenceNode.deepCopy(parent: SentenceNode? = null): SentenceNode {
    val cp = Node(
        content.copy(actionSpace = content.actionSpace.toMutableList()),
        parent,
        children = mutableListOf()
    )
    cp.children = children.map { it.deepCopy(cp) }
    return cp
}

fun <C : INodeContent> Node<C>.getOpenWith(previous: Set<Sentence> = emptySet()) =
    leaves.filter { leaf ->
        val allSentences =
            leaf.withAllParents.mapTo(mutableSetOf()) { it.content.sentence } + previous
        val negated = allSentences.filterIsInstance<Not>()
        val truthy = allSentences - negated
        (negated.map { it.sentence } intersect truthy).isEmpty()
    }

private fun createNode(content: Sentence, parent: SentenceNode?): SentenceNode {
    val s = content.deepSimplified
    val newActions = parent?.content?.actionSpace?.toMutableList() ?: mutableListOf()
    if (!s.isPrimitive) newActions.add(s)
    val node = Node(MutableNodeContent(s, newActions), parent, emptyList())
    parent?.addChild(node)
    return node
}

fun performTreeAlgorithm(
    vararg premisses: Sentence,
    entails: Sentence,
    bruteforceMethod: Boolean = true
): TruthTreeResult {
    val top = createNode(premisses.first(), null)
    var current = top
    premisses.drop(1).forEach { current = createNode(it, current) }

    val negatedConclusion = not(entails)
    val conclusionNode = createNode(negatedConclusion, current)

    var i = 1
    if (bruteforceMethod) {
        while (conclusionNode.getOpenWith().firstOrNull { it.content.actionSpace.isNotEmpty() }
                ?.also { current = it } != null) {
            val action = getRatedActions(
                conclusionNode.withAllParents.map { it.content.sentence }.toSet(),
                conclusionNode,
                current
            ).entries.minByOrNull { it.value }!!.key
            conclusionNode
                .getOpenWith()
                .filter { n -> n.content.actionSpace.any { it === action } }
                .forEach { action.decomposeTo(it) }
            top.withAllChildren.first { it.content.sentence === action }.content.decomposedAt = i++
        }
    } else {
        var openLeaves: List<SentenceNode>
        while (conclusionNode.getOpenWith().toList().also { openLeaves = it }.isNotEmpty()) {
            val toDecompose = openLeaves.first().withAllParents.toList().filter {
                it.content.decomposedAt == -1 && !it.content.sentence.isPrimitive
            }.minByOrNull {
                val s = it.content.sentence
                if (s is And || (s is Not && (s.sentence is Or || s.sentence is Implication))) 0 else 1
            } ?: break
            toDecompose.content.decomposedAt = i++
            toDecompose.getOpenWith().forEach {
                toDecompose.content.sentence.decomposeTo(it, delAction = false)
            }
        }
    }
    val transformedTop = transformTree(top)
    val transformedConcl = transformedTop.getNthChild(premisses.size)
    return TruthTreeResult(
        premisses.toList(),
        transformedTop,
        transformedConcl,
        conclusionNode.getOpenWith().none()
    )
}

private fun transformTree(n: SentenceNode): TruthTreeNode =
    TruthTreeNode(
        content = NodeContent(n.content.sentence, n.content.decomposedAt),
        parent = null,
        children = n.children.map { transformTree(it) }
    ).also {
        it.children.forEach { c -> c.parent = it }
    }

private fun getRatedActions(
    previous: Set<Sentence>,
    conclusionNode: SentenceNode,
    selected: SentenceNode
): Map<Sentence, Int> {
    return selected.content.actionSpace.associateWith { s ->
        var complexity = 0
        val deepCopy = conclusionNode.deepCopy()
        deepCopy
            .getOpenWith(previous)
            .filter { n -> n.content.actionSpace.any { it === s } }
            .forEach { complexity += s.decomposeTo(it) }
        deepCopy.getOpenWith(previous).firstOrNull { it.content.actionSpace.isNotEmpty() }
            ?.let { n ->
                complexity += getRatedActions(previous, deepCopy, n).values.minOrNull()!!
            }
        complexity
    }
}

private fun Sentence.decomposeTo(node: SentenceNode, delAction: Boolean = true): Int {
    if (delAction)
        check(node.content.actionSpace.remove(this))
    when (this) {
        is And -> {
            var n = node
            for (s in sentences)
                n = createNode(s, n)
            return sentences.size
        }
        is Not -> {
            when (val notSent = sentence) {
                is Or -> {
                    var n = node
                    for (s in notSent.sentences)
                        n = createNode(not(s), n)
                    return notSent.sentences.size
                }
                is Implication -> {
                    createNode(not(notSent.s2), createNode(notSent.s1, node))
                    return 2
                }
                is And -> {
                    for (s in notSent.sentences)
                        createNode(not(s), node)
                    return notSent.sentences.size
                }
                is Equality -> {
                    createNode(not(notSent.s2), createNode(notSent.s1, node))
                    createNode(notSent.s2, createNode(not(notSent.s1), node))
                    return 4
                }
                else -> error("No decomposition possible")
            }
        }
        is Or -> {
            sentences.forEach { createNode(it, node) }
            return sentences.size
        }
        is Implication -> {
            createNode(not(s1), node)
            createNode(s2, node)
            return 2
        }
        is Equality -> {
            createNode(s2, createNode(s1, node))
            createNode(not(s2), createNode(not(s1), node))
            return 4
        }
        else -> error("No decomposition possible")
    }
}