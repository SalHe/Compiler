package com.github.salhe.compiler.test.grammar

import com.github.salhe.compiler.grammar.GrammarClassification
import com.github.salhe.compiler.grammar.analyseGrammar
import com.github.salhe.compiler.grammar.classifyGrammar
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class GrammarClassifyTest {

    @Test
    fun classifyTest() {

        // 请不要使用特殊语素作为”字母“，比如"void"等。。。
        // 由于需要支持单词，所以需要用空格分隔不同符号

        val testCases = mapOf(

            // 对应PPT：第2章 形式语言概论.pdf，第27页
            """
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
            """.trimIndent() to GrammarClassification.PSG,

            // 对应PPT：第2章 形式语言概论.pdf，第28页
            """
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
            """.trimIndent() to GrammarClassification.CFG,

            // 对应PPT：第2章 形式语言概论.pdf，第29页
            """
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
            """.trimIndent() to GrammarClassification.CSG,

            // 对应PPT：第2章 形式语言概论.pdf，第30页
            """
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
            """.trimIndent() to GrammarClassification.RG,


            )

        testCases.forEach { (grammarDescription, classification) ->
            val grammar = analyseGrammar(grammarDescription)
            val recognizedClassification = classifyGrammar(grammar)
            Assertions.assertEquals(
                classification,
                recognizedClassification,
                "文法：${grammarDescription.replace(" ", "")}应该为${classification}型文法，但此处却被识别为${recognizedClassification}"
            )
        }
    }
}