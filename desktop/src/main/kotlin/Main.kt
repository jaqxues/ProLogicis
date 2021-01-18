import androidx.compose.desktop.Window
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.AmbientClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.dp
import com.jaqxues.prologicis.parseInput
import com.jaqxues.prologicis.performTreeAlgorithm
import kotlinx.coroutines.*

fun main() = Window(title = "ProLogicis") {
    var inputState by remember { mutableStateOf("") }
    var output by remember { mutableStateOf("") }
    var clipBoardContent by remember { mutableStateOf<AnnotatedString?>(null) }
    var useBruteforce by remember { mutableStateOf(false) }
    var lastJob: Job? = null
    MaterialTheme {
        clipBoardContent?.let { AmbientClipboardManager.current.setText(it) }
        Column(
            Modifier.padding(32.dp).fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Each Sentence must be separated by a new line or ';'. The last sentence will be used as conclusion.",
                Modifier.padding(16.dp)
            )
            Row(Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Use bruteforce method, with full recursion to find optimal way")
                Checkbox(useBruteforce, { useBruteforce = it })
            }
            OutlinedTextField(inputState, { inputState = it }, modifier = Modifier.fillMaxWidth().padding(16.dp))
            Button(onClick = {
                val parts = inputState.split("[;\n]+".toRegex())
                if (parts.size < 2) {
                    output = "Input needs to be at least 2 sentences"
                    return@Button
                }
                val sentences = try {
                    parts.map(::parseInput)
                } catch (t: Throwable) {
                    output = "A sentence could not be parsed" + t
                    return@Button
                }
                if (useBruteforce)
                    output = "If it loads for too long, you might have to disable bruteforce"
                lastJob?.cancel()
                lastJob = GlobalScope.launch(Dispatchers.Default) {
                    output = performTreeAlgorithm(
                        *sentences.dropLast(1).toTypedArray(),
                        entails = sentences.last(),
                        bruteforceMethod = useBruteforce
                    ).latexFormat
                    lastJob = null
                }
            }, Modifier.padding(16.dp)) {
                Text("Run Tree Algorithm")
            }
            Text(output, Modifier.padding(16.dp))
            Button(onClick = {
                clipBoardContent = buildAnnotatedString { append(output) }
            }, Modifier.padding(16.dp)) {
                Text("Copy Output")
            }
        }
    }
}