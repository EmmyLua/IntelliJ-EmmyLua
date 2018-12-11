# EmmyLua for IntelliJ IDEA

[![Online EmmyLua Doc](https://img.shields.io/badge/emmy-doc-46BC99.svg?style=flat-square)](https://emmylua.github.io)
[![Jetbrains plugin](https://img.shields.io/jetbrains/plugin/d/9768-emmylua.svg?style=flat-square)](https://plugins.jetbrains.com/plugin/9768-emmylua)
[![Jetbrains plugin](https://img.shields.io/jetbrains/plugin/v/9768-emmylua.svg?style=flat-square)](https://plugins.jetbrains.com/plugin/9768-emmylua)
[![donate](https://img.shields.io/badge/donate-emmy-FF69B4.svg?style=flat-square)](https://emmylua.github.io/donate.html)
[![Join the chat at gitter](https://img.shields.io/badge/chat-on%20gitter-46BC99.svg?style=flat-square)](https://gitter.im/emmylua/Lobby?utm_source=share-link&utm_medium=link&utm_campaign=share-link)
[![加入QQ群](https://img.shields.io/badge/chat-QQ群-46BC99.svg?style=flat-square)](//shang.qq.com/wpa/qunwpa?idkey=f1acce081c45fbb5670ed5f880f7578df7a8b84caa5d2acec230ac957f0c1716)

Version | CI status
--------|----------
IDEA 171|[![Build status](https://ci.appveyor.com/api/projects/status/m25uajpqa2fft8ah?svg=true)](https://ci.appveyor.com/project/EmmyLua/intellij-emmylua)
IDEA 172-181|[![Build status](https://ci.appveyor.com/api/projects/status/m25uajpqa2fft8ah?svg=true)](https://ci.appveyor.com/project/EmmyLua/intellij-emmylua)
IDEA 182|[![Build status](https://ci.appveyor.com/api/projects/status/xvahlg1ceiy16dxl?svg=true)](https://ci.appveyor.com/project/EmmyLua/intellij-emmylua-7n83m)

QQ交流群：`29850775`
(最新版本以及部分视频演示在群文件中下载)

![snapshot](/snapshot/overview.gif)


## Find usages
![find_usages](/snapshot/find_usages.gif)

## Rename
![rename](/snapshot/rename.gif)

## Parameter hints
![param_hints](/snapshot/param_hints.png)
![param_hints_cfg](/snapshot/param_hints_cfg.png)

## Go to symbol
![go_to_symbol](/snapshot/go_to_symbol.gif)

## Go to class
![go_to_class](/snapshot/go_to_class.gif)

## Quick Documentation(Ctrl + Q)
![quick_documentation](/snapshot/quick_documentation.gif)

## Method separators
![method_separators_cfg](/snapshot/method_separators_cfg.png)
![method_separators](/snapshot/method_separators.png)

## Method override line marker
![method_override_line_marker](/snapshot/method_override_line_marker.gif)

## Features
| feature                              | progress |
| ------------------------------------ | :------: |
| Syntax highlighting                  |    ✔     |
| Highlighting global                  |    ✔     |
| Highlighting local/param             |    ✔     |
| Highlighting up value                |    ✔     |
| Lua 5.3 support                      |    ✔     |
| Find usages                          |    ✔     |
| Rename(Shift + F6)                   |    ✔     |
| Go to definition(Ctrl + Mouse)       |    ✔     |
| Go to symbol(Ctrl + Alt + Shift + N) |    ✔     |
| Go to class(Ctrl + N)                |    ✔     |
| Go to file(Ctrl + Shift + N)         |    ✔     |
| Parameter name hints                 |    ✔     |
| Keyword completion                   |    ✔     |
| Basic completion                     |    ✔     |
| Structure view                       |    ✔     |
| Brace Matching                       |    ✔     |
| Comment in/out                       |    ✔     |
| Color settings page                  |    ✔     |
| Comment based type/class annotation  |    ✔     |
| Method override line marker          |    ✔     |
| Name suggestion for refactor         |    ✔     |
| Quick Documentation(Ctrl + Q)        |    ✔     |
| Live templates                       |    ✔     |
| Postfix completion templates         |    14    |
| Code formatter                       |    ✔     |
| Code intentions                      |    9     |
| Code inspections                     |    7     |
| Lua Standard Library/API             |    ✔     |
| Region folding                       |    ✔     |
| Attach Debugger                      |    ✔     |
| Remote Debugger                      |    ✔     |
| Lua Check                            |    ✔     |
| Embed Remote Debugger                |    0%    |
| Lua Profiler                         |    ✔     |
| ... .etc                             |          |

## Build environment requirements

- Visual Studio 2015
- JDK 1.8
- Setup environment variables as following:

        JAVA_HOME="path to JDK 1.8"
        IDEA_HOME="path to IDEA 172+"
        
## Building

  `./gradlew buildPlugin`

## Developed By

[**@tangzx** 阿唐](https://github.com/tangzx)

**Contributors**
- [**@dsmgit** dsmgit](https://github.com/dsmgit)
- [**@Perryvw** Perry van Wesel](https://github.com/Perryvw)
- [**@mqualizz** Marco Qualizza](https://github.com/mqualizz)
- [**@taigacon** taigacon](https://github.com/taigacon)
- [**@ice1000** Tesla Ice Zhang](https://github.com/ice1000)
- [**@LiamYao** Yao](https://github.com/LiamYao)
- [**@luchuanbaker** luchuanbaker](https://github.com/luchuanbaker)
- [**@Dauch** Tyler Dauch](https://github.com/Dauch)
- [**@jb574** jb574](https://github.com/jb574)
- [**@KKKIIO** KKKIIO](https://github.com/KKKIIO)
- [**@zj6882917** zj6882917](https://github.com/zj6882917)
