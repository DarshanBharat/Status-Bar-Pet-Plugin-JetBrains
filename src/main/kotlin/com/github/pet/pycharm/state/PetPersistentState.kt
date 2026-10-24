package com.github.pet.pycharm.state

import com.github.pet.pycharm.models.AnimationSpeed
import com.github.pet.pycharm.models.PetAccessory
import com.github.pet.pycharm.models.PetType
import com.github.pet.pycharm.models.PetCoat
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.min

@State(
    name = "com.github.pet.pycharm.state.PetPersistentState",
    storages = [Storage("statusBarPet.xml")]
)
class PetPersistentState : PersistentStateComponent<PetPersistentState> {

    var petTypeId: String = PetType.DOG.id
    var customNames: MutableMap<String, String> = mutableMapOf(
        PetType.CAT.id to "Luna",
        PetType.DOG.id to "Milo",
        PetType.DUCK.id to "Waddles",
        PetType.CRAB.id to "Pinchy"
    )

    var xp: Long = 0
    var level: Int = 1
    var isPaused: Boolean = false
    var speedId: String = AnimationSpeed.MEDIUM.id
    var activeAccessories: MutableSet<String> = mutableSetOf()
    var companionPetIds: MutableSet<String> = mutableSetOf()
    var widthId: String = "normal"
    var showHomes: Boolean = true
    var followMouse: Boolean = true
    var wanderingEnabled: Boolean = true
    var reducedMotion: Boolean = false
    var companionInteractions: Boolean = true
    var coatIds: MutableMap<String, String> = mutableMapOf()
    var progressionVersion: Int = 1
    var lastFedTimestamp: Long = 0L
    var nightOwlUnlocked: Boolean = false
    var healthRemindersEnabled: Boolean = false

    var happiness: Int = 85
    var totalKeystrokes: Long = 0
    var keystrokesToday: Long = 0
    var totalCodingMinutes: Long = 0
    var codingMinutesToday: Long = 0
    var streakDays: Int = 1
    var lastActiveDate: String = ""

    // Milestones and stats
    var successfulRunsCount: Int = 0
    var passedTestsCount: Int = 0
    var commitsCount: Int = 0
    var completedPomodoros: Int = 0

    // AI edits are tracked separately when the user explicitly enables agent tracking.
    var aiAgentTrackingEnabled: Boolean = false
    var aiAgentEditCount: Long = 0
    var aiAgentCharsAdded: Long = 0
    var aiAgentCharsRemoved: Long = 0
    var aiAgentEditsByAgent: MutableMap<String, Long> = mutableMapOf()
    var manualEditCount: Long = 0
    var manualCharsRemoved: Long = 0
    var otherEditCount: Long = 0
    var otherCharsAdded: Long = 0
    var otherCharsRemoved: Long = 0

    @Transient
    private var activeCodingSecondsInInterval: Int = 0

    @Transient
    private var continuousFlowMinutes: Int = 0
    @Transient
    private var continuousFlowSeconds: Int = 0

    @Transient
    private var lastKeystrokeTimestamp: Long = 0L
    @Transient
    private var lastCodingTickTimestamp: Long = 0L

    @Synchronized
    override fun getState(): PetPersistentState = PetPersistentState().also { copy ->
        XmlSerializerUtil.copyBean(this, copy)
        copy.customNames = HashMap(customNames)
        copy.activeAccessories = HashSet(activeAccessories)
        copy.companionPetIds = HashSet(companionPetIds)
        copy.coatIds = HashMap(coatIds)
        copy.aiAgentEditsByAgent = HashMap(aiAgentEditsByAgent)
    }

    @Synchronized
    override fun loadState(state: PetPersistentState) {
        XmlSerializerUtil.copyBean(state, this)
        aiAgentTrackingEnabled = false
        reducedMotion = false
        if (customNames[PetType.CAT.id] == "Mochi") customNames[PetType.CAT.id] = "Luna"
        if (customNames[PetType.DOG.id] == "Sparky") customNames[PetType.DOG.id] = "Milo"
        if (customNames[PetType.DUCK.id] == "Quackers") customNames[PetType.DUCK.id] = "Waddles"
        if (customNames[PetType.CRAB.id] == "Ferris") customNames[PetType.CRAB.id] = "Pinchy"
        level = level.coerceIn(1, PetProgression.MAX_LEVEL)
        xp = xp.coerceAtLeast(0)
        happiness = happiness.coerceIn(0, 100)
        if (progressionVersion < 2) {
            // Preserve fractional progress when moving from the original level * 100 curve.
            xp = (xp.toDouble() / (level * 100L) * xpForLevel(level)).toLong()
            progressionVersion = 2
        }
        addXp(0)
        petTypeId = getSelectedPetType().id
        companionPetIds.retainAll(PetType.entries.map { it.id }.toSet())
        companionPetIds.remove(petTypeId)
        while (companionPetIds.size > 2) {
            val iter = companionPetIds.iterator()
            if (iter.hasNext()) { iter.next(); iter.remove() }
        }
        activeAccessories.retainAll(PetAccessory.entries.filter { isAccessoryUnlocked(it) }.map { it.id }.toSet())
        lastKeystrokeTimestamp = 0L
        lastCodingTickTimestamp = 0L
        activeCodingSecondsInInterval = 0
        continuousFlowMinutes = 0
        continuousFlowSeconds = 0
    }

    fun getContinuousFlowMinutes(): Int = continuousFlowMinutes

    fun getDeveloperRank(): String {
        return when {
            level >= 25 -> "Legendary Builder"
            level >= 20 -> "Distinguished Fellow"
            level >= 16 -> "Principal Engineer"
            level >= 12 -> "Staff Engineer"
            level >= 9  -> "Tech Lead"
            level >= 6  -> "Senior Developer"
            level >= 4  -> "Developer"
            level >= 2  -> "Junior Developer"
            else -> "Intern"
        }
    }

    fun getSelectedPetType(): PetType {
        return PetType.entries.find { it.id.equals(petTypeId, ignoreCase = true) } ?: PetType.DOG
    }

    @Synchronized
    fun setSelectedPetType(type: PetType) {
        val previous = petTypeId
        val wasCompanion = companionPetIds.remove(type.id)
        petTypeId = type.id
        if (wasCompanion) companionPetIds.add(previous)
        while (companionPetIds.size > 2) {
            val iter = companionPetIds.iterator()
            if (iter.hasNext()) { iter.next(); iter.remove() }
        }
    }

    // Stable lanes prevent a main-pet selection from swapping sleeping companions.
    fun getVisiblePets(): List<PetType> {
        var mask = 1 shl getSelectedPetType().ordinal
        PetType.entries.forEach { if (it.id in companionPetIds) mask = mask or (1 shl it.ordinal) }
        return VISIBLE_PETS[mask]
    }

    fun getPetName(type: PetType): String = customNames[type.id]?.takeIf { it.isNotBlank() } ?: type.defaultName

    fun getCoat(type: PetType): PetCoat = PetCoat.entries.firstOrNull { it.id == coatIds[type.id] } ?: PetCoat.ORIGINAL

    @Synchronized
    fun toggleCompanion(type: PetType) {
        if (type == getSelectedPetType()) return
        if (companionPetIds.contains(type.id)) {
            companionPetIds.remove(type.id)
        } else if (companionPetIds.size < 2) {
            companionPetIds.add(type.id)
        }
    }

    fun getSpeed(): AnimationSpeed {
        return AnimationSpeed.fromId(speedId)
    }

    @Synchronized
    fun setSpeed(speed: AnimationSpeed) {
        speedId = speed.id
    }

    fun isAccessoryActive(accessory: PetAccessory): Boolean {
        return activeAccessories.contains(accessory.id)
    }

    fun isAccessoryUnlocked(accessory: PetAccessory): Boolean {
        return level >= accessory.unlockLevel
    }

    @Synchronized
    fun toggleAccessory(accessory: PetAccessory) {
        if (!isAccessoryUnlocked(accessory)) return
        if (activeAccessories.contains(accessory.id)) {
            activeAccessories.remove(accessory.id)
        } else {
            // Hats and eyewear share attachment points, so equipping one replaces its peers.
            val hats = setOf("hat", "pirate", "crown", "viking")
            if (accessory.id in hats) activeAccessories.removeAll(hats)
            activeAccessories.add(accessory.id)
        }
    }

    fun getPetName(): String {
        return getPetName(getSelectedPetType())
    }

    fun xpNeededForNextLevel(): Long {
        return xpForLevel(level)
    }

    private fun xpForLevel(lvl: Int): Long = PetProgression.required(lvl)

    fun nextUnlock(): PetAccessory? = PetAccessory.entries.firstOrNull { it.unlockLevel > level }

    fun getLevelProgressPercent(): Int {
        val needed = xpNeededForNextLevel()
        if (needed <= 0) return 100
        val percent = ((xp.toDouble() / needed) * 100).toInt()
        return percent.coerceIn(0, 100)
    }

    @Synchronized
    fun addXp(amount: Long): Boolean {
        progressionVersion = 2
        level = level.coerceIn(1, PetProgression.MAX_LEVEL)
        val earned = PetProgression.add(xp, amount)
        val needed = xpNeededForNextLevel()
        if (earned < needed) { xp = earned; return false }
        val previousLevel = level
        val maximum = PetProgression.before(PetProgression.MAX_LEVEL) + PetProgression.required(PetProgression.MAX_LEVEL) - 1
        val total = PetProgression.add(PetProgression.before(level), earned).coerceAtMost(maximum)
        level = PetProgression.levelAt(total, level)
        xp = total - PetProgression.before(level)
        if (level > previousLevel) happiness = (happiness.toLong() + 15L * (level - previousLevel)).coerceAtMost(100).toInt()
        return level > previousLevel
    }

    @Synchronized
    fun recordKeystrokes(count: Int = 1): Boolean {
        if (count <= 0) return false
        checkStreak()
        val previous = totalKeystrokes
        totalKeystrokes = PetProgression.add(totalKeystrokes, count.toLong())
        keystrokesToday = PetProgression.add(keystrokesToday, count.toLong())
        if (System.currentTimeMillis() - lastKeystrokeTimestamp > 10_000L) {
            lastCodingTickTimestamp = System.currentTimeMillis()
            continuousFlowMinutes = 0
            continuousFlowSeconds = 0
        }
        lastKeystrokeTimestamp = System.currentTimeMillis()
        nightOwlUnlocked = nightOwlUnlocked || java.time.LocalTime.now().hour >= 22 || java.time.LocalTime.now().hour < 5
        return addXp(totalKeystrokes / 5L - previous / 5L)
    }

    @Synchronized
    fun recordManualEdit(addedChars: Int, removedChars: Int): Boolean {
        manualEditCount = PetProgression.add(manualEditCount, 1)
        manualCharsRemoved = PetProgression.add(manualCharsRemoved, removedChars.toLong())
        // A deletion is also active coding time, but earns no character XP.
        if (addedChars == 0) {
            checkStreak()
            if (System.currentTimeMillis() - lastKeystrokeTimestamp > 10_000L) {
                lastCodingTickTimestamp = System.currentTimeMillis()
                continuousFlowMinutes = 0
                continuousFlowSeconds = 0
            }
            lastKeystrokeTimestamp = System.currentTimeMillis()
        }
        return recordKeystrokes(addedChars)
    }

    @Synchronized
    fun recordOtherEdit(addedChars: Int, removedChars: Int) {
        otherEditCount = PetProgression.add(otherEditCount, 1)
        otherCharsAdded = PetProgression.add(otherCharsAdded, addedChars.toLong())
        otherCharsRemoved = PetProgression.add(otherCharsRemoved, removedChars.toLong())
    }

    @Synchronized
    fun recordCodingTimeTick(elapsedSeconds: Int): Pair<Boolean, Long> {
        val now = System.currentTimeMillis()
        if (lastCodingTickTimestamp == 0L || now < lastCodingTickTimestamp) {
            lastCodingTickTimestamp = now
            return false to 0L
        }
        if (lastKeystrokeTimestamp == 0L || now - lastKeystrokeTimestamp !in 0L..10_000L) {
            lastCodingTickTimestamp = now
            continuousFlowMinutes = 0
            continuousFlowSeconds = 0
            return Pair(false, 0L)
        }
        val elapsed = ((now - lastCodingTickTimestamp) / 1000L).coerceIn(0, 10).toInt()
        if (elapsed == 0) return false to 0L
        lastCodingTickTimestamp += elapsed * 1000L
        // Several open IDE windows share this state; elapsed wall time is counted once.
        checkStreak()

        activeCodingSecondsInInterval += elapsed
        continuousFlowSeconds += elapsed
        var bonusXp = 0L
        var leveledUp = false

        if (activeCodingSecondsInInterval >= 60) {
            activeCodingSecondsInInterval -= 60
            bonusXp += 5L
            totalCodingMinutes = PetProgression.add(totalCodingMinutes, 1)
            codingMinutesToday = PetProgression.add(codingMinutesToday, 1)
        }
        if (continuousFlowSeconds >= 60) {
            continuousFlowSeconds -= 60
            continuousFlowMinutes += 1
            if (continuousFlowMinutes % 10 == 0) {
                bonusXp += 25L
            }

            if (continuousFlowMinutes % 30 == 0) {
                bonusXp += 100L
            }

        }
        if (bonusXp > 0) leveledUp = addXp(bonusXp)

        return Pair(leveledUp, bonusXp)
    }

    @Synchronized
    fun petAffectionTick(): Boolean {
        happiness = min(100, happiness + 1)
        return if (Math.random() < 0.25) {
            addXp(1)
        } else {
            false
        }
    }

    @Synchronized
    fun feed(): Pair<Boolean, Long> {
        val now = System.currentTimeMillis()
        if (lastFedTimestamp <= now && now - lastFedTimestamp < 60_000L) return false to 0L
        lastFedTimestamp = now
        happiness = min(100, happiness + 20)
        val bonusXp = 15L
        val leveledUp = addXp(bonusXp)
        return Pair(leveledUp, bonusXp)
    }

    @Synchronized
    fun recordAiAgentEdit(addedChars: Int, removedChars: Int, agent: String = "AI session") {
        aiAgentEditCount = PetProgression.add(aiAgentEditCount, 1)
        aiAgentCharsAdded = PetProgression.add(aiAgentCharsAdded, addedChars.toLong())
        aiAgentCharsRemoved = PetProgression.add(aiAgentCharsRemoved, removedChars.toLong())
        aiAgentEditsByAgent[agent] = PetProgression.add(aiAgentEditsByAgent[agent] ?: 0L, 1)
    }

    @Synchronized
    fun recordRunSuccess(isTest: Boolean): Boolean {
        successfulRunsCount = (successfulRunsCount.toLong() + 1).coerceIn(0, Int.MAX_VALUE.toLong()).toInt()
        if (isTest) passedTestsCount = (passedTestsCount.toLong() + 1).coerceIn(0, Int.MAX_VALUE.toLong()).toInt()
        return addXp(if (isTest) 25 else 10)
    }

    @Synchronized
    fun recordCommitSuccess(): Boolean {
        commitsCount = (commitsCount.toLong() + 1).coerceIn(0, Int.MAX_VALUE.toLong()).toInt()
        return addXp(50)
    }

    @Synchronized
    fun recordFocusComplete(): Boolean {
        completedPomodoros = (completedPomodoros.toLong() + 1).coerceIn(0, Int.MAX_VALUE.toLong()).toInt()
        return addXp(50)
    }

    private fun checkStreak() {
        val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        if (lastActiveDate.isEmpty()) {
            lastActiveDate = today
            streakDays = 1
            return
        }

        if (lastActiveDate == today) {
            return
        }

        val lastDate = runCatching { LocalDate.parse(lastActiveDate) }.getOrNull()
        streakDays = if (lastDate == LocalDate.now().minusDays(1)) (streakDays.toLong() + 1).coerceAtMost(Int.MAX_VALUE.toLong()).toInt() else 1
        keystrokesToday = 0
        codingMinutesToday = 0
        lastActiveDate = today
    }

    companion object {
        private val VISIBLE_PETS = Array(1 shl PetType.entries.size) { mask ->
            java.util.List.copyOf(PetType.entries.filter { mask and (1 shl it.ordinal) != 0 })
        }
        fun getInstance(): PetPersistentState {
            return ApplicationManager.getApplication().getService(PetPersistentState::class.java)
        }
    }
}
