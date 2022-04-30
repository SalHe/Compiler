# 词法分析器

## 语言形式化描述

> 本形式化描述主要采用上下文无关文法，且以更清晰表达语义为准，所以可能会存在一些中间的非终结符。事实上，这些非终结符对于词法分析器的形式描述可能用不上，但是对于语法分析器的形式描述可能是有用的，所以他们并不是完全无用。

Token串描述：

$$
\begin{aligned}
<TokenStream> &\rightarrow <Token> <Empty> <TokenStream> | \epsilon \\
<Token> &\rightarrow <Identifier> | <Literal> | <Operator> | <Punctuation> | <Keyword> \\
<Empty> &\rightarrow 空格等空白字符 \\
\\
<Digit> &\rightarrow 0 | <NonZeroDigit> \\
<NonZeroDigit> &\rightarrow 1 | 2 | 3 | 4 | 5 | 6 | 7 | 8 | 9 \\
\\
<Identifier> &\rightarrow <IdentifierStart> <IdentifierTrail>\\
<IdentifierStart> &\rightarrow (\_|A|B|...|Z|a|b|...|z) \\
<IdentifierAllChar> &\rightarrow <IdentifierStart> | <Digit> \\
<IdentifierTrail> &\rightarrow <IdentifierAllChar> <IdentifierTrail> | \epsilon \\
\\
<Operator> &\rightarrow + | - | * | / | = \\
<Punctuation> &\rightarrow      \space (     \space
                              | \space )     \space
                              | \space [     \space
                              | \space ]     \space
                              | \space \{    \space
                              | \space \}    \space
                              | \space ;    \space
                              \\
<Keyword> &\rightarrow if | else | while | for | do | <PrimitiveType> \\
<PrimitiveType> &\rightarrow int | float | double | char | bool \\
\\
<Literal> &\rightarrow <NumberLiteral> | <StringLiteral> \\
\\
<NumberLiteral> &\rightarrow <IntegerLiteral> \\
<IntegerLiteral> &\rightarrow <NonZeroDigit> <IntegerLiterTrail> | <Digit> \\
<IntegerLiteralTrail> &\rightarrow <Digit> <IntegerLiteralTrail> | \epsilon \\
\\
<StringLiteral> &\rightarrow `` <StringLiteralContent> " (注：这里的引号为英文状态下的引号)\\
<StringLiteralContent> &\rightarrow <StringLiteralChar> <StringLiteralContent> | \epsilon \\
<StringLiteralChar> &\rightarrow 任意非英文引号、特殊控制符的字符 \\
\\
\end{aligned}
$$

## 单词编码表

| Token              | 编码 | 说明         |
| ------------------ | ---- | ------------ |
| \<Identifier\>     | 0    | 标识符       |
| \<IntegerLiteral\> | 1    | 整数字面量   |
| \<StringLiteral\>  | 2    | 字符串字面量 |
| \<Operator\>       | 3    | 操作符       |
| \<Punctuation\>    | 4    | 界限符       |
| \<Keyword\>        | 5    | 关键字       |
| \<PrimitiveType\>  | 6    | 基本类型     |

## 状态转换图

![状态转换图](./TokenScannerStatus.svg)

图中识别状态机的状态转移连线上为正则表达式，表示状态转移时可以接收的单个字符。

由于界限符识别较为简单，此处并没有单独画出他的子状态机。

而“确认Token并存储”子状态机为一个中间层，为了更清晰的表明状态机行为而存在，实际上可以不存在。如果状态可达“需要回退一个字符”，则其从识别状态机出发的边相当于从“识别”状态出发。比如“数字识别”中接收到了字符`"`(双引号)，因为其下一步会到达“需要回退一个字”，所以可以直接等价于从“识别”状态出发，而识别状态接收到`"`会进入识别字符串字面量，所以此时可以直接等价于从“识别字符串字面量”状态出发。

## 词法分析算法

词法分析算法是一个状态机，它的输入是一个字符串，输出是一个单词列表。

执行过程与[状态转换图](#状态转换图)中的过程一致。

该过程中需要由一个变量`word`暂存识别的部分字符串。

1. 初始化状态机，标识当前状态为“空闲”状态。
2. 如果当前为“空闲”状态，读入下一个字符：
   1. 如果读入的是界限符中的字符，则将读入的字符找到对应的Token(此Token应为单例，比如分号`;`)，并将Token存入Token列表，然后跳转到第`1`步。
   2. 如果读入的是下划线、英文字母，标记当前为“识别词语”状态，并将读入字符存入`word`。
   3. 如果读入的是数字，标记当前为“识别数字”状态，并将读入的字符存入`word`。
   4. 如果读入的是等号，则将等号Token存入Token列表。
   5. 如果读入的是+、-、*、\，标记当前为“识别操作符”状态，并将读入的字符存入`word`。
   6. 否则报错。
3. 如果当前为“识别词语”状态，读入下一个字符：
   1. 如果读入的是下划线、英文字母、数字，则追加在`word`中。
   2. 其他情况，则
      1. 若`word`与关键字(如if)拼写一致，则识别为一个关键字，将关键字对应的Token存入Token列表，将当前状态设为“空闲”状态。
      2. 若`word`与基本类型(如int)拼写一致，则识别为一个基本类型，将关键字对应的Token存入Token列表，将当前状态设为“空闲”状态。
      3. 否则将`word`识别为一个标识符，将标识符对应的Token存入Token列表，将当前状态设为“空闲”状态。
      4. 回退一个字符。
4. 如果当前为“识别数字”状态，读入下一个字符：
   1. 如果读入的是数字，则追加在`word`中继续读入，跳转到第`4`步。
   2. 其他情况，则将识别的数字作为整型Token(只考虑了整数字面量)存入Token列表，将当前状态设为“空闲”状态。回退一个字符。
5. 如果当前为“识别操作符”状态，读入下一个字符：
   1. 如果读入的是=、+、-、*、\，则追加在`word`中继续读入。
   2. 其他情况，则判断`word`是否为某一运算符(比如+、++、+=)，如果不是则报错；如果是则存储对应的Token，将当前状态标记为“空闲”。回退一个字符。
6. 如果读入`<EOF>`即文件结束，终止识别。


## 测试计划