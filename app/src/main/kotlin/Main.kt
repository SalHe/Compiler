package com.github.salhe.compiler.app

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.github.salhe.compiler.filterComment
import com.github.salhe.compiler.grammar.GrammarClassification
import com.github.salhe.compiler.grammar.analyseGrammar
import com.github.salhe.compiler.grammar.classifyGrammar
import com.github.salhe.compiler.scan
import com.github.salhe.compiler.token.Token
import com.github.salhe.compiler.token.scanner.ScannerException

fun main() = application {
    val state = rememberWindowState(width = 1000.dp)
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

val examples = listOf(
    "简单C程序" to """
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
                """.trimIndent(),

    "PSG" to """
                (
                    {Start,NTA, NTB,   NTC, NTD, NTE},
                    {Ta},
                    {
                        Start > NTA NTC Ta NTB,
                        NTC Ta > Ta Ta NTC,
                        NTC NTB > NTD NTB,
                        NTC NTB > NTE,
                        Ta NTD > NTD Ta,
                        Ta NTE > NTE Ta,
                        NTA NTE >
                    },
                    Start
                )
            """.trimIndent(),

    "CSG" to """
                (
                    {Start,NTB, NTC,   NTD    },
                    {Ta, Tb, Tc},
                    {
                        Start > Ta Start NTB NTC,
                        Start > Ta NTB NTC,
                        NTC NTB > NTC NTD,
                        NTC NTD > NTB NTD,
                        NTB NTD > NTB NTC,
                        Ta NTB > Ta Tb,
                        Tb NTB > Tb Tb,
                        Tb NTC > Tb Tc,
                        Tc NTC > Tc Tc
                    },
                    Start
                )
            """.trimIndent(),

    "CFG" to """
                (
                    {NTZ , NTS, NTA ,NTB, NTC    },
                    {Ta, Tb, Tc},
                    {
                        NTZ > NTS NTC,
                        NTA > Ta NTA Tc,
                        NTA > Tb NTB Tb,
                        NTB > Tb NTB,
                        NTB > ,
                        NTS > Ta NTA Tc,
                        NTC > Ta NTC Tb,
                        NTC >
                    },
                    NTZ
                )
            """.trimIndent(),

    "RG" to """
                (
                    {NTZ , NTU, NTV      },
                    {0, 1},
                    {
                        NTZ > NTU 0,
                        NTZ > NTV 1,
                        NTU > NTZ 1,
                        NTU > 1,
                        NTV > NTZ 0,
                        NTV > 0,
                    },
                    NTZ
                )
            """.trimIndent()
)

@Composable
fun Scanner() {

    var src by remember { mutableStateOf("// 尝试从上面的按钮获取示例或者自己输入吧~") }
    var analysisResult by remember { mutableStateOf(Result.success<List<Token>>(listOf())) }
    var grammarClassification by remember { mutableStateOf(Result.success<GrammarClassification?>(null)) }
    var analysing by remember { mutableStateOf(false) }
    var realTimeAnalysing by remember { mutableStateOf(true) }
    var ignoreComment by remember { mutableStateOf(false) }
    var lineSeparator by remember { mutableStateOf(false) }

    fun analyseSource() {
        analysing = true
        analysisResult = Result.success(listOf()) // 清空
        analysisResult = try {
            val tokens = src.scan(lineSeparator)
            Result.success(tokens)
        } catch (e: Exception) {
            Result.failure(e)
        }

        analysisResult
            .onSuccess { tokens ->
                grammarClassification = try {
                    Result.success(classifyGrammar(analyseGrammar(tokens)))
                } catch (e: Exception) {
                    // 暂时不需要分类处理异常
                    Result.failure(e)
                }
            }.onFailure {
                grammarClassification = Result.failure(it)
            }

        analysing = false
    }

    fun analyseSourceIfRealtime() {
        if (realTimeAnalysing) analyseSource()
    }

    analyseSourceIfRealtime()

    Row(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.weight(1f).fillMaxSize()) {

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                item { Text("示例代码：") }
                itemsIndexed(examples) { _, item ->
                    val (title, source) = item
                    Button(onClick = { src = source }) { Text(title) }
                }
            }

            Text("代码(类C语言)👇")
            TextField(src, {
                src = it
                analyseSource()
            }, modifier = Modifier.weight(1f).fillMaxWidth())
            Row {
                Button(
                    onClick = ::analyseSource, enabled = !realTimeAnalysing && !analysing
                ) {
                    Text("分析")
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // 这里应该有联合效果
                    Checkbox(realTimeAnalysing, {
                        realTimeAnalysing = it
                        analyseSourceIfRealtime()
                    })
                    Text("实时分析")
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // 这里应该有联合效果
                    Checkbox(ignoreComment, {
                        ignoreComment = it
                        analyseSourceIfRealtime()
                    })
                    Text("忽略注释")
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // 这里应该有联合效果
                    Checkbox(lineSeparator, {
                        lineSeparator = it
                        analyseSourceIfRealtime()
                    })
                    Text("包括换行符")
                }
            }
        }
        Box(modifier = Modifier.weight(1f)) {
            val listState = rememberLazyListState()

            LazyColumn(state = listState) {

                analysisResult
                    .onSuccess { tokens ->

                        grammarClassification.onSuccess {
                            if (it != null) {
                                item { Text("你可能输入的是一个文法描述，文法类型为：$it", color = Color.Blue) }
                            }
                        }

                        itemsIndexed(if (ignoreComment) tokens.filterComment() else tokens) { index, token ->
                            Text("@$index: $token")
                        }
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
