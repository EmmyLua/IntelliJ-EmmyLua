/*
 * Copyright (c) 2017. tangzx(love.tangzx@qq.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.tools.ant.taskdefs.condition.Os
import java.io.ByteArrayOutputStream
import de.undercouch.gradle.tasks.download.*

plugins {
    id("org.jetbrains.intellij").version("0.6.5")
    id("org.jetbrains.kotlin.jvm").version("1.4.20")
    id("de.undercouch.download").version("3.4.3")
}

data class BuildData(
    val ideaSDKVersion: String,
    val sinceBuild: String,
    val untilBuild: String,
    val archiveName: String,
    val targetCompatibilityLevel: JavaVersion,
    val explicitJavaDependency: Boolean = true,
    val instrumentCodeCompilerVersion: String? = null
)

val buildDataList = mapOf(
    "203" to BuildData(
        ideaSDKVersion = "IC-211.5787.15",
        sinceBuild = "203",
        untilBuild = "211.*",
        archiveName = "IntelliJ-EmmyLua",
        targetCompatibilityLevel = JavaVersion.VERSION_11,
        explicitJavaDependency = true,
        instrumentCodeCompilerVersion = "203.7148.70"
    ),
    "202" to BuildData(
        ideaSDKVersion = "IC-202.6397.94",
        sinceBuild = "202",
        untilBuild = "202.*",
        archiveName = "IntelliJ-EmmyLua",
        targetCompatibilityLevel = JavaVersion.VERSION_1_8
    ),
    "201" to BuildData(
        ideaSDKVersion = "IC-201.8743.12",
        sinceBuild = "201",
        untilBuild = "201.*",
        archiveName = "IntelliJ-EmmyLua",
        targetCompatibilityLevel = JavaVersion.VERSION_1_8
    ),
    "193" to BuildData(
        ideaSDKVersion = "IC-193.5233.102",
        sinceBuild = "193",
        untilBuild = "194.*",
        archiveName = "IntelliJ-EmmyLua",
        targetCompatibilityLevel = JavaVersion.VERSION_1_8
    ),
    "182" to BuildData(
        ideaSDKVersion = "IC-182.2371.4",
        sinceBuild = "182",
        untilBuild = "193.*",
        archiveName = "IntelliJ-EmmyLua",
        explicitJavaDependency = false,
        targetCompatibilityLevel = JavaVersion.VERSION_1_8
    ),
    "172" to BuildData(
        ideaSDKVersion = "IC-172.4574.19",
        sinceBuild = "172",
        untilBuild = "181.*",
        archiveName = "IntelliJ-EmmyLua",
        explicitJavaDependency = false,
        targetCompatibilityLevel = JavaVersion.VERSION_1_8
    ),
    "171" to BuildData(
        ideaSDKVersion = "IC-171.4694.73",
        sinceBuild = "171",
        untilBuild = "171.*",
        archiveName = "IntelliJ-EmmyLua",
        explicitJavaDependency = false,
        targetCompatibilityLevel = JavaVersion.VERSION_1_8
    )
)

val developVersion = "203"

val emmyDebuggerVersion = "1.0.16"

val resDir = "src/main/resources"

val isWin = Os.isFamily(Os.FAMILY_WINDOWS)

val isCI = System.getenv("APPVEYOR") != null

// CI
if (isCI) {
    version =
        System.getenv("APPVEYOR_REPO_TAG_NAME") ?:
        System.getenv("APPVEYOR_BUILD_VERSION")
    exec {
        executable = "git"
        args("config", "--global", "user.email", "love.tangzx@qq.com")
    }
    exec {
        executable = "git"
        args("config", "--global", "user.name", "tangzx")
    }
}

fun getRev(): String {
    val os = ByteArrayOutputStream()
    exec {
        executable = "git"
        args("rev-parse", "HEAD")
        standardOutput = os
    }
    return os.toString().substring(0, 7)
}

task("downloadEmmyDebugger", type = Download::class) {
    src(arrayOf(
        "https://github.com/EmmyLua/EmmyLuaDebugger/releases/download/${emmyDebuggerVersion}/emmy_core.so",
        "https://github.com/EmmyLua/EmmyLuaDebugger/releases/download/${emmyDebuggerVersion}/emmy_core.dylib",
        "https://github.com/EmmyLua/EmmyLuaDebugger/releases/download/${emmyDebuggerVersion}/emmy_core@x64.zip",
        "https://github.com/EmmyLua/EmmyLuaDebugger/releases/download/${emmyDebuggerVersion}/emmy_core@x86.zip"
    ))

    dest("temp")
}

task("unzipEmmyDebugger", type = Copy::class) {
    dependsOn("downloadEmmyDebugger")
    from(zipTree("temp/emmy_core@x64.zip")) {
        into("x64")
    }
    from(zipTree("temp/emmy_core@x86.zip")) {
        into("x86")
    }
    destinationDir = file("temp")
}

task("installEmmyDebugger", type = Copy::class) {
    dependsOn("unzipEmmyDebugger")
    from("temp/x64/") {
        include("emmy_core.dll")
        into("debugger/emmy/windows/x64")
    }
    from("temp/x86/") {
        include("emmy_core.dll")
        into("debugger/emmy/windows/x86")
    }
    from("temp") {
        include("emmy_core.so")
        into("debugger/emmy/linux")
    }
    from("temp") {
        include("emmy_core.dylib")
        into("debugger/emmy/mac")
    }
    destinationDir = file("src/main/resources")
}

fun setupVersion(ver: String) {
    val versionData = buildDataList[ver]!!

    configure<JavaPluginConvention> {
        sourceCompatibility = versionData.targetCompatibilityLevel
        targetCompatibility = versionData.targetCompatibilityLevel
    }

    tasks {
        buildPlugin {
            //dependsOn("installEmmyDebugger")
            archiveBaseName.set(versionData.archiveName)
            from(fileTree(resDir) { include("debugger/**") }) {
                into("/${project.name}/classes/")
            }
            from(fileTree(resDir) { include("!!DONT_UNZIP_ME!!.txt") }) {
                into("/${project.name}")
            }
        }

        compileKotlin {
            kotlinOptions {
                jvmTarget = "1.8"
            }
        }

        patchPluginXml {
            setSinceBuild(versionData.sinceBuild)
            setUntilBuild(versionData.untilBuild)
        }

        versionData.instrumentCodeCompilerVersion?.let {
            instrumentCode {
                setCompilerVersion(it)
            }
        }
    }

    intellij {
        type = "IC"
        updateSinceUntilBuild = false
        downloadSources = false
        version = versionData.ideaSDKVersion
        localPath = System.getenv("IDEA_HOME_${ver}")
        sandboxDirectory = "${project.buildDir}/${ver}/idea-sandbox"
    }
}

project(":") {
    repositories {
        maven(url = "https://www.jetbrains.com/intellij-repository/releases")
        mavenCentral()
        jcenter()
    }

    dependencies {
        implementation(fileTree(baseDir = "libs") { include("*.jar") })
        implementation("com.google.code.gson:gson:2.8.6")
        implementation("org.scala-sbt.ipcsocket:ipcsocket:1.3.0")
        implementation("org.luaj:luaj-jse:3.0.1")
        implementation("org.eclipse.mylyn.github:org.eclipse.egit.github.core:2.1.5")
    }

    sourceSets {
        main {
            java.srcDirs("gen", "src/main/compat")
            resources.exclude("debugger/**")
        }
    }

    setupVersion(developVersion)
}

buildDataList.forEach {
    val ver = it.key

    task("buildPluginWithBunch${ver}") {
        finalizedBy("buildPlugin")
        doLast {
            // reset
            exec {
                executable = "git"
                args("reset", "HEAD", "--hard")
            }
            // clean untracked files
            exec {
                executable = "git"
                args("clean", "-d", "-f")
            }
            // switch
            exec {
                executable = if (isWin) "bunch/bin/bunch.bat" else "bunch/bin/bunch"
                args("switch", ".", ver)
            }
            // reset to HEAD
            exec {
                executable = "git"
                args("reset", getRev())
            }
        }
    }

    task("build_${ver}") {
        finalizedBy(if (isCI) "buildPluginWithBunch${ver}" else "buildPlugin")
        doLast {
            setupVersion(ver)
        }
    }
}