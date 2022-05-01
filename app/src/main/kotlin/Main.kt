package com.github.salhe.compiler.app

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.github.salhe.compiler.scan
import com.github.salhe.compiler.token.Token
import com.github.salhe.compiler.token.scanner.ScannerException

fun main() = application {
    val state = rememberWindowState()
    Window(
        onCloseRequest = ::exitApplication,
        title = "Compiler",
        state = state
    ) {
        MaterialTheme {
            Row {
                Scanner()
            }
        }
    }

}

@Composable
fun Scanner() {
    var src by remember {
        mutableStateOf(
            """
        void main(){
            int a = 5;
            int b = 5 + 10 * 3;
            print("Hello!");
            if(a==5){
                print("a == 5");
            }
            else if(a < 5 )    {
                print("a<5");
            }
            while(!b){
                print("b!=0");
                b--;
            }
            if(a!=b){
                print("a!=b");
            }
            a++;
        }
    """.trimIndent()
        )
    }
    var analysisResult by remember { mutableStateOf(Result.success<List<Token>>(listOf())) }
    var analysing by remember { mutableStateOf(false) }
    var lineSeparator by remember { mutableStateOf(false) }
    Row(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.weight(1f).fillMaxSize()) {
            Text("源代码(类C语言)：")
            TextField(src, { src = it }, modifier = Modifier.weight(1f).fillMaxWidth())
            Row {
                Button(
                    onClick = {
                        analysisResult = try {
                            analysing = true
                            val tokens = src.scan(lineSeparator)
                            Result.success(tokens)
                        } catch (e: Exception) {
                            Result.failure(e)
                        } finally {
                            analysing = false
                        }
                    }, enabled = !analysing
                ) {
                    Text("分析")
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // 这里应该有联合效果
                    Checkbox(lineSeparator, { lineSeparator = it })
                    Text("包括换行符")
                }
            }
        }
        Box(modifier = Modifier.weight(1f)) {
            val listState = rememberLazyListState()

            LazyColumn(state = listState) {
                analysisResult
                    .onSuccess {
                        itemsIndexed(it) { index, token -> Text("@$index: $token") }
                        item { Text("到底咯~~~") }
                    }.onFailure { exception ->
                        if (exception is ScannerException) {
                            item {
                                Text(
                                    "(${exception.row},${exception.col}): ${exception.message}",
                                    color = Color.Red
                                )
                            }
                        } else {
                            item { Text("未知错误：\n$exception", color = Color.Red) }
                        }
                    }
            }

            VerticalScrollbar(
                modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
                adapter = rememberScrollbarAdapter(listState)
            )
        }
    }
}