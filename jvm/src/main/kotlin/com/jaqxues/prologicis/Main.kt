package com.jaqxues.prologicis

/**
 * This file was created by Jacques Hoffmann (jaqxues) in the Project ProLogicis.<br>
 * Date: 04.12.20 - Time 19:58.
 */
fun main(args: Array<String>) {
//    val A = Symbol("The students study Mathematics")
//    val B = Symbol("The students study Economics")
//    val C = Symbol("The students study Philosophy")
//    val D = Symbol("The students study French")

//    val s1 = (A or B) implies (C or D)
//    val s2 = not(C implies not(B))
//    val conclusion = A or B

    val A = Symbol("A")
    val B = Symbol("B")
    val C = Symbol("C")
    val D = Symbol("D")
    val E = Symbol("E")

    val P = Symbol("P")
    val R = Symbol("R")
    val S = Symbol("S")
    val T = Symbol("T")
    val Q = Symbol("Q")


    println("Tree Algorithms Tests: ")
    println(performTreeAlgorithm(
        not(A implies B) or (C iif D),
        (not(A) or not(B)) implies not(A or B),
        entails = not(C) or D
    ).prettyOutput)

    println(performTreeAlgorithm(
        (A iif B) implies (not(C) or D),
        A implies B,
        entails = (not(C implies B) or (A and C)) implies D
    ).prettyOutput)

    println(performTreeAlgorithm(
        (A and not(B)) or (not(B) and E),
        (A iif B) implies (not(A) implies not(E)),
        not(A and not(B)),
        entails = not(E)
    ).prettyOutput)


    println(performTreeAlgorithm(
        P iif R,
        (S or T) implies P,
        R and Q,
        not(T),
        entails = R implies not(P)
    ).prettyOutput)

    println(performTreeAlgorithm(
        not(A implies B) or (C iif D),
        (not(A) or not(B)) implies not(A or B),
        entails = not(C) or D,
        bruteforceMethod = false
    ).prettyOutput)

    println(performTreeAlgorithm(
        (A iif B) implies (not(C) or D),
        A implies B,
        entails = (not(C implies B) or (A and C)) implies D,
        bruteforceMethod = false
    ).prettyOutput)

    println(performTreeAlgorithm(
        (A and not(B)) or (not(B) and E),
        (A iif B) implies (not(A) implies not(E)),
        not(A and not(B)),
        entails = not(E),
        bruteforceMethod = false
    ).prettyOutput)


    println(performTreeAlgorithm(
        P iif R,
        (S or T) implies P,
        R and Q,
        not(T),
        entails = R implies not(P),
        bruteforceMethod = false
    ).prettyOutput)
}

val TruthTreeResult.prettyOutput get() = buildString {
    append(if (valid) "Valid: " else "Invalid: ")
    append(latexFormat)
}