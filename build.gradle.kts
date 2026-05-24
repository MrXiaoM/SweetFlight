import top.mrxiaom.gradle.LibraryHelper

plugins {
    java
    `maven-publish`
    id ("com.gradleup.shadow") version "9.3.0"
    id ("com.github.gmazzo.buildconfig") version "5.6.7"
}
buildscript {
    repositories.mavenCentral()
    dependencies.classpath("top.mrxiaom:LibrariesResolver-Gradle:1.7.23")
}
val base = top.mrxiaom.gradle.LibraryHelper(project)

group = "top.mrxiaom.sweet.flight"
version = "1.1.3"
val targetJavaVersion = 8
val pluginBaseModules = base.modules.run { listOf(library, l10n) }
val shadowGroup = "top.mrxiaom.sweet.flight.libs"

repositories {
    mavenCentral()
    maven("https://repo.codemc.io/repository/maven-public/")
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://repo.helpch.at/releases/")
    maven("https://jitpack.io")
    maven("https://repo.rosewooddev.io/repository/public/")
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.20-R0.1-SNAPSHOT")
    compileOnly(base.depend.annotations)

    compileOnly("me.clip:placeholderapi:2.12.2")
    compileOnly("com.github.Zrips:Residence:6.0.0.1") { isTransitive = false }
    compileOnly("cn.lunadeer:DominionAPI:4.8.3")

    base.library(LibraryHelper.adventure("4.25.0"))
    base.library(base.depend.HikariCP)
    base.collectPluginHolders()

    implementation("com.github.technicallycoded:FoliaLib:0.4.4") { isTransitive = false }
    for (artifact in pluginBaseModules) {
        implementation(artifact)
    }
    implementation(base.resolver.lite)
}
buildConfig {
    className("BuildConstants")
    packageName("top.mrxiaom.sweet.flight")

    base.doResolveLibraries()
    buildConfigField("String", "VERSION", "\"${project.version}\"")
    buildConfigField("String[]", "RESOLVED_LIBRARIES", base.join())
}

top.mrxiaom.gradle.LibraryHelper.initJava(project, base, targetJavaVersion, true)
top.mrxiaom.gradle.LibraryHelper.initPublishing(project)

tasks {
    shadowJar {
        configurations.add(project.configurations.runtimeClasspath.get())
        mapOf(
            "top.mrxiaom.pluginbase" to "base",
            "com.tcoded.folialib" to "folialib",
        ).forEach { (original, target) ->
            relocate(original, "$shadowGroup.$target")
        }
    }
}
