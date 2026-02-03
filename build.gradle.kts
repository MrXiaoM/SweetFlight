plugins {
    java
    `maven-publish`
    id ("com.gradleup.shadow") version "8.3.0"
    id ("com.github.gmazzo.buildconfig") version "5.6.7"
}
buildscript {
    repositories.mavenCentral()
    dependencies.classpath("top.mrxiaom:LibrariesResolver-Gradle:1.7.5")
}
val base = top.mrxiaom.gradle.LibraryHelper(project)

group = "top.mrxiaom.sweet.flight"
version = "1.1.1"
val targetJavaVersion = 8
val pluginBaseModules = base.modules.run { listOf(library, paper, l10n) }
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
    // compileOnly("org.spigotmc:spigot:1.20") // NMS

    compileOnly("me.clip:placeholderapi:2.11.6")
    compileOnly(files("libs/Residence.jar"))
    compileOnly("org.jetbrains:annotations:24.0.0")
    compileOnly("cn.lunadeer:DominionAPI:4.7.3")

    base.library("net.kyori:adventure-api:4.22.0")
    base.library("net.kyori:adventure-platform-bukkit:4.4.0")
    base.library("net.kyori:adventure-text-minimessage:4.22.0")
    base.library("net.kyori:adventure-text-serializer-plain:4.22.0")
    base.library("com.zaxxer:HikariCP:4.0.3")

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
java {
    val javaVersion = JavaVersion.toVersion(targetJavaVersion)
    if (JavaVersion.current() < javaVersion) {
        toolchain.languageVersion.set(JavaLanguageVersion.of(targetJavaVersion))
    }
}
tasks {
    shadowJar {
        mapOf(
            "top.mrxiaom.pluginbase" to "base",
            "com.tcoded.folialib" to "folialib",
        ).forEach { (original, target) ->
            relocate(original, "$shadowGroup.$target")
        }
    }
    val copyTask = create<Copy>("copyBuildArtifact") {
        dependsOn(shadowJar)
        from(shadowJar.get().outputs)
        rename { "${project.name}-$version.jar" }
        into(rootProject.file("out"))
    }
    build {
        dependsOn(copyTask)
    }
    withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        if (targetJavaVersion >= 10 || JavaVersion.current().isJava10Compatible) {
            options.release.set(targetJavaVersion)
        }
    }
    processResources {
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
        from(sourceSets.main.get().resources.srcDirs) {
            expand(mapOf("version" to version))
            include("plugin.yml")
        }
    }
}
publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components.getByName("java"))
            groupId = project.group.toString()
            artifactId = rootProject.name
            version = project.version.toString()
        }
    }
}
