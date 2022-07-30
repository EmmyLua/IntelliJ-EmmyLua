![logo](/snapshot/logo.png)
# EmmyLua for IntelliJ IDEA

[![Build status](https://github.com/EmmyLua/IntelliJ-EmmyLua/actions/workflows/build.yml/badge.svg?branch=master)](https://github.com/EmmyLua/IntelliJ-EmmyLua/actions/workflows/build.yml)
[![Online EmmyLua Doc](https://img.shields.io/badge/emmy-doc-46BC99.svg?style=flat-square)](https://emmylua.github.io)
[![在线文档](https://img.shields.io/badge/emmy-中文-46BC99.svg?style=flat-square)](https://emmylua.github.io/zh_CN)
[![Jetbrains plugin](https://img.shields.io/jetbrains/plugin/d/9768-emmylua.svg?style=flat-square)](https://plugins.jetbrains.com/plugin/9768-emmylua)
[![Jetbrains plugin](https://img.shields.io/jetbrains/plugin/v/9768-emmylua.svg?style=flat-square)](https://plugins.jetbrains.com/plugin/9768-emmylua)
[![donate](https://img.shields.io/badge/donate-emmy-FF69B4.svg?style=flat-square)](https://emmylua.github.io/donate.html)
[![Join the chat at gitter](https://img.shields.io/badge/chat-on%20gitter-46BC99.svg?style=flat-square)](https://gitter.im/emmylua/Lobby?utm_source=share-link&utm_medium=link&utm_campaign=share-link)
[![加入QQ群](https://img.shields.io/badge/chat-QQ(1)群-46BC99.svg?style=flat-square)](https://jq.qq.com/?_wv=1027&k=5Br83i5)
[![加入QQ群](https://img.shields.io/badge/chat-QQ(2)群-46BC99.svg?style=flat-square)](https://jq.qq.com/?_wv=1027&k=5EeI0Sm)

QQ交流群：1群：`29850775` 2群：`529914962`
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

## Road map
- [ ] Attach debugger rewriting
- [x] Remote debugger rewriting
- [ ] Data follow inspections
- [ ] Ctrl follow inspections
- [ ] `@interface` support
- [ ] API Hub

## Build environment requirements

- JDK 11
- Setup environment variables as following:

        JAVA_HOME="path to JDK 11"
        IDEA_HOME_203="path to IDEA 203+"
        
## Building

  `./gradlew buildPlugin -DIDEA_VER=203`

## Developed By

[**@tangzx** 阿唐](https://github.com/tangzx)

**Contributors**
- [**@dsmgit** dsmgit](https://github.com/dsmgit)
- [**@Perryvw** Perry van Wesel](https://github.com/Perryvw)
- [**@ice1000** Tesla Ice Zhang](https://github.com/ice1000)
- [**@mqualizz** Marco Qualizza](https://github.com/mqualizz)
- [**@jb574** jb574](https://github.com/jb574)
- [**@luchuanbaker** luchuanbaker](https://github.com/luchuanbaker)
- [**@LiamYao** Yao](https://github.com/LiamYao)
- [**@Dauch** Tyler Dauch](https://github.com/Dauch)
- [**@Christopher-St** Christopher-St](https://github.com/Christopher-St)
- [**@taigacon** taigacon](https://github.com/taigacon)
- [**@KKKIIO** KKKIIO](https://github.com/KKKIIO)
- [**@zj6882917** zj6882917](https://github.com/zj6882917)
- [**@fangfang1984** fangfang1984](https://github.com/fangfang1984)
