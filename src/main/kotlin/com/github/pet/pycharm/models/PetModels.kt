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
        defaultName = "Mochi",
        description = "A soft and calm cat. Codes with elegance and purrs on clean code.",
        treatEmoji = "🐟",
        treatName = "Crispy Fish",
        cheerPhrases = listOf(
            "Purr-fect logic! You're writing poetry in code!",
            "Take a deep breath — you've got this bug figured out!",
            "Every expert was once a beginner. Keep flowing!",
            "Mochi loves watching you code. You're doing amazing!",
            "That function was so clean, it deserves a chef's kiss!",
            "Step by step, line by line. Great things take patience!"
        )
    ),
    DOG(
        id = "dog",
        displayName = "Cyber Pup",
        defaultName = "Sparky",
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
        defaultName = "Quackers",
        description = "The iconic developer debugging master. Explain your bugs to Quackers!",
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
    )
}

enum class PetActivityState {
    IDLE,               // Walking, sitting, looking around
    CODING,             // Typing at mini laptop, screen glowing with live rhythm
    SLEEPING,           // Inactivity sleep: curled up with drifting Zzz particles
    PETTING,            // Heart eyes, purring & vibrating with cursor
    EATING,             // Munching treat
    CELEBRATING,        // Git commit victory dance! Leaping & stars
    WATCHING_TESTS,     // Stadium mode: Leaning on edge, sweat drops, biting nails!
    STADIUM_VICTORY,    // 100% tests pass: waving green #1 foam finger & confetti!
    FAINTED,            // Test failure: comically fainted with X_X eyes & red referee card
    MEME_WARNING,       // Friday afternoon deploy / Monster diff warning sign!
    SCRUNCHING_PAPER    // Rapid backspace/deletion: tosses crumpled paper ball
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
