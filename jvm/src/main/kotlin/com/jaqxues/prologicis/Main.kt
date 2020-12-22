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



    val s1 = (not(A implies B)) or (C iif D)
    val s2 = (not(A) or not(B)) implies not(A or B)
    val conclusion = not(C) or D
//    val s1 = A or B
//    val s2 = D and C
//    val conclusion = A implies B


    performTreeAlgorithm(s1, s2, entails = conclusion)
}