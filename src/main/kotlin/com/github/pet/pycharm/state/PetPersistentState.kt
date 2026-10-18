package com.github.pet.pycharm.state

import com.github.pet.pycharm.models.AnimationSpeed
import com.github.pet.pycharm.models.PetAccessory
import com.github.pet.pycharm.models.PetType
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
        PetType.CAT.id to "Mochi",
        PetType.DOG.id to "Sparky",
        PetType.DUCK.id to "Quackers"
    )

    var xp: Long = 0
    var level: Int = 1
    var isPaused: Boolean = false
    var speedId: String = AnimationSpeed.MEDIUM.id
    var activeAccessories: MutableSet<String> = mutableSetOf()

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

    @Transient
    private var activeCodingSecondsInInterval: Int = 0

    @Transient
    private var continuousFlowMinutes: Int = 0

    @Transient
    private var lastKeystrokeTimestamp: Long = 0L

    override fun getState(): PetPersistentState = this

    override fun loadState(state: PetPersistentState) {
        XmlSerializerUtil.copyBean(state, this)
        checkStreak()
    }

    fun getDeveloperRank(): String {
        return when {
            level >= 25 -> "10x Mythic Architect"
            level >= 20 -> "Distinguished Fellow"
            level >= 16 -> "Principal Architect"
            level >= 12 -> "Staff Engineer"
            level >= 9  -> "Tech Lead"
            level >= 6  -> "Senior Dev"
            level >= 4  -> "Mid-Level Dev"
            level >= 2  -> "Junior Dev"
            else -> "Intern"
        }
    }

    fun getSelectedPetType(): PetType {
        return PetType.entries.find { it.id.equals(petTypeId, ignoreCase = true) } ?: PetType.DOG
    }

    fun setSelectedPetType(type: PetType) {
        petTypeId = type.id
    }

    fun getSpeed(): AnimationSpeed {
        return AnimationSpeed.fromId(speedId)
    }

    fun setSpeed(speed: AnimationSpeed) {
        speedId = speed.id
    }

    fun isAccessoryActive(accessory: PetAccessory): Boolean {
        return activeAccessories.contains(accessory.id)
    }

    fun isAccessoryUnlocked(accessory: PetAccessory): Boolean {
        return level >= accessory.unlockLevel
    }

    fun toggleAccessory(accessory: PetAccessory) {
        if (!isAccessoryUnlocked(accessory)) return
        if (activeAccessories.contains(accessory.id)) {
            activeAccessories.remove(accessory.id)
        } else {
            activeAccessories.add(accessory.id)
        }
    }

    fun getPetName(): String {
        val currentType = getSelectedPetType()
        return customNames[currentType.id] ?: currentType.defaultName
    }

    fun xpNeededForNextLevel(): Long {
        return level.toLong() * 100L
    }

    fun getLevelProgressPercent(): Int {
        val needed = xpNeededForNextLevel()
        if (needed <= 0) return 100
        val percent = ((xp.toDouble() / needed) * 100).toInt()
        return percent.coerceIn(0, 100)
    }

    @Synchronized
    fun addXp(amount: Long): Boolean {
        xp += amount
        var leveledUp = false
        var needed = xpNeededForNextLevel()
        while (xp >= needed) {
            xp -= needed
            level += 1
            leveledUp = true
            happiness = min(100, happiness + 15)
            needed = xpNeededForNextLevel()

            PetAccessory.entries.find { it.unlockLevel == level }?.let {
                activeAccessories.add(it.id)
            }
        }
        return leveledUp
    }

    @Synchronized
    fun recordKeystrokes(count: Int = 1): Boolean {
        totalKeystrokes += count
        keystrokesToday += count
        lastKeystrokeTimestamp = System.currentTimeMillis()
        checkStreak()

        if (totalKeystrokes % 5L == 0L) {
            return addXp(1)
        }
        return false
    }

    @Synchronized
    fun recordCodingTimeTick(elapsedSeconds: Int): Pair<Boolean, Long> {
        val now = System.currentTimeMillis()
        if (now - lastKeystrokeTimestamp > 10_000L) {
            continuousFlowMinutes = 0
            activeCodingSecondsInInterval = 0
            return Pair(false, 0L)
        }

        activeCodingSecondsInInterval += elapsedSeconds
        var bonusXp = 0L
        var leveledUp = false

        if (activeCodingSecondsInInterval >= 30) {
            activeCodingSecondsInInterval -= 30
            bonusXp += 5L
            totalCodingMinutes += 1
            codingMinutesToday += 1
            continuousFlowMinutes += 1

            if (continuousFlowMinutes % 10 == 0) {
                bonusXp += 25L
            }

            if (continuousFlowMinutes % 30 == 0) {
                bonusXp += 100L
            }

            leveledUp = addXp(bonusXp)
        }

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
        happiness = min(100, happiness + 20)
        val bonusXp = 15L
        val leveledUp = addXp(bonusXp)
        return Pair(leveledUp, bonusXp)
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
        if (lastDate != null) {
            val yesterday = LocalDate.now().minusDays(1)
            if (lastDate == yesterday) {
                streakDays += 1
                keystrokesToday = 0
                codingMinutesToday = 0
            } else if (lastDate.isBefore(yesterday)) {
                streakDays = 1
                keystrokesToday = 0
                codingMinutesToday = 0
            }
        }
        lastActiveDate = today
    }

    companion object {
        fun getInstance(): PetPersistentState {
            return ApplicationManager.getApplication().getService(PetPersistentState::class.java)
        }
    }
}
