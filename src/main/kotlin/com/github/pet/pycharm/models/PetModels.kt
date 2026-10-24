package com.github.pet.pycharm.models

enum class PetType(
    val id: String,
    val displayName: String,
    val defaultName: String,
    val description: String,
    val treatEmoji: String,
    val treatName: String,
    val cheerPhrases: List<String>
) {
    CAT(
        id = "cat",
        displayName = "Pixel Cat",
        defaultName = "Luna",
        description = "A soft and calm cat. Codes with elegance and purrs on clean code.",
        treatEmoji = "🐟",
        treatName = "Crispy Fish",
        cheerPhrases = listOf(
            "Purr-fect logic! You're writing poetry in code!",
            "Take a deep breath — you've got this bug figured out!",
            "Every expert was once a beginner. Keep flowing!",
            "Luna loves watching you code. You're doing amazing!",
            "That function was so clean, it deserves a chef's kiss!",
            "Step by step, line by line. Great things take patience!"
        )
    ),
    DOG(
        id = "dog",
        displayName = "Cyber Pup",
        defaultName = "Milo",
        description = "An energetic corgi pup who wags his tail at every passing test.",
        treatEmoji = "🍖",
        treatName = "Juicy Bone",
        cheerPhrases = listOf(
            "Woof! You are an absolute coding powerhouse today!",
            "Bugs don't stand a chance against your brainpower!",
            "I believe in you 100%! Let's conquer this feature!",
            "High paws! That was a brilliant piece of code!",
            "Such focus! Much dedication! Very genius!",
            "Take pride in how far you've come today!"
        )
    ),
    DUCK(
        id = "duck",
        displayName = "Rubber Ducky",
        defaultName = "Waddles",
        description = "The iconic developer debugging master. Explain your bugs to Waddles!",
        treatEmoji = "🍞",
        treatName = "Golden Breadcrumb",
        cheerPhrases = listOf(
            "Quack! Walk me through that function line by line!",
            "Have you checked the edge cases and boundary conditions?",
            "Quack! If the function is too complex, break it into smaller pieces!",
            "Explain the logic to me out loud — rubber ducking works wonders!",
            "Quack! Don't assume — verify with a quick print or test!",
            "You're a brilliant engineer! That bug is no match for you!"
        )
    ),
    CRAB(
        id = "crab",
        displayName = "Rust Crab",
        defaultName = "Pinchy",
        description = "The fearless concurrency crab. Fearless memory safety and borrow checker approved!",
        treatEmoji = "⚙️",
        treatName = "Rusty Gear",
        cheerPhrases = listOf(
            "Borrow checker passed with zero warnings! Blazingly fast!",
            "Fearless concurrency achieved! No race conditions in sight!",
            "Memory safe, thread safe, and unstoppable today!",
            "Zero-cost abstractions at their finest! Pure performance!",
            "Cargo check: 0 errors! Keep snipping through those features!",
            "Snip snap! You crushed that complex algorithmic puzzle!"
        )
    )
}

enum class PetActivityState {
    IDLE,               // Walking, sitting, looking around
    CODING,             // Typing at mini laptop, screen glowing with live rhythm
    SLEEPING,           // Resting horizontally in place with drifting Zzz particles
    PETTING,            // Heart eyes, purring & vibrating with cursor
    EATING,             // Munching treat
    RUNNING,            // A run or task has started
    COMMITTING,         // Preparing a commit
    DEBUGGING,          // Working through a failed run
    CELEBRATING,        // Git commit victory dance! Leaping & stars
    WATCHING_TESTS,     // Stadium mode: Leaning on edge, sweat drops, biting nails!
    STADIUM_VICTORY,    // 100% tests pass: waving green #1 foam finger & confetti!
    FAINTED,            // Test failure: comically fainted with X_X eyes & red referee card
    MEME_WARNING,       // Friday afternoon deploy / Monster diff warning sign!
    SCRUNCHING_PAPER,   // Rapid backspace/deletion: tosses crumpled paper ball
    YAWNING,
    STRETCHING,
    GROOMING,
    LOOKING_AROUND,
    EAR_TWITCH,
    GREETING,
    PLAYING,
    RUN_SUCCEEDED,
    RUN_FAILED,
    TESTS_FAILED,
    COMMIT_SUCCEEDED,
    HYDRATE_REMINDER,
    MERGE_CONFLICT
}

enum class PetCoat(
    val id: String, val label: String,
    private val cat: Int, private val dog: Int, private val duck: Int,
    private val dogEar: Int, private val duckWing: Int,
    private val crab: Int = 0xDE4C36, private val crabClaw: Int = 0xA72C15
) {
    ORIGINAL("original", "Original", 0xF58220, 0xEA8A1A, 0xFFD426, 0xC76E08, 0xF2BF11, 0xDE4C36, 0xA72C15),
    CREAM("cream", "Cream", 0xE8C99B, 0xE5C293, 0xF9EDC4, 0xBE986D, 0xDCCB92, 0xFA9A85, 0xD46A55),
    SILVER("silver", "Silver", 0x9DAFC3, 0xA4ADB9, 0xC2D5E5, 0x738399, 0x93B4CA, 0x8A9BA8, 0x5C6D7A),
    COCOA("cocoa", "Cocoa", 0xA67C66, 0xB38158, 0xC9A47C, 0x805238, 0xA77D56, 0x9E3D2B, 0x6E2114);

    private val catColor = java.awt.Color(cat)
    private val dogColor = java.awt.Color(dog)
    private val duckColor = java.awt.Color(duck)
    private val earColor = java.awt.Color(dogEar)
    private val wingColor = java.awt.Color(duckWing)
    private val crabColor = java.awt.Color(crab)
    private val clawColor = java.awt.Color(crabClaw)
    fun body(type: PetType): java.awt.Color = when (type) {
        PetType.CAT -> catColor; PetType.DOG -> dogColor; PetType.DUCK -> duckColor; PetType.CRAB -> crabColor
    }
    fun detail(type: PetType): java.awt.Color = when (type) {
        PetType.DOG -> earColor; PetType.DUCK -> wingColor; PetType.CRAB -> clawColor; else -> wingColor
    }
}

enum class AnimationSpeed(
    val id: String,
    val displayName: String,
    val delayMs: Int,
    val walkSpeed: Float
) {
    FAST("fast", "🐇 Fast (Lively)", 33, 0.45f),
    MEDIUM("medium", "🚶 Medium (Calm & Smooth)", 55, 0.28f),
    ZEN_SLOW("slow", "🐢 Zen / Slow (Ultra-Relaxed)", 90, 0.14f);

    companion object {
        fun fromId(id: String): AnimationSpeed {
            return entries.find { it.id.equals(id, ignoreCase = true) } ?: MEDIUM
        }
    }
}

enum class PetAccessory(
    val id: String,
    val displayName: String,
    val unlockLevel: Int
) {
    BOWTIE("bowtie", "🎀 Dapper Bowtie", 2),
    HEADPHONES("headphones", "🎧 Coding Headphones", 3),
    GLASSES("glasses", "👓 Hacker Glasses", 5),
    WIZARD_HAT("hat", "🎩 Wizard Hat", 7),
    PIRATE_HAT("pirate", "🏴‍☠️ Pirate Captain Hat", 9),
    CROWN("crown", "👑 Golden Crown", 12),
    HALO("halo", "😇 Angelic Halo", 15),
    VIKING_HELMET("viking", "⚔️ Viking Horns", 20)
}
