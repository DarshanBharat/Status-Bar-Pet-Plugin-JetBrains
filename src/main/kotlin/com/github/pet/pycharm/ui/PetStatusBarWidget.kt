package com.github.pet.pycharm.ui

import com.github.pet.pycharm.icons.PetParticleManager
import com.github.pet.pycharm.icons.PetVectorRenderer
import com.github.pet.pycharm.models.*
import com.github.pet.pycharm.state.PetPersistentState
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.JBPopup
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.wm.CustomStatusBarWidget
import com.intellij.openapi.wm.StatusBar
import com.intellij.ui.JBColor
import com.intellij.util.ui.JBUI
import java.awt.*
import java.awt.event.*
import java.util.concurrent.ConcurrentHashMap
import javax.swing.*
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.sin

class PetStatusBarWidget(private val project: Project) : CustomStatusBarWidget {
    companion object {
        const val WIDGET_ID = "com.github.pet.pycharm.StatusBarPet"
        private val instances = ConcurrentHashMap<Project, PetStatusBarWidget>()
        private val pendingSignals = ConcurrentHashMap.newKeySet<Pair<Project?, Int>>()
        private fun dispatch(project: Project? = null, signal: Int = 0, action: (PetStatusBarWidget) -> Unit) {
            if (instances.isEmpty()) return
            val app = ApplicationManager.getApplication() ?: return
            if (app.isDisposed) return
            val key = if (signal == 0) null else project to signal
            if (key != null && !pendingSignals.add(key)) return
            app.invokeLater {
                try {
                    val targets = if (project == null) instances.values.toList() else listOfNotNull(instances[project])
                    targets.filter { !it.project.isDisposed && !it.disposed }.forEach(action)
                } finally { if (key != null) pendingSignals.remove(key) }
            }
        }
        fun notifyCodingActivity() = notifyLiveTyping(1)
        fun notifyLiveTyping(charsCount: Int, project: Project? = null) = dispatch(project, 1) { it.onTyping() }
        fun notifyDeletion(deletedLen: Int, project: Project? = null) = dispatch(project, 2) { it.trigger(PetActivityState.SCRUNCHING_PAPER, 1600) }
        fun notifySaved() = dispatch(signal = 3) { it.saveStampUntil = System.currentTimeMillis() + 1500 }
        fun notifyWatchingTests(runName: String, project: Project? = null, runId: Long = 0L) = dispatch(project) {
            it.runningTasks[runId] = PetActivityState.WATCHING_TESTS
            it.trigger(PetActivityState.WATCHING_TESTS, 2400)
        }
        fun notifyRunStarted(runName: String, isDebug: Boolean, project: Project? = null, runId: Long = 0L) = dispatch(project) {
            val activity = if (isDebug) PetActivityState.DEBUGGING else PetActivityState.RUNNING
            it.runningTasks[runId] = activity
            it.trigger(activity, 2400)
        }
        fun notifyRunStopped(runId: Long, project: Project? = null) = dispatch(project) {
            it.runningTasks.remove(runId)
            if (it.temporaryState in setOf(PetActivityState.RUNNING, PetActivityState.WATCHING_TESTS, PetActivityState.DEBUGGING)) {
                it.temporaryUntil = 0L
            }
        }
        fun notifyCommitStarted() = dispatch { it.trigger(PetActivityState.COMMITTING, 2500) }
        fun notifyFridayPushMeme() = dispatch { it.message("Friday commit! Take a moment to review your changes.") }
        fun notifyMonsterDiffMeme(fileCount: Int) = dispatch { it.message("$fileCount files in this commit. A good time for one more review.") }
        fun notifyLateNightMeme() = dispatch { it.message("Late night coding? Remember to take a break.") }
        fun notifyLevelUp(newLevel: Int, petName: String) = dispatch { it.levelUp(newLevel) }
        fun notifyCommitSuccess(commitMsg: String, leveledUp: Boolean) = dispatch {
            it.trigger(PetActivityState.COMMIT_SUCCEEDED, 3600)
            it.message("Commit complete · +50 XP\n${commitMsg.lineSequence().firstOrNull().orEmpty().take(80)}")
            if (leveledUp) it.levelUp(it.petState.level)
        }
        fun notifyMergeConflict(fileName: String, project: Project? = null) = dispatch(project) {
            it.trigger(PetActivityState.MERGE_CONFLICT, 4000)
            it.message("Spotted merge conflicts in ${fileName.take(40)}! 🔍\nLet's resolve them carefully.")
        }
        fun notifyRunFinished(runName: String, isSuccess: Boolean, isTest: Boolean, project: Project? = null) = dispatch(project) {
            it.trigger(when {
                !isSuccess && isTest -> PetActivityState.TESTS_FAILED
                !isSuccess -> PetActivityState.RUN_FAILED
                isTest -> PetActivityState.STADIUM_VICTORY
                else -> PetActivityState.RUN_SUCCEEDED
            }, 3200)
            it.message(if (isSuccess) "${if (isTest) "Tests passed · +25 XP" else "Run complete · +10 XP"}\n${runName.take(80)}"
                else "Don't worry, you've got this! 🔧\nLet's check the error in ${runName.take(60)}")
        }
    }

    private val petState = PetPersistentState.getInstance()
    private data class Motion(var x: Float = -1f, var target: Float = -1f, var facingRight: Boolean = true,
        var walk: Float = 0f, var waitingUntil: Long = 0L, var walking: Boolean = false, var sleeping: Boolean = false,
        var sleepBlend: Float = 0f, var settlingAt: Long = 0L, var wakingAt: Long = 0L,
        var displayState: PetActivityState = PetActivityState.IDLE, var actionProgress: Float = 0f,
        var idleState: PetActivityState = PetActivityState.IDLE, var idleStarted: Long = 0L, var idleUntil: Long = 0L,
        var nextIdleAt: Long = System.currentTimeMillis() + 5000 + (Math.random() * 6000).toLong(),
        var lastZzzAt: Long = 0L, var buddyNap: Boolean = false)
    private data class SocialVisit(val first: String, val second: String, val started: Long,
        val napAfter: Boolean, var greetingAt: Long = 0L, var lastHeartAt: Long = 0L)
    private val motions = mutableMapOf<String, Motion>()
    private var disposed = false
    private var statusBar: StatusBar? = null
    private var tick = 0L
    private var animationMillis = 0.0
    private var lastFrameNanos = 0L
    private var levelGlowUntil = 0L
    private var socialVisit: SocialVisit? = null
    private var nextSocialAt = System.currentTimeMillis() + 10_000
    private val runningTasks = linkedMapOf<Long, PetActivityState>()
    private var lastActivity = System.currentTimeMillis()
    private var temporaryState = PetActivityState.IDLE
    private var temporaryUntil = 0L
    private var saveStampUntil = 0L
    private var pettedPet: String? = null
    private var lastBreakReminderAt = 0L
    private var lastPetting = 0L
    private var lastAffection = 0L
    private var pointerX: Float? = null
    private var pointerY = 0f
    private var pettingOffset = 0f
    private var pomodoroEnd = 0L
    private var activeStatusMenu: PetMenuPanel? = null
    private var activeMessagePopup: JBPopup? = null
    private val messageTimer = Timer(6000) {
        activeMessagePopup?.cancel()
        activeMessagePopup = null
    }.apply { isRepeats = false }
    private var lastLayoutSignature = 0
    private var geometryWidth = -1
    private var geometryHeight = -1
    private var geometryPets: List<PetType> = emptyList()
    private var geometryScale = 0f
    private val particles = PetParticleManager()
    private val animationTimer = Timer(petState.getSpeed().delayMs) { animate() }
    private val clockTimer = Timer(1000) { heartbeat() }
    private val scale: Float get() {
        // Fill the available height, reserving extra headroom only for taller headwear.
        val logicalHeight = when {
            "hat" in petState.activeAccessories -> 31f
            "crown" in petState.activeAccessories || "halo" in petState.activeAccessories ||
                "viking" in petState.activeAccessories || "pirate" in petState.activeAccessories -> 29f
            else -> 27f
        }
        val heightScale = min(JBUI.scale(1).toFloat() * 1.25f, (component.height.coerceAtLeast(18) - 2) / logicalHeight)
        val availableWidth = component.width.takeIf { it > 0 } ?: component.preferredSize.width
        val widthScale = availableWidth / (petState.getVisiblePets().size * 42f)
        var result = min(heightScale, widthScale)
        val pets = petState.getVisiblePets()
        // Enlarging a window must not enlarge two stationary sleeping pets into
        // each other. Preserve their anchors and temporarily limit visual scale.
        for (i in pets.indices) for (j in i + 1 until pets.size) {
            val a = motions[pets[i].id] ?: continue
            val b = motions[pets[j].id] ?: continue
            if ((a.sleeping || a.sleepBlend > 0f || b.sleeping || b.sleepBlend > 0f) && a.x >= 0 && b.x >= 0) {
                result = min(result, (abs(a.x - b.x) / 33f).coerceAtLeast(0.1f))
            }
        }
        return result
    }
    private val baseline: Float get() = component.height - 11f * scale

    private val component: JPanel = object : JPanel() {
        init {
            isOpaque = false
            cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
            toolTipText = null
            border = JBUI.Borders.empty()
            addComponentListener(object : ComponentAdapter() {
                override fun componentResized(e: ComponentEvent) { if (!disposed) { constrainMotions(); repaint() } }
            })
            addMouseListener(object : MouseAdapter() {
                override fun mouseClicked(e: MouseEvent) {
                    if (e.button == MouseEvent.BUTTON1) showStatus()
                    else if (e.button == MouseEvent.BUTTON3) showStatus()
                }
                override fun mouseExited(e: MouseEvent) {
                    pointerX = null
                    pettedPet = null
                    motions.values.forEach { it.waitingUntil = 0 }
                    repaint()
                }
            })
            addMouseMotionListener(object : MouseMotionAdapter() {
                override fun mouseMoved(e: MouseEvent) = trackMouse(e)
                override fun mouseDragged(e: MouseEvent) = trackMouse(e)
            })
        }
        override fun getToolTipText(): String? = null
        override fun getToolTipText(e: MouseEvent): String? = null
        override fun paintComponent(g: Graphics) {
            super.paintComponent(g)
            constrainMotions()
            val now = System.currentTimeMillis()
            val pets = petState.getVisiblePets()
            val g2 = g as Graphics2D
            val origTransform = g2.transform
            pets.forEachIndexed { index, type ->
                val motion = motionFor(type, index, pets.size)
                val activity = motion.displayState
                val petTick = if (petState.isPaused) 0L else (tick + type.ordinal * 37L + index * 53L)
                if (petState.showHomes) {
                    g2.transform = origTransform
                    g2.translate(homeX(index, pets.size).toDouble(), baseline.toDouble())
                    g2.scale(scale.toDouble(), scale.toDouble())
                    PetVectorRenderer.renderHome(g2, type, if (petState.isPaused || petState.reducedMotion) 0 else (tick + index * 41L))
                }
                if (now < levelGlowUntil) {
                    g2.transform = origTransform
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
                    val strength = if (petState.reducedMotion) 45 else (30 + 25 * (sin(petTick * 0.22) + 1)).toInt()
                    g2.color = Color(84, 138, 247, strength.coerceIn(0, 100))
                    g2.fill(java.awt.geom.Ellipse2D.Float(motion.x - 17 * scale, baseline - 18 * scale, 34 * scale, 27 * scale))
                }
                g2.transform = origTransform
                g2.translate((motion.x + if (!petState.reducedMotion && pettedPet == type.id && !motion.sleeping && motion.sleepBlend == 0f) pettingOffset * scale else 0f).toDouble(), baseline.toDouble())
                g2.scale(scale.toDouble(), scale.toDouble())
                PetVectorRenderer.renderPet(g2, type, activity, petTick,
                    if (activity != PetActivityState.SLEEPING && (motion.walking || activity == PetActivityState.RUNNING)) motion.walk else 0f,
                    motion.facingRight, jumpOffset = when (activity) {
                        PetActivityState.CELEBRATING, PetActivityState.STADIUM_VICTORY, PetActivityState.PLAYING -> abs(sin(petTick * 0.35)).toFloat() * 1.4f
                        else -> 0f
                    },
                    isPaused = petState.isPaused, activeAccessories = petState.activeAccessories,
                    showSaveStamp = now < saveStampUntil && type == petState.getSelectedPetType(),
                    gazeX = if (petState.reducedMotion) 0f else pointerX?.let { ((it - motion.x) / 35f).coerceIn(-1f, 1f) }
                        ?: sin(petTick * (if (activity == PetActivityState.LOOKING_AROUND) 0.12 else 0.018) + index).toFloat(),
                    coat = petState.getCoat(type), sleepBlend = motion.sleepBlend,
                    actionProgress = motion.actionProgress, reducedMotion = petState.reducedMotion)
                if ((petState.isPaused || petState.reducedMotion) && motion.sleeping) {
                    g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)
                    g2.font = UIManager.getFont("Label.font").deriveFont(Font.BOLD, 9f)
                    g2.color = Color(0x70A1FF)
                    g2.drawString("Zzz", 4f, -6f)
                }
            }
            g2.transform = origTransform
            if (!petState.isPaused && !petState.reducedMotion) particles.render(g2)
        }
    }

    init {
        ApplicationManager.getApplication().getService(com.github.pet.pycharm.tracking.PetEditTracker::class.java)
        instances[project] = this
        updateSize()
        clockTimer.start()
    }
    override fun ID() = WIDGET_ID
    override fun getComponent(): JComponent = component
    override fun install(statusBar: StatusBar) { this.statusBar = statusBar; updateAnimationSchedule() }
    override fun dispose() {
        disposed = true
        animationTimer.stop()
        clockTimer.stop()
        messageTimer.stop()
        activeStatusMenu?.isVisible = false
        activeMessagePopup?.cancel()
        particles.clear()
        instances.remove(project, this)
        if (instances.isEmpty()) pendingSignals.clear()
    }
    private fun layoutSignature(): Int = 31 * (31 * petState.widthId.hashCode() + petState.getVisiblePets().hashCode()) + petState.activeAccessories.hashCode()
    private fun updateSize() {
        lastLayoutSignature = layoutSignature()
        val baseWidth = when (petState.widthId) { "compact" -> 72; "wide" -> 180; else -> 112 }
        // Share the space: one pet uses 1×, two 1.5×, and three at most 2×.
        val petCount = petState.getVisiblePets().size
        component.preferredSize = Dimension(JBUI.scale(baseWidth * (petCount + 1) / 2), JBUI.scale(24))
        component.minimumSize = Dimension(JBUI.scale(40 * (petCount + 1) / 2), JBUI.scale(18))
        component.revalidate()
        constrainMotions()
        component.repaint()
    }
    private fun bounds(index: Int, count: Int): Pair<Float, Float> {
        val width = (component.width.takeIf { it > 0 } ?: component.preferredSize.width).toFloat()
        val lane = width / count
        val margin = min(lane * 0.22f, 16f * scale)
        return index * lane + margin to (index + 1) * lane - margin
    }
    private fun homeX(index: Int, count: Int): Float = bounds(index, count).first
    private fun positionFree(type: PetType, x: Float): Boolean {
        val visit = socialVisit
        if (visit != null && (type.id == visit.first || type.id == visit.second)) return true
        return petState.getVisiblePets().none { other ->
            other != type && motions[other.id]?.let { it.x >= 0f && abs(it.x - x) < 24f * scale } == true
        }
    }

    private fun speciesSpeedMultiplier(type: PetType): Float = when (type) {
        PetType.DOG -> 1.18f     // Milo: energetic, bounding trot
        PetType.CAT -> 0.88f     // Luna: calm, elegant, deliberate stroll
        PetType.DUCK -> 0.96f    // Waddles: steady waddle
        PetType.CRAB -> 1.12f    // Pinchy: quick scuttling bursts
    }

    private fun speciesWalkCadence(type: PetType): Float = when (type) {
        PetType.DOG -> 0.90f     // Longer bounds
        PetType.CAT -> 0.72f     // Smooth stealthy glide
        PetType.DUCK -> 1.05f    // Rapid little waddling foot taps
        PetType.CRAB -> 1.45f    // Rapid 6-legged scuttling clatter
    }

    private fun speciesWaitInterval(type: PetType): Long = when (type) {
        PetType.DOG -> 800L + (Math.random() * 1800).toLong()
        PetType.CAT -> 2200L + (Math.random() * 3800).toLong()
        PetType.DUCK -> 1200L + (Math.random() * 2500).toLong()
        PetType.CRAB -> 1500L + (Math.random() * 2800).toLong()
    }

    private fun pickNextTarget(type: PetType, left: Float, right: Float, currentX: Float): Float {
        val span = right - left
        if (span <= 1f) return (left + right) / 2
        val r = Math.random()
        return when {
            r < 0.45 -> {
                val farEdge = if (abs(currentX - left) < abs(currentX - right)) right else left
                farEdge + (if (farEdge == left) 1f else -1f) * (Math.random() * span * 0.15f).toFloat()
            }
            r < 0.75 -> left + span * (0.20f + (Math.random() * 0.60f).toFloat())
            else -> {
                val delta = (span * 0.35f) * (if (Math.random() < 0.5) 1f else -1f)
                (currentX + delta).coerceIn(left, right)
            }
        }.coerceIn(left, right)
    }

    private fun randomIdleState(type: PetType): PetActivityState = when (type) {
        PetType.CAT -> listOf(PetActivityState.GROOMING, PetActivityState.YAWNING, PetActivityState.STRETCHING, PetActivityState.LOOKING_AROUND).random()
        PetType.DOG -> listOf(PetActivityState.EAR_TWITCH, PetActivityState.LOOKING_AROUND, PetActivityState.YAWNING, PetActivityState.STRETCHING).random()
        PetType.DUCK -> listOf(PetActivityState.GROOMING, PetActivityState.EAR_TWITCH, PetActivityState.LOOKING_AROUND).random()
        PetType.CRAB -> listOf(PetActivityState.LOOKING_AROUND, PetActivityState.EAR_TWITCH, PetActivityState.STRETCHING).random()
    }

    private fun motionFor(type: PetType, index: Int, count: Int): Motion = motions.getOrPut(type.id) { Motion() }.also {
        val (left, right) = bounds(index, count)
        if (it.x < 0) {
            it.x = listOf((left + right) / 2, left, right).firstOrNull { candidate -> positionFree(type, candidate) } ?: (left + right) / 2
            it.target = pickNextTarget(type, left, right, it.x)
            it.waitingUntil = System.currentTimeMillis() + (index * 600L) + (Math.random() * 800).toLong()
        }
        // A sleeping pet retains its position even when its lane or home moves.
        if (!it.sleeping && it.sleepBlend == 0f && it.wakingAt == 0L) {
            val clamped = it.x.coerceIn(left, right)
            it.x = clamped
            it.target = it.target.coerceIn(left, right)
        }
    }
    private fun constrainMotions() {
        if (disposed) return
        val pets = petState.getVisiblePets()
        val currentScale = scale
        if (geometryWidth == component.width && geometryHeight == component.height && geometryPets === pets && geometryScale == currentScale) return
        geometryWidth = component.width; geometryHeight = component.height; geometryPets = pets; geometryScale = currentScale
        val pinned = pets.any { type -> motions[type.id]?.let { it.sleeping || it.sleepBlend > 0f || it.wakingAt > 0L } == true }
        if (pinned) {
            pets.forEachIndexed { i, type -> motionFor(type, i, pets.size) }
        } else {
            // Assign all awake lanes together: checking against old positions
            // sequentially can otherwise leave two pets overlapping after resize.
            pets.forEachIndexed { index, type ->
                val motion = motions.getOrPut(type.id) { Motion() }
                val (left, right) = bounds(index, pets.size)
                if (motion.x < 0) {
                    motion.x = (left + right) / 2
                    motion.target = pickNextTarget(type, left, right, motion.x)
                    motion.waitingUntil = System.currentTimeMillis() + (index * 600L)
                }
                else motion.x = motion.x.coerceIn(left, right)
                motion.target = motion.target.coerceIn(left, right)
            }
        }
    }
    private fun activityFor(type: PetType, now: Long): PetActivityState = when {
        petState.isPaused -> PetActivityState.IDLE
        pettedPet == type.id -> PetActivityState.PETTING
        now < temporaryUntil -> temporaryState
        runningTasks.isNotEmpty() -> runningTasks.values.last()
        motions[type.id]?.buddyNap == true && now - lastActivity > 5000 -> PetActivityState.SLEEPING
        now - lastActivity > 35_000 && (!petState.showHomes || !petState.wanderingEnabled || petState.reducedMotion || motions[type.id]?.sleeping == true || atHome(type)) -> PetActivityState.SLEEPING
        else -> PetActivityState.IDLE
    }
    private fun atHome(type: PetType): Boolean {
        val pets = petState.getVisiblePets()
        return abs((motions[type.id]?.x ?: 0f) - homeX(pets.indexOf(type), pets.size)) < 0.01f
    }
    private fun trackMouse(e: MouseEvent) {
        if (petState.isPaused) return
        pointerX = e.x.toFloat()
        pointerY = e.y.toFloat()
        markActivity()
        val nearest = petState.getVisiblePets().minByOrNull { abs((motions[it.id]?.x ?: 0f) - e.x) } ?: return
        val motion = motions[nearest.id] ?: return
        pettedPet = if (abs(motion.x - e.x) < 15f * scale && abs(baseline - pointerY) < 18f * scale) nearest.id else null
        if (pettedPet != null) {
            lastPetting = lastActivity
            if (lastPetting - lastAffection > 700) {
                lastAffection = lastPetting
                if (!petState.reducedMotion) particles.spawnHeart(motion.x, baseline - 10 * scale)
                if (petState.petAffectionTick()) levelUp(petState.level)
            }
        }
    }
    private fun markActivity() {
        lastActivity = System.currentTimeMillis()
        socialVisit = null
        nextSocialAt = lastActivity + 10_000 + (Math.random() * 5000).toLong()
        motions.values.forEach { it.buddyNap = false; it.idleUntil = 0 }
    }

    private var lastScheduleCheckMillis = 0L
    private var cachedForeground = true

    private fun updateAnimationSchedule(): Boolean = updateAnimationSchedule(force = true)

    private fun updateAnimationSchedule(force: Boolean): Boolean {
        if (disposed) return false
        val now = System.currentTimeMillis()
        if (!force && now - lastScheduleCheckMillis < 300L) {
            return cachedForeground
        }
        lastScheduleCheckMillis = now
        val window = SwingUtilities.getWindowAncestor(component)
        val minimized = window is Frame && window.extendedState and Frame.ICONIFIED != 0
        val visible = component.isShowing && window?.isVisible == true
        val foreground = window?.isActive == true || activeStatusMenu?.isVisible == true
        cachedForeground = foreground
        val delay = AnimationSchedule.delayMs(visible, minimized, petState.isPaused, foreground, petState.reducedMotion, petState.getSpeed().delayMs)
        if (delay < 0) {
            animationTimer.stop()
            lastFrameNanos = 0L
            particles.clear()
            return false
        }
        animationTimer.delay = delay
        if (!animationTimer.isRunning) { lastFrameNanos = 0L; animationTimer.start() }
        return foreground
    }

    /** Returns true while a pose requires a fixed horizontal position. */
    private fun advanceRest(motion: Motion, desired: PetActivityState, now: Long, elapsedMs: Float): Boolean {
        if (desired == PetActivityState.SLEEPING) {
            if (!motion.sleeping) { motion.sleeping = true; motion.settlingAt = now; motion.wakingAt = 0L }
            motion.target = motion.x
            motion.walk = 0f
            motion.waitingUntil = 0L
            val yawnMs = if (petState.reducedMotion) 0L else 900L
            motion.actionProgress = ((now - motion.settlingAt).toFloat() / 900f).coerceIn(0f, 1f)
            if (now - motion.settlingAt < yawnMs) {
                motion.displayState = PetActivityState.YAWNING
            } else {
                motion.displayState = PetActivityState.SLEEPING
                motion.sleepBlend = if (petState.reducedMotion) 1f else (motion.sleepBlend + elapsedMs / 750f).coerceAtMost(1f)
                if (!petState.reducedMotion && motion.sleepBlend > 0.8f && now - motion.lastZzzAt > 1500) {
                    motion.lastZzzAt = now
                    particles.spawnZzz(motion.x + 4 * scale, baseline - 6 * scale)
                }
            }
            return true
        }
        if (motion.sleeping) {
            motion.sleeping = false
            motion.settlingAt = 0L
            motion.wakingAt = now
            motion.nextIdleAt = now + 5000
        }
        if (petState.reducedMotion) { motion.sleepBlend = 0f; motion.wakingAt = 0L }
        if (motion.wakingAt > 0L) {
            motion.sleepBlend = (motion.sleepBlend - elapsedMs / 650f).coerceAtLeast(0f)
            motion.displayState = PetActivityState.STRETCHING
            motion.actionProgress = ((now - motion.wakingAt - 350f) / 1100f).coerceIn(0f, 1f)
            if (now - motion.wakingAt < 1450) return true
            motion.wakingAt = 0L
            motion.sleepBlend = 0f
        }
        motion.displayState = desired
        motion.actionProgress = 0f
        return false
    }

    private fun updateSocialVisit(pets: List<PetType>, now: Long) {
        val allowed = pets.size > 1 && petState.companionInteractions && petState.wanderingEnabled && !petState.reducedMotion &&
            pointerX == null && pettedPet == null && now >= temporaryUntil && runningTasks.isEmpty() && now - lastActivity in 5000..34_000
        if (!allowed) { socialVisit = null; return }
        var visit = socialVisit
        if (visit == null && now >= nextSocialAt) {
            val index = (Math.random() * (pets.size - 1)).toInt()
            visit = SocialVisit(pets[index].id, pets[index + 1].id, now, !petState.showHomes && Math.random() < 0.5)
            socialVisit = visit
            nextSocialAt = now + 25_000
        }
        visit ?: return
        val firstIndex = pets.indexOfFirst { it.id == visit.first }
        val secondIndex = pets.indexOfFirst { it.id == visit.second }
        if (firstIndex < 0 || secondIndex != firstIndex + 1 || now - visit.started > 22_000) { socialVisit = null; return }
        val first = motionFor(pets[firstIndex], firstIndex, pets.size)
        val second = motionFor(pets[secondIndex], secondIndex, pets.size)
        if (first.sleeping || second.sleeping || first.wakingAt > 0 || second.wakingAt > 0) { socialVisit = null; return }
        first.target = bounds(firstIndex, pets.size).second
        second.target = bounds(secondIndex, pets.size).first
        first.waitingUntil = 0L; second.waitingUntil = 0L
        if (abs(first.x - first.target) < 0.1f && abs(second.x - second.target) < 0.1f) {
            if (visit.greetingAt == 0L) visit.greetingAt = now
            first.facingRight = true; second.facingRight = false
            if (now - visit.greetingAt > 3800) {
                first.buddyNap = visit.napAfter; second.buddyNap = visit.napAfter
                first.waitingUntil = now + 2000; second.waitingUntil = now + 2000
                socialVisit = null
            } else if (now - visit.lastHeartAt > 1000) {
                visit.lastHeartAt = now
                particles.spawnHeart((first.x + second.x) / 2, baseline - 8 * scale)
            }
        }
    }

    private fun animate() {
        if (disposed || petState.isPaused) return
        val foreground = updateAnimationSchedule(force = false)
        if (!animationTimer.isRunning) return
        val nanos = System.nanoTime()
        val elapsedMs = if (lastFrameNanos == 0L) petState.getSpeed().delayMs.toFloat()
            else ((nanos - lastFrameNanos) / 1_000_000f).coerceIn(0f, 100f)
        lastFrameNanos = nanos
        animationMillis += elapsedMs * if (foreground) 1.0 else 0.35
        tick = (animationMillis / 55).toLong()
        val now = System.currentTimeMillis()
        if (now - lastPetting > 450) pettedPet = null
        pettingOffset += ((if (pettedPet != null) sin(tick * 0.7).toFloat() * 0.55f else 0f) - pettingOffset) * 0.25f
        val pets = petState.getVisiblePets()
        updateSocialVisit(pets, now)
        pets.forEachIndexed { index, type ->
            val motion = motionFor(type, index, pets.size)
            val activity = activityFor(type, now)
            motion.walking = false
            if (advanceRest(motion, activity, now, elapsedMs)) return@forEachIndexed
            if (activity != PetActivityState.IDLE) {
                if (activity == PetActivityState.RUNNING && !petState.reducedMotion) motion.walk += elapsedMs / 100f
                return@forEachIndexed
            }
            val visit = socialVisit?.takeIf { it.first == type.id || it.second == type.id }
            if (visit != null && visit.greetingAt > 0L) {
                motion.displayState = if (now - visit.greetingAt < 1200) PetActivityState.GREETING else PetActivityState.PLAYING
                motion.actionProgress = ((now - visit.greetingAt) / 3800f).coerceIn(0f, 1f)
                return@forEachIndexed
            }
            val returningHome = now - lastActivity > 35_000 && petState.showHomes && petState.wanderingEnabled
            val following = pointerX != null && petState.followMouse
            if (visit == null && !returningHome && !following) {
                if (now < motion.idleUntil) {
                    motion.displayState = motion.idleState
                    motion.actionProgress = ((now - motion.idleStarted).toFloat() / (motion.idleUntil - motion.idleStarted)).coerceIn(0f, 1f)
                    return@forEachIndexed
                }
                if (now >= motion.nextIdleAt) {
                    motion.idleState = randomIdleState(type)
                    motion.idleStarted = now
                    motion.idleUntil = now + (if (type == PetType.CAT) 2400L else 1800L)
                    motion.nextIdleAt = now + 7000L + (Math.random() * 9000).toLong() + (index * 1500L)
                    motion.displayState = motion.idleState
                    return@forEachIndexed
                }
            }
            val (left, right) = bounds(index, pets.size)
            when {
                returningHome -> { motion.target = homeX(index, pets.size); motion.waitingUntil = 0L }
                visit != null -> Unit // The visit assigned safe targets inside each pet's lane.
                following -> {
                    val followOffset = when (type) {
                        PetType.DOG -> 0f
                        PetType.CAT -> if (index % 2 == 0) -8f * scale else 8f * scale
                        PetType.DUCK -> if (index % 2 == 0) 4f * scale else -4f * scale
                        PetType.CRAB -> if (motion.facingRight) -6f * scale else 6f * scale
                    }
                    motion.target = (pointerX!! + followOffset).coerceIn(left, right)
                    motion.waitingUntil = 0L
                }
                !petState.wanderingEnabled -> { motion.target = motion.x; return@forEachIndexed }
                now < motion.waitingUntil -> return@forEachIndexed
            }
            val distance = motion.target - motion.x
            if (abs(distance) > 0.01f) {
                motion.facingRight = distance > 0
                val speedMult = speciesSpeedMultiplier(type)
                val cadence = speciesWalkCadence(type)
                val step = min(abs(distance), petState.getSpeed().walkSpeed * JBUI.scale(1) * 1.8f * speedMult * elapsedMs / petState.getSpeed().delayMs)
                val nextX = motion.x + if (distance > 0) step else -step
                if (positionFree(type, nextX)) {
                    motion.x = nextX
                    motion.walk += step * cadence
                    motion.walking = true
                } else {
                    motion.waitingUntil = now + speciesWaitInterval(type)
                    motion.target = pickNextTarget(type, left, right, motion.x)
                    motion.walk = 0f
                }
            } else if (visit == null && pointerX == null && now - lastActivity <= 35_000) {
                motion.waitingUntil = now + speciesWaitInterval(type)
                motion.target = pickNextTarget(type, left, right, motion.x)
                motion.walk = 0f
            }
        }
        if (!petState.reducedMotion) particles.update()
        component.repaint()
    }
    private fun heartbeat() {
        if (disposed) return
        if (lastLayoutSignature != layoutSignature()) updateSize()
        if (!petState.followMouse || petState.reducedMotion) pointerX = null
        updateAnimationSchedule(force = true)
        val (leveledUp, bonus) = petState.recordCodingTimeTick(1)
        if (leveledUp) levelUp(petState.level)
        else if (bonus >= 25) message("Flow bonus · +$bonus XP")
        if (petState.healthRemindersEnabled && petState.getContinuousFlowMinutes() >= 45 && System.currentTimeMillis() - lastBreakReminderAt > 45 * 60_000L) {
            lastBreakReminderAt = System.currentTimeMillis()
            trigger(PetActivityState.HYDRATE_REMINDER, 4500)
            message("☕ Time for a quick stretch and water break!\nGreat ${petState.getContinuousFlowMinutes()}m focus session.")
        }
        if (pomodoroEnd > 0 && System.currentTimeMillis() >= pomodoroEnd) {
            pomodoroEnd = 0
            val gainedLevel = petState.recordFocusComplete()
            message("Focus session complete · +50 XP\nTime for a short break.")
            if (gainedLevel) levelUp(petState.level)
        }
        activeStatusMenu?.takeIf { it.isVisible }?.refresh()
    }
    private fun trigger(activity: PetActivityState, duration: Long) {
        markActivity()
        if (petState.isPaused) return
        temporaryState = activity
        temporaryUntil = System.currentTimeMillis() + duration
        component.repaint()
    }
    private fun onTyping() {
        // Keep important reactions visible while editor events arrive.
        if (System.currentTimeMillis() < temporaryUntil && temporaryState != PetActivityState.CODING) {
            markActivity()
            return
        }
        trigger(PetActivityState.CODING, 1400)
    }
    private fun feed() {
        val (leveledUp, bonus) = petState.feed()
        trigger(PetActivityState.EATING, 2200)
        message(if (bonus > 0) "${petState.getPetName()} enjoyed ${petState.getSelectedPetType().treatName} · +$bonus XP" else "Still full! Treat XP is available once a minute.")
        if (leveledUp) levelUp(petState.level)
    }
    private fun toggleFocus() {
        pomodoroEnd = if (pomodoroEnd > 0) 0 else System.currentTimeMillis() + 25 * 60_000
        message(if (pomodoroEnd > 0) "25 minute focus session started." else "Focus session stopped.")
    }
    private fun levelUp(level: Int) {
        levelGlowUntil = System.currentTimeMillis() + 2200
        if (System.currentTimeMillis() >= temporaryUntil && runningTasks.isEmpty()) trigger(PetActivityState.CELEBRATING, 2200)
        val unlocked = PetAccessory.entries.filter { it.unlockLevel == level }.joinToString { it.displayName }
        message("Level $level · ${petState.getDeveloperRank()}${if (unlocked.isNotEmpty()) "\nUnlocked: $unlocked" else ""}")
    }
    private fun escape(text: String): String = text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;")
    private fun showStatus() {
        if (activeStatusMenu?.isVisible == true) { activeStatusMenu?.isVisible = false; return }
        activeMessagePopup?.cancel()
        val panel = PetMenuPanel(petState, { pomodoroEnd > 0 },
            onClose = { activeStatusMenu?.isVisible = false },
            onFeed = { feed() },
            onCheer = {
                trigger(PetActivityState.CELEBRATING, 2200)
                val type = petState.getSelectedPetType()
                message(type.cheerPhrases.random().replace(type.defaultName, petState.getPetName(type)))
            },
            onFocus = { toggleFocus() },
            onChange = {
                pettedPet = null
                if (petState.reducedMotion || petState.isPaused) particles.clear()
                if (!petState.followMouse || petState.reducedMotion) pointerX = null
                if (!petState.companionInteractions || !petState.wanderingEnabled || petState.reducedMotion) socialVisit = null
                updateAnimationSchedule(force = true)
                updateSize()
            })
        activeStatusMenu = panel
        if (!component.isShowing) return
        val bounds = component.graphicsConfiguration.bounds
        val insets = Toolkit.getDefaultToolkit().getScreenInsets(component.graphicsConfiguration)
        val anchor = component.locationOnScreen
        val size = panel.preferredSize
        val left = bounds.x + insets.left
        val right = bounds.x + bounds.width - insets.right
        val x = anchor.x.coerceIn(left, (right - size.width).coerceAtLeast(left))
        val y = (anchor.y - size.height - JBUI.scale(8)).coerceAtLeast(bounds.y + insets.top)
        panel.show(component, x - anchor.x, y - anchor.y)
    }
    private fun popup(content: JComponent, focus: Boolean, preferredFocus: JComponent? = null): JBPopup = JBPopupFactory.getInstance()
        .createComponentPopupBuilder(content, preferredFocus).setRequestFocus(focus).setFocusable(focus)
        .setCancelOnClickOutside(true).setCancelOnOtherWindowOpen(true).setCancelKeyEnabled(true).createPopup()
    private fun showAbove(popup: JBPopup, preferred: Dimension) {
        if (!component.isShowing || project.isDisposed) return
        val screen = component.graphicsConfiguration.bounds
        val insets = Toolkit.getDefaultToolkit().getScreenInsets(component.graphicsConfiguration)
        val anchor = component.locationOnScreen
        val available = Rectangle(screen.x + insets.left, screen.y + insets.top,
            screen.width - insets.left - insets.right, screen.height - insets.top - insets.bottom)
        val width = preferred.width.coerceAtMost(available.width)
        val height = preferred.height.coerceAtMost((anchor.y - available.y - JBUI.scale(8)).coerceAtLeast(1))
        popup.setSize(Dimension(width, height))
        val x = anchor.x.coerceIn(available.x, (available.x + available.width - width).coerceAtLeast(available.x))
        popup.showInScreenCoordinates(component, Point(x, anchor.y - height - JBUI.scale(8)))
    }
    private fun message(text: String) {
        statusBar?.setInfo(text.lineSequence().firstOrNull().orEmpty())
        if (activeStatusMenu?.isVisible == true) {
            activeStatusMenu?.showFeedback(text)
            return
        }
        if (disposed || !component.isShowing || SwingUtilities.getWindowAncestor(component)?.isActive != true) return
        messageTimer.stop()
        activeMessagePopup?.cancel()
        val panel = JPanel(BorderLayout(JBUI.scale(8), 0)).apply { border = JBUI.Borders.empty(10, 12) }
        val textLabel = JLabel("<html><body style='width:220px'><b>${escape(petState.getPetName())}</b><br/>${escape(text).replace("\n", "<br/>")}</body></html>")
        panel.add(textLabel, BorderLayout.CENTER)
        panel.add(JButton("×").apply {
            toolTipText = null
            accessibleContext.accessibleName = "Close pet message"
            addActionListener { messageTimer.stop(); activeMessagePopup?.cancel(); activeMessagePopup = null }
        }, BorderLayout.EAST)
        activeStatusMenu?.isVisible = false
        activeMessagePopup = popup(panel, false)
        showAbove(activeMessagePopup!!, panel.preferredSize)
        messageTimer.restart()
    }
}
