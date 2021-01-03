package com.jaqxues.prologicis

import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.HTMLInputElement

/**
 * This file was created by Jacques Hoffmann (jaqxues) in the Project ProLogicis.<br>
 * Date: 04.12.20 - Time 19:58.
 */
fun main() {
    window.onload = {
        console.log(document)
        console.log(document.getElementById("form_formula"))
        document.getElementById("form_formula")!!.addEventListener("submit", { evt ->
            evt.preventDefault()
            val latexFormat = try {
                val value = (document.getElementById("formula")!! as HTMLInputElement).value
                val sentences = value.split(";").map { parseInput(it.trim()) }
                performTreeAlgorithm(*sentences.take(sentences.size).toTypedArray(), entails = sentences.last()).latexFormat
            } catch (t: Throwable) {
                "Error Parsing or Processing"
            }
            document.getElementById("tt_out")!!.innerHTML = latexFormat
        })
    }
}