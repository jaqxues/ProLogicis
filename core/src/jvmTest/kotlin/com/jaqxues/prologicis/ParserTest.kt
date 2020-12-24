package com.jaqxues.prologicis

import kotlin.test.Test
import kotlin.test.assertEquals


/**
 * This file was created by Jacques Hoffmann (jaqxues) in the Project ProLogicis.<br>
 * Date: 24.12.20 - Time 17:50.
 */
class ParserTest {
    @Test
    fun parseExamples() {
        val A = Symbol("A")
        val B = Symbol("B")
        val C = Symbol("C")
        val D = Symbol("D")
        val examples = mapOf(
            "A implies B" to (A implies B),
            "A implies not B" to (A implies not(B)),
            "not A and not C iif B" to ((not(A) and not(C)) iif B),
            "(A and not((C iif D) or A) implies (A and not B)) iif (C and not(C or D) implies A)" to
                    ((A and not((C iif D) or A) implies (A and not(B))) iif (C and not(C or D) implies A))
        )
        examples.forEach { (k, v) ->
            println(parseInput(k))
            println(v)
            assertEquals(parseInput(k), v, "Unmatched Parser Output")
        }
    }
}