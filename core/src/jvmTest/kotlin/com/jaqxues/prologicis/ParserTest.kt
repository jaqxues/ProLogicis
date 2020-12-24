package com.jaqxues.prologicis

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails


/**
 * This file was created by Jacques Hoffmann (jaqxues) in the Project ProLogicis.<br>
 * Date: 24.12.20 - Time 17:50.
 */
class ParserTest {
    @Test
    fun parseCorrectExamples() {
        val A = Symbol("A")
        val B = Symbol("B")
        val C = Symbol("C")
        val D = Symbol("D")
        val examples = mapOf(
            "A implies B" to (A implies B),
            "A implies not B" to (A implies not(B)),
            "not A <-> not B" to (not(A) iif not(B)),
            "not(A) <-> not(B)" to (not(A) iif not(B)),
            "A implies (B or C)" to (A implies (B or C)),
            "not A and not C iif B" to ((not(A) and not(C)) iif B),
            "B or C implies (A and D)" to ((B or C) implies (A and D)),
            "A and not(B) implies (C iif D)" to ((A and not(B)) implies (C iif D)),
            "(A and not((C iif D) or A) implies (A and not B)) iif (C and not(C or D) implies A)" to
                    ((A and not((C iif D) or A) implies (A and not(B))) iif (C and not(C or D) implies A))
        )
        examples.forEach { (k, v) ->
            assertEquals(parseInput(k), v, "Unmatched Parser Output")
        }
    }

    @Test
    fun parseIncorrectExamples() {
        arrayOf(
            "A implies (C",
            "A implies (C and not A",
            "A and not",
            "A iif (C or D implies <-> A)",
            "A <- B",
            "A ->implies B",
            "A or B implies C (or C and D)"
        ).forEach { assertFails { parseInput(it) } }
    }
}