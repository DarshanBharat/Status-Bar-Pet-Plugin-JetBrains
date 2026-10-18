plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "2.0.21"
    id("org.jetbrains.intellij.platform")
}

group = "com.github.pet"
version = "1.0.0"

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    intellijPlatform {
        local("C:/Program Files/JetBrains/PyCharm 2026.2.3")
    }
}

intellijPlatform {
    pluginConfiguration {
        id = "com.github.pet.pycharm-pet"
        name = "Status Bar Pet"
        version = "1.0.0"
        description = """
            <p>
                Status Bar Pet is a lightweight, non-intrusive animated pet companion living directly in your JetBrains status bar. 
                Gamify your coding sessions, track Pomodoro focus sprints, and relieve programming stress with tactile mouse petting while keeping your editor clean and distraction-free.
            </p>

            <h3>Why Developers Love Status Bar Pet</h3>
            <ul>
                <li><b>Zero Distraction and 100% Silent:</b> No unexpected chimes, beeps, or audio interruptions during meetings or deep focus. Operates strictly within the bottom status bar without obscuring your code.</li>
                <li><b>Pure Vector Rendering:</b> Handcrafted Java2D vector graphics that look ultra-sharp on HiDPI and Apple Retina displays with zero pixel blur.</li>
                <li><b>Zero CPU Overhead:</b> Includes an instant <i>"Freeze Movement"</i> mode for 0% CPU consumption when you need absolute stillness, and an automatic sleep mode when you're away.</li>
            </ul>

            <h3>Key Features</h3>
            <ul>
                <li><b>Tactile Mouse Petting (Zero-Click Anti-Stress):</b> Simply move and gently rub your mouse cursor over the pet in the status bar to trigger soothing purring vibrations and floating hearts without clicking.</li>
                <li><b>Real-Time Typing Synchronization:</b> The pet types along on its mini laptop with your keystrokes, reacts to deletions, and stamps a green <code>[✓ SAVED]</code> badge on <code>Ctrl+S</code> / <code>Cmd+S</code>.</li>
                <li><b>Flow-State Coding XP and Progression:</b> Earn XP passively by writing code and staying in flow state, climbing through 9 developer career ranks.</li>
                <li><b>8 Unlockable Vector Accessories:</b> Equip and customize Dapper Bowtie, Coding Headphones, Hacker Glasses, Wizard Hat, Pirate Captain Hat, Golden Crown, Angelic Halo, and Viking Horns.</li>
                <li><b>Built-in 25-Minute Pomodoro Focus Timer:</b> 1-click focus sprints with remaining countdown in the status bar and break reminders.</li>
                <li><b>Inactivity Sleep:</b> Automatically curls up with gentle drifting Zzz particles after 35s of idle time.</li>
            </ul>

            <h3>Choose Your Mascot</h3>
            <ul>
                <li><b>Mochi the Cat:</b> Relaxed, purrs on clean code, and sleeps peacefully during long refactoring sessions.</li>
                <li><b>Sparky the Corgi:</b> High-energy developer pup who wags his tail at every commit and green test suite.</li>
                <li><b>Quackers the Rubber Duck:</b> The iconic symbol of rubber duck debugging.</li>
            </ul>

            <h3>Quick Controls</h3>
            <ul>
                <li><b>Hover and Rub Mouse:</b> Tactile petting with purring vibration and hearts.</li>
                <li><b>Left Click:</b> Inspect level, current XP progress, and Pomodoro timer.</li>
                <li><b>Right Click:</b> Mascot switcher, accessory closet, Pomodoro toggle, and freeze mode.</li>
            </ul>
        """.trimIndent()
        changeNotes = """
            <h4>v1.0.0</h4>
            <ul>
                <li>Initial release of Status Bar Pet.</li>
                <li>Mascots included: Cat (Mochi), Dog (Sparky), and Rubber Duck (Quackers).</li>
                <li>Interactive mouse hover petting with vibration and floating hearts.</li>
                <li>Continuous XP progression from typing and writing code.</li>
                <li>Unlockable accessories across 9 developer levels.</li>
                <li>Integrated 25-minute Pomodoro focus timer.</li>
                <li>Pause/Freeze movement toggle and idle sleep mode.</li>
                <li>Clean, silent vector graphics with zero sound effects.</li>
            </ul>
        """.trimIndent()
        vendor {
            name = "DarshanBharat"
            url = "https://github.com/DarshanBharat/JetBrains-Status-Bar-Pet-Plugin"
        }
        ideaVersion {
            sinceBuild = "241"
            untilBuild = provider { null }
        }
    }
}

tasks.withType<JavaCompile> {
    sourceCompatibility = "17"
    targetCompatibility = "17"
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        freeCompilerArgs.addAll(
            "-Xskip-metadata-version-check",
            "-Xjvm-default=all"
        )
    }
}

