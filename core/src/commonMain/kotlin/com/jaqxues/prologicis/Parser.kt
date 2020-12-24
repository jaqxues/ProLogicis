package com.jaqxues.prologicis


/**
 * This file was created by Jacques Hoffmann (jaqxues) in the Project ProLogicis.<br>
 * Date: 22.12.20 - Time 14:20.
 */

internal enum class OperationType {
    NOT, AND, OR, IMPLICATION, EQUALITY;

    // Precedence Rules: (No generally accepted order for Implication/Equality -> in this case just using the ordinal)
    // Not < And < Or < Implication, Equality
    val precedence: Int get() = ordinal
    val leftAssociative: Boolean get() = true
    val isBinary: Boolean get() = this !== NOT
}

internal sealed class Token {
    object OpenParenthesis : Token()
    object CloseParenthesis : Token()
    data class Identifier(val content: String) : Token()
    data class Operation(val op: OperationType) : Token()
    object EOF: Token()
}

/*
Symbol -> [a-ZA-Z]+
E -> Symbol|(E)|E K E|'not' E|
K -> 'implies'|'->'|'and'|'or'|'iif'|'<->'

Or:
Sentence -> AtomicSentence|ComplexSentence
AtomicSentence -> [a-zA-Z_$]+
ComplexSentence -> (Sentence)|[Sentence]
                   |not Sentence
                   |Sentence and Sentence
                   |Sentence or Sentence
                   |Sentence implies Sentence
                   |Sentence iif Sentence
Operator Precedence: not, and, or, implies, iif

Or:
E --> P {B P}
P --> v | "(" E ")" | U P
B --> "+" | "-" | "*" | "/" | "^"
U --> "-"
 */

private class Parser(token: Sequence<Token>) {
    val tokenIterator = token.iterator()
    var current = tokenIterator.next()

    val variables = mutableMapOf<String, Symbol>()
    val operators = ArrayDeque<OperationType?>()
    val operands = ArrayDeque<Sentence>()

    fun consume() {
        if (tokenIterator.hasNext())
            current = tokenIterator.next()
    }

    fun compileE() {
        compileP()
        var c = current
        while (c is Token.Operation && c.op.isBinary) {
            pushOperator(c.op)
            consume()
            compileP()
            c = current
        }
        while (operators.last() != null) {
            popOperator()
        }
    }

    fun expect(token: Token) {
        check(current == token) { "Expected Token $token but received Token $current" }
        consume()
    }

    fun compileP() {
        when (val c = current) {
            is Token.Identifier -> {
                operands.add(variables.getOrPut(c.content) { Symbol(c.content) })
                consume()
            }
            is Token.OpenParenthesis -> {
                consume()
                operators.add(null)
                compileE()
                expect(Token.CloseParenthesis)
                operators.removeLast()
            }
            is Token.Operation -> {
                check(!c.op.isBinary) { "Expected Unary Operation, received ${c.op}" }
                pushOperator(c.op)
                consume()
                compileP()
            }
            else -> error("Received invalid Token $c")
        }
    }

    fun popOperator() {
        val op = operators.removeLast() ?: error("Unexpected sentinel value")
        if (op.isBinary) {
            val t2 = operands.removeLast()
            val t1 = operands.removeLast()
            operands.add(
                when (op) {
                    OperationType.IMPLICATION -> t1 implies t2
                    OperationType.EQUALITY -> t1 iif t2
                    OperationType.AND -> t1 and t2
                    OperationType.OR -> t1 or t2
                    else -> error("Unknown Binary Operator")
                }
            )
        } else {
            operands.add(not(operands.removeLast()))
        }
    }

    fun pushOperator(op: OperationType) {
        while (operators.last().let { it != null && it.precedence <= op.precedence }) {
            popOperator()
        }
        operators.add(op)
    }
}

fun parseInput(input: String) {
    val p = Parser(runLexer(input))
    p.operators.add(null)
    p.compileE()
    p.expect(Token.EOF)
    check(!p.tokenIterator.hasNext())
    println(p.operands)
}

abstract class CharNavIterator : CharIterator() {
    abstract fun previous(): Char
}

val String.navIterator
    get() = object : CharNavIterator() {
        private var index = 0
        override fun nextChar(): Char = get(index++)
        override fun previous(): Char = get(--index)
        override fun hasNext(): Boolean = index < length
    }

private fun runLexer(input: String) = sequence {
    println(OperationType.NOT < OperationType.OR)
    println()
    println(input)
    val ci = input.navIterator
    while (ci.hasNext()) {
        when (val c = ci.nextChar()) {
            '"', '\'' -> {
                yield(Token.Identifier(buildString {
                    var char: Char
                    var escaped = false
                    while (ci.hasNext()) {
                        char = ci.nextChar()
                        if (char == '\\') escaped = true
                        else if (!escaped && char == c) return@buildString
                        else append(char)
                    }
                    error("String Symbol spanned until EOF signal!")
                }))
            }
            '(' -> {
                yield(Token.OpenParenthesis)
            }
            ')' -> {
                yield(Token.CloseParenthesis)
            }
            in ('a'..'z') + ('A'..'Z') + "_\$" -> {
                val s = buildString {
                    var char = c
                    append(char)
                    while (ci.hasNext()) {
                        char = ci.nextChar()
                        if (char in (('a'..'z') + ('A'..'Z') + "_\$"))
                            append(char)
                        else {
                            ci.previous()
                            break
                        }
                    }
                }
                when (s) {
                    "implies" -> yield(Token.Operation(OperationType.IMPLICATION))
                    "iif" -> yield(Token.Operation(OperationType.EQUALITY))
                    "and" -> yield(Token.Operation(OperationType.AND))
                    "or" -> yield(Token.Operation(OperationType.OR))
                    "not" -> yield(Token.Operation(OperationType.NOT))
                    else -> yield(Token.Identifier(s))
                }
            }
            in "<->" -> {
                val operator = buildString {
                    append(c)
                    var char: Char
                    while (ci.hasNext()) {
                        char = ci.nextChar()
                        if (char in "<->") append(char)
                        else {
                            ci.previous()
                            break
                        }
                    }
                }
                when (operator) {
                    "->" -> yield(Token.Operation(OperationType.IMPLICATION))
                    "<->" -> yield(Token.Operation(OperationType.EQUALITY))
                    else -> error("Unknown Operator '$operator'")
                }
            }
            ' ', '\n' -> Unit
        }
    }
    yield(Token.EOF)
}
