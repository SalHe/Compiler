# 编译原理

[![Java CI with Gradle](https://github.com/SalHe/Compiler/actions/workflows/test.yml/badge.svg)](https://github.com/SalHe/Compiler/actions/workflows/test.yml)

编译原理实习代码。

## 运行

1. Clone源代码并用IDEA打开运行
2. 直接运行已打包程序：[下载](https://github.com/SalHe/compiler/releases)

## 简要介绍

### 词法分析

[详细文档](./docs/Scanner.md)

### 支持Token类型

- 操作符
- 字面量
- 标识符
- 边界符
- 关键字
- 基础类型
- ...

### 特性

- 分析错误报告

### 效果预览

![效果预览](./docs/Scanner.gif)
![效果预览](./docs/Scanner2.gif)

### 语法分类

- PSG
  ![PSG](./docs/PSG.png)
- CSG
  ![CSG](./docs/CSG.png)
- CFG
  ![CFG](./docs/CFG.png)
- RG(左线性、右线性，虽然没特殊声明，但代码已做区分)
  ![RG](./docs/RG.png)

## 感受

这门课个人觉得还是挺有意思的，就是需要使劲啃。最后，Kotlin真好用。
