package com.jaqxues.prologicis

import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.*
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.HTMLSelectElement
import org.w3c.dom.HTMLTextAreaElement

/**
 * This file was created by Jacques Hoffmann (jaqxues) in the Project ProLogicis.<br>
 * Date: 04.12.20 - Time 19:58.
 */
fun main() {
    window.onload = {
        var job: Job? = null
        document.getElementById("form_formula")!!.addEventListener("submit", { evt ->
            evt.preventDefault()
            try {
                job?.cancel()
                val inContent = (document.getElementById("formula")!! as HTMLTextAreaElement).value
                val bruteforce = (document.getElementById("bruteforce")!! as HTMLInputElement).checked
                val outputFormat = (document.getElementById("output_type")!! as HTMLSelectElement).value
                val sentences = inContent.trim().split("[;\n]+".toRegex()).map { parseInput(it.trim()) }

                if (bruteforce) {
                    htmlOutput = "Processing with Brute force... Disable Brute force if it takes too long."
                }
                check(sentences.size > 1) { "You need to input at least 2 sentences" }

                job = GlobalScope.launch(Dispatchers.Default) {
                    if (bruteforce) // Wait 5 milliseconds to allow output to update
                        delay(5)
                    val output = try {
                        performTreeAlgorithm(
                            *sentences.take(sentences.size - 1).toTypedArray(),
                            entails = sentences.last(),
                            bruteforceMethod = bruteforce
                        ).let {
                            useGraphvizDotOut(it.digraphVizDotFormat)
                            when (outputFormat) {
                                "latex" -> it.latexFormat
                                "graph" -> it.graphVizDotFormat
                                "digraph" -> it.digraphVizDotFormat
                                "none" -> "Success, updating Graph"
                                else -> throw IllegalArgumentException("Selected unknown output format $outputFormat")
                            }
                        }
                    } catch (t: Throwable) {
                        t.printStackTrace()
                        "Error processing sentences"
                    }
                    withContext(Dispatchers.Main) {
                        htmlOutput = output
                    }
                    job = null
                }
            } catch (t: Throwable) {
                t.printStackTrace()
                htmlOutput = "Error initializing and parsing given sentences. Check Console for errors"
            }
        })
    }
}

var htmlOutput: String = ""
    set(new) {
        field = new
        document.getElementById("tt_out")!!.innerHTML = new
    }

external fun useGraphvizDotOut(output: String)