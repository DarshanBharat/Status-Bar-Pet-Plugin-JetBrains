package com.github.pet.pycharm.ui

/** Pure policy, independent of window-system availability in release tests. */
object AnimationSchedule {
    fun delayMs(showing: Boolean, minimized: Boolean, paused: Boolean, foreground: Boolean, reducedMotion: Boolean, speedDelay: Int): Int = when {
        !showing || minimized || paused -> -1
        !foreground -> 300
        reducedMotion -> 160
        else -> speedDelay
    }
}
