package com.jaqxues.prologicis


/**
 * This file was created by Jacques Hoffmann (jaqxues) in the Project ProLogicis.<br>
 * Date: 05.12.20 - Time 10:42.
 */
typealias Biconditional = Equality

abstract class Sentence {
    abstract fun format(): String

    infix fun implies(other: Sentence) = Implication(this, other)
    infix fun or(other: Sentence) = Or(this, other)
    infix fun xor(other: Sentence) = Xor(this, other)
    infix fun and(other: Sentence) = And(this, other)
    infix fun iif(other: Sentence) = Biconditional(this, other)

    val isPrimitive get() = this is Symbol || (this is Not && this.sentence is Symbol)

    val deepSimplified: Sentence
        get() {
            var current = this
            while (true) {
                if (current is Not && current.sentence is Not) {
                    current = (current.sentence as Not).sentence
                } else break
            }
            return current
        }
}

fun not(sentence: Sentence) = Not(sentence)

data class Symbol(val proposition: String) : Sentence() {
    override fun format() = proposition
}

data class Or(val sentences: List<Sentence>) : Sentence() {
    constructor(vararg sentences: Sentence) : this(sentences.toList())

    override fun format() = sentences.joinToString(separator = " or ") { it.format() }
}

data class Xor(val sentences: List<Sentence>) : Sentence() {
    constructor(vararg sentences: Sentence) : this(sentences.toList())

    override fun format() = sentences.joinToString(separator = " xor ") { it.format() }
}

data class And(val sentences: List<Sentence>) : Sentence() {
    constructor(vararg sentences: Sentence) : this(sentences.toList())

    override fun format() = sentences.joinToString(separator = " and ") { it.format() }
}

data class Not(val sentence: Sentence) : Sentence() {
    override fun format() = "Not(${sentence.format()})"
}

data class Implication(val s1: Sentence, val s2: Sentence) : Sentence() {
    override fun format() = "${s1.format()} -> ${s2.format()}"
}

data class Equality(val s1: Sentence, val s2: Sentence) : Sentence() {
    override fun format() = "${s1.format()} <-> ${s2.format()}"
}