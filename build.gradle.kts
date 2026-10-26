plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "2.3.20"
    id("org.jetbrains.intellij.platform")
}

group = "com.github.pet"
version = "1.2.0"

repositories {
    mavenCentral()
    intellijPlatform { defaultRepositories() }
}

dependencies {
    intellijPlatform {
        local("C:/Program Files/JetBrains/PyCharm 2026.2.3")
        pluginVerifier()
    }
}

intellijPlatform {
    buildSearchableOptions = false
    pluginVerification {
        ides { local(file("C:/Program Files/JetBrains/PyCharm 2026.2.3")) }
    }
    pluginConfiguration {
        id = "com.github.pet.pycharm-pet"
        name = "Status Bar Pet"
        version = project.version.toString()
        vendor {
            name = "Darshan Bharat"
            email = "contact@darshan-bharat.com"
            url = "https://darshan-bharat.com"
        }
        ideaVersion {
            sinceBuild = "242"
            untilBuild = provider { null }
        }
    }
}

tasks.withType<JavaCompile> {
    sourceCompatibility = "21"
    targetCompatibility = "21"
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
        jvmDefault.set(org.jetbrains.kotlin.gradle.dsl.JvmDefaultMode.NO_COMPATIBILITY)
    }
}

tasks.register<JavaExec>("releaseChecks") {
    group = "verification"
    description = "Runs regression checks and offscreen rendering benchmarks in an isolated JVM."
    dependsOn(tasks.testClasses)
    classpath = sourceSets.test.get().runtimeClasspath + sourceSets.main.get().compileClasspath
    mainClass.set("com.github.pet.pycharm.ReleaseChecks")
    systemProperty("java.awt.headless", "true")
    jvmArgs("--add-opens=java.desktop/javax.swing=ALL-UNNAMED",
        "--add-exports=java.desktop/sun.swing=ALL-UNNAMED", "--add-exports=java.desktop/sun.awt=ALL-UNNAMED")
}

// The regression runners use main methods and no external test framework.
tasks.test {
    exclude("**/ReleaseChecks*.class", "**/RenderBenchmark*.class")
}
tasks.check { dependsOn("releaseChecks") }
