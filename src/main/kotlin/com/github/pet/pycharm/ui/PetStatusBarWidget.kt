package com.github.pet.pycharm.ui

import com.github.pet.pycharm.icons.PetParticleManager
import com.github.pet.pycharm.icons.PetVectorRenderer
import com.github.pet.pycharm.models.AnimationSpeed
import com.github.pet.pycharm.models.PetAccessory
import com.github.pet.pycharm.models.PetActivityState
import com.github.pet.pycharm.models.PetType
import com.github.pet.pycharm.state.PetPersistentState
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.wm.CustomStatusBarWidget
import com.intellij.openapi.wm.StatusBar
import com.intellij.ui.JBColor
import com.intellij.ui.awt.RelativePoint
import com.intellij.util.ui.JBFont
import com.intellij.util.ui.JBUI
import java.awt.*
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.event.MouseMotionAdapter
import java.awt.geom.GeneralPath
import java.awt.geom.RoundRectangle2D
import javax.swing.*
import kotlin.math.abs
import kotlin.math.sin

class PetStatusBarWidget(private val project: Project) : CustomStatusBarWidget {

    companion object {
        const val WIDGET_ID = "com.github.pet.pycharm.StatusBarPet"
        private var activeInstance: PetStatusBarWidget? = null

        fun notifyCodingActivity() {
            activeInstance?.onLiveTyping(1)
        }

        fun notifyLiveTyping(charsCount: Int) {
            activeInstance?.onLiveTyping(charsCount)
        }

        fun notifyDeletion(deletedLen: Int) {
            activeInstance?.onDeletion(deletedLen)
        }

        fun notifySaved() {
            activeInstance?.onSaved()
        }

        fun notifyWatchingTests(runName: String) {
            activeInstance?.onWatchingTests(runName)
        }

        fun notifyFridayPushMeme() {
            activeInstance?.onFridayPushMeme()
        }

        fun notifyMonsterDiffMeme(fileCount: Int) {
            activeInstance?.onMonsterDiffMeme(fileCount)
        }

        fun notifyLateNightMeme() {
            activeInstance?.onLateNightMeme()
        }

        fun notifyLevelUp(newLevel: Int, petName: String) {
            activeInstance?.showLevelUpBalloon(newLevel, petName)
        }

        fun notifyCommitSuccess(commitMsg: String, leveledUp: Boolean) {
            activeInstance?.onCommitCelebration(commitMsg, leveledUp)
        }

        fun notifyRunFinished(runName: String, isSuccess: Boolean, isTest: Boolean) {
            activeInstance?.onRunFinished(runName, isSuccess, isTest)
        }
    }

    private var statusBar: StatusBar? = null

    // Physics & animation
    private var tick: Long = 0
    private var posX: Float = 22f
    private var targetX: Float = 40f
    private var facingRight: Boolean = true
    private var walkCycle: Float = 0f
    private var isWalking: Boolean = false
    private var jumpOffset: Float = 0f
    private var jumpVelocity: Float = 0f

    // Mouse Petting / Vibration state
    private var isMousePetting: Boolean = false
    private var lastMousePettingTime: Long = 0L
    private var pettingVibrationOffset: Float = 0f
    private var mousePettingFrameCounter: Int = 0

    // Timers
    private var lastActivityTime: Long = System.currentTimeMillis()
    private val inactivitySleepThresholdMs: Long = 35_000L
    private var temporaryState: PetActivityState? = null
    private var temporaryStateEndTime: Long = 0L

    // Health / Anti-burnout hydration reminder & Time-based coding XP
    private var continuousCodingStartMs: Long = System.currentTimeMillis()
    private var lastHealthReminderMs: Long = 0L
    private var lastOneSecondTickMs: Long = System.currentTimeMillis()

    // Pomodoro sprint
    private var isPomodoroActive: Boolean = false
    private var pomodoroStartMs: Long = 0L
    private val pomodoroDurationMs: Long = 25 * 60 * 1000L

    // Save Stamp effect
    private var showSaveStamp: Boolean = false
    private var saveStampEndTime: Long = 0L

    private val particleManager = PetParticleManager()

    // Configurable animation timer based on speed setting
    private val animationTimer = Timer(PetPersistentState.getInstance().getSpeed().delayMs) {
        updatePhysics()
        component.repaint()
    }

    private val component = object : JPanel() {
        init {
            isOpaque = false
            cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
            border = JBUI.Borders.empty()
            preferredSize = Dimension(JBUI.scale(128), JBUI.scale(22))
            minimumSize = preferredSize

            addMouseListener(object : MouseAdapter() {
                override fun mouseClicked(e: MouseEvent) {
                    if (!PetPersistentState.getInstance().isPaused) {
                        wakeUp()
                    }
                    if (e.button == MouseEvent.BUTTON1 || e.button == MouseEvent.BUTTON3) {
                        showPetMenu(e.component, e.x, e.y)
                    }
                }

                override fun mouseExited(e: MouseEvent) {
                    stopMousePetting()
                }
            })

            addMouseMotionListener(object : MouseMotionAdapter() {
                override fun mouseMoved(e: MouseEvent) {
                    checkAndTriggerMousePetting(e.x, e.y)
                }

                override fun mouseDragged(e: MouseEvent) {
                    checkAndTriggerMousePetting(e.x, e.y)
                }
            })
        }

        override fun getToolTipText(): String {
            val state = PetPersistentState.getInstance()
            val petType = state.getSelectedPetType()
            val name = state.getPetName()
            val level = state.level
            val xp = state.xp
            val neededXp = state.xpNeededForNextLevel()
            val happiness = state.happiness
            val streak = state.streakDays
            val paused = state.isPaused
            val speed = state.getSpeed().displayName
            val rank = state.getDeveloperRank()

            val pomodoroInfo = if (isPomodoroActive) {
                val remainingSec = ((pomodoroDurationMs - (System.currentTimeMillis() - pomodoroStartMs)) / 1000L).coerceAtLeast(0)
                val m = remainingSec / 60
                val s = remainingSec % 60
                "<br/><b>Pomodoro:</b> ⏱️ ${m}m ${s}s remaining"
            } else ""

            val moodText = when {
                paused -> "⏸️ Frozen / Paused (Zero Motion)"
                isMousePetting -> "❤️ Purring & Enjoying Pets!"
                temporaryState == PetActivityState.CELEBRATING -> "🎉 Celebrating Git Commit / Tests!"
                temporaryState == PetActivityState.CODING -> "⚡ Typing & Flowing!"
                isAsleep() -> "💤 Sleeping peacefully (Idle)"
                happiness >= 80 -> "✨ Ecstatic & Cheerful"
                else -> "😊 Relaxed companion"
            }

            return """
                <html>
                <body style='font-family: sans-serif; padding: 4px;'>
                    <b>🐾 $name the ${petType.displayName}</b> <span style='color: #FA8231;'>[$rank &bull; Lv. $level]</span><br/>
                    <b>Mood:</b> $moodText<br/>
                    <b>XP:</b> $xp / $neededXp (${state.getLevelProgressPercent()}%)<br/>
                    <b>Streak:</b> 🔥 $streak day(s) | <b>Keys today:</b> ${state.keystrokesToday}<br/>
                    <b>Time coded today:</b> ~${state.codingMinutesToday} mins$pomodoroInfo<br/>
                    <b>Speed:</b> $speed<br/>
                    <hr style='border: none; border-top: 1px solid #444;'/>
                    <i style='color: #FF7675;'>🐾 Rub cursor over pet to pet & purr!</i><br/>
                    <i style='color: #888;'>Click for Pomodoro, Achievements, Speed & Settings</i>
                </body>
                </html>
            """.trimIndent()
        }

        override fun paintComponent(g: Graphics) {
            super.paintComponent(g)
            val g2 = g.create() as Graphics2D

            val state = PetPersistentState.getInstance()
            val petType = state.getSelectedPetType()

            val effectiveState = when {
                state.isPaused -> PetActivityState.IDLE
                isMousePetting -> PetActivityState.PETTING
                temporaryState != null -> temporaryState!!
                isAsleep() -> PetActivityState.SLEEPING
                else -> PetActivityState.IDLE
            }

            // 1. Render Pet (with vibration offset if being petted)
            val centerY = height / 2f + 1f
            val petG2 = g2.create() as Graphics2D
            val drawX = posX + (if (isMousePetting && !state.isPaused) pettingVibrationOffset else 0f)
            petG2.translate(drawX.toDouble(), centerY.toDouble())

            PetVectorRenderer.renderPet(
                g2 = petG2,
                petType = petType,
                state = effectiveState,
                tick = tick,
                walkCycle = if (isWalking && !state.isPaused) walkCycle else 0f,
                facingRight = facingRight,
                jumpOffset = if (state.isPaused) 0f else jumpOffset,
                isPaused = state.isPaused,
                activeAccessories = state.activeAccessories,
                showSaveStamp = showSaveStamp && System.currentTimeMillis() < saveStampEndTime
            )
            petG2.dispose()

            // 2. Floating particles
            if (!state.isPaused) {
                particleManager.render(g2)
            }

            // 3. Render Sleek Unified XP & Streak Pill (No broken emoji boxes!)
            renderModernPill(g2, state)

            g2.dispose()
        }

        private fun renderModernPill(g2: Graphics2D, state: PetPersistentState) {
            val pillW = JBUI.scale(56)
            val pillH = JBUI.scale(16)
            val pillX = width - pillW - JBUI.scale(2)
            val pillY = (height - pillH) / 2

            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)

            // Pill Background with subtle border
            val bg = JBColor(Color(0, 0, 0, 18), Color(255, 255, 255, 18))
            val border = JBColor(Color(0, 0, 0, 30), Color(255, 255, 255, 30))
            g2.color = bg
            g2.fill(RoundRectangle2D.Float(pillX.toFloat(), pillY.toFloat(), pillW.toFloat(), pillH.toFloat(), 6f, 6f))
            g2.color = border
            g2.stroke = BasicStroke(1f)
            g2.draw(RoundRectangle2D.Float(pillX.toFloat(), pillY.toFloat(), pillW.toFloat(), pillH.toFloat(), 6f, 6f))

            // Level text: "L2"
            g2.font = JBFont.label().deriveFont(Font.BOLD, JBUI.scale(8).toFloat())
            g2.color = JBColor(Color(0x37, 0x41, 0x51), Color(0xEA, 0xEB, 0xED))
            g2.drawString("L${state.level}", pillX + JBUI.scale(4), pillY + JBUI.scale(8))

            // Micro progress bar track
            val barX = pillX + JBUI.scale(16)
            val barY = pillY + JBUI.scale(5)
            val barW = JBUI.scale(22)
            val barH = (JBUI.scale(7) / 2).coerceAtLeast(3)

            g2.color = JBColor(Color(0, 0, 0, 35), Color(255, 255, 255, 35))
            g2.fill(RoundRectangle2D.Float(barX.toFloat(), barY.toFloat(), barW.toFloat(), barH.toFloat(), 2f, 2f))

            // Progress bar fill
            val percent = state.getLevelProgressPercent()
            val fillW = (barW * (percent / 100f)).toInt().coerceIn(0, barW)
            if (fillW > 0) {
                g2.color = when (state.getSelectedPetType()) {
                    PetType.CAT -> Color(0xF3, 0x82, 0x30)
                    PetType.DOG -> Color(0xF3, 0x9C, 0x12)
                    PetType.DUCK -> Color(0xFF, 0xD3, 0x2A)
                }
                g2.fill(RoundRectangle2D.Float(barX.toFloat(), barY.toFloat(), fillW.toFloat(), barH.toFloat(), 2f, 2f))
            }

            // Streak: Draw vector flame icon (NEVER breaks like emoji font!)
            val flameX = pillX + JBUI.scale(40).toFloat()
            val flameY = pillY + JBUI.scale(5).toFloat()
            drawVectorFlame(g2, flameX, flameY)

            // Streak number "1d"
            g2.font = JBFont.label().deriveFont(Font.PLAIN, JBUI.scale(8).toFloat())
            g2.color = JBColor(Color(0xD9, 0x77, 0x06), Color(0xFB, 0xBF, 0x24))
            g2.drawString("${state.streakDays}d", flameX + JBUI.scale(6).toFloat(), (pillY + JBUI.scale(9)).toFloat())

            // Paused badge
            if (state.isPaused) {
                g2.color = Color(0x9E, 0x9E, 0x9E)
                g2.font = Font("Segoe UI", Font.BOLD, JBUI.scale(7))
                g2.drawString("⏸", (pillX + JBUI.scale(4)).toFloat(), (pillY + JBUI.scale(14)).toFloat())
            }
        }

        private fun drawVectorFlame(g2: Graphics2D, x: Float, y: Float) {
            val flame = GeneralPath()
            flame.moveTo((x + 2.5f).toDouble(), y.toDouble())
            flame.curveTo((x + 4.5f).toDouble(), (y + 1f).toDouble(), (x + 5f).toDouble(), (y + 3.5f).toDouble(), (x + 3.5f).toDouble(), (y + 5.5f).toDouble())
            flame.curveTo((x + 2.5f).toDouble(), (y + 6.5f).toDouble(), (x + 0.5f).toDouble(), (y + 5.5f).toDouble(), x.toDouble(), (y + 3.5f).toDouble())
            flame.curveTo((x + 0.5f).toDouble(), (y + 2f).toDouble(), (x + 1.5f).toDouble(), (y + 1f).toDouble(), (x + 2.5f).toDouble(), y.toDouble())

            g2.color = Color(0xFF, 0x6B, 0x4A)
            g2.fill(flame)

            // Inner flame highlight
            g2.color = Color(0xFF, 0xD3, 0x2A)
            g2.fill(java.awt.geom.Ellipse2D.Float(x + 1.5f, y + 2.8f, 2f, 2.5f))
        }
    }

    init {
        activeInstance = this
        val state = PetPersistentState.getInstance()
        animationTimer.delay = state.getSpeed().delayMs
        if (!state.isPaused) {
            animationTimer.start()
        }
    }

    override fun ID(): String = WIDGET_ID

    override fun getComponent(): JComponent = component

    override fun install(statusBar: StatusBar) {
        this.statusBar = statusBar
    }

    override fun dispose() {
        animationTimer.stop()
        particleManager.clear()
        if (activeInstance == this) {
            activeInstance = null
        }
    }

    private fun isAsleep(): Boolean {
        if (PetPersistentState.getInstance().isPaused) return false
        if (temporaryState != null) return false
        if (isMousePetting) return false
        return (System.currentTimeMillis() - lastActivityTime) > inactivitySleepThresholdMs
    }

    private fun wakeUp() {
        lastActivityTime = System.currentTimeMillis()
        if (temporaryState == PetActivityState.SLEEPING) {
            temporaryState = null
        }
    }

    private fun checkAndTriggerMousePetting(mouseX: Int, mouseY: Int) {
        val state = PetPersistentState.getInstance()
        if (state.isPaused) return

        // Hitbox of the pet
        val hitMinX = posX - JBUI.scale(14)
        val hitMaxX = posX + JBUI.scale(14)

        if (mouseX in hitMinX.toInt()..hitMaxX.toInt()) {
            wakeUp()
            isMousePetting = true
            lastMousePettingTime = System.currentTimeMillis()

            // Vibrate pet position with purr frequency
            pettingVibrationOffset = (sin(tick * 1.8) * 1.4f).toFloat()

            mousePettingFrameCounter++
            // Every ~10 frames of active mouse movement over pet: spawn heart & increase affection
            if (mousePettingFrameCounter % 10 == 0) {
                particleManager.spawnHeart(posX + (Math.random().toFloat() * 6f - 3f), (component.height / 2f) - 6f)
                val leveledUp = state.petAffectionTick()
                if (leveledUp) {
                    showLevelUpBalloon(state.level, state.getPetName())
                }
                statusBar?.setInfo("❤️ ${state.getPetName()} is purring happily with your pets!")
            }
            component.repaint()
        } else {
            stopMousePetting()
        }
    }

    private fun stopMousePetting() {
        if (isMousePetting) {
            isMousePetting = false
            pettingVibrationOffset = 0f
            component.repaint()
        }
    }

    private fun updatePhysics() {
        val state = PetPersistentState.getInstance()

        if (state.isPaused) {
            isWalking = false
            walkCycle = 0f
            jumpOffset = 0f
            jumpVelocity = 0f
            return
        }

        tick++

        // Time-based coding XP & flow milestone tracker (runs every ~1 second)
        val now = System.currentTimeMillis()
        if (now - lastOneSecondTickMs >= 1000L) {
            lastOneSecondTickMs = now
            val (leveledUp, bonusXp) = state.recordCodingTimeTick(1)
            if (bonusXp > 0) {
                particleManager.spawnSparkle(posX + 6f, (component.height / 2f) - 4f)
                if (bonusXp >= 25L) {
                    showCheerBubble("🔥 Flow State Bonus! +$bonusXp XP for sustained coding!", state.getPetName())
                }
                if (leveledUp) {
                    showLevelUpBalloon(state.level, state.getPetName())
                }
            }

            // Pomodoro countdown
            if (isPomodoroActive) {
                val elapsed = now - pomodoroStartMs
                if (elapsed >= pomodoroDurationMs) {
                    isPomodoroActive = false
                    state.completedPomodoros += 1
                    val pomoLeveledUp = state.addXp(50)
                    repeat(10) {
                        particleManager.spawnSparkle(posX + (it * 3f - 10f), component.height / 2f - 2f)
                        particleManager.spawnHeart(posX + (it * 3f - 10f), component.height / 2f - 4f)
                    }
                    showCheerBubble("☕ 25-Minute Pomodoro Completed!\n+50 XP earned! Great focus, time for a 5m break!", state.getPetName())
                    if (pomoLeveledUp) {
                        showLevelUpBalloon(state.level, state.getPetName())
                    }
                }
            }
        }

        // Check if mouse petting timed out (if mouse stopped moving)
        if (isMousePetting && (now - lastMousePettingTime > 500L)) {
            stopMousePetting()
        }

        // Jumps
        if (jumpOffset > 0 || jumpVelocity > 0) {
            jumpOffset += jumpVelocity
            jumpVelocity -= 0.6f
            if (jumpOffset <= 0f) {
                jumpOffset = 0f
                jumpVelocity = 0f
            }
        }

        // Temporary states timeout
        if (temporaryState != null && now > temporaryStateEndTime) {
            temporaryState = null
        }

        // If being actively petted by cursor, pause roaming and purr!
        if (isMousePetting) {
            isWalking = false
            particleManager.update()
            return
        }

        // Sleeping
        if (isAsleep()) {
            isWalking = false
            if (tick % 35L == 0L) {
                particleManager.spawnZzz(posX + 4f, (component.height / 2f) - 6f)
            }
            particleManager.update()
            return
        }

        // Celebrating
        if (temporaryState == PetActivityState.CELEBRATING) {
            isWalking = false
            if (tick % 10L == 0L) {
                particleManager.spawnSparkle(posX + (Math.random().toFloat() * 16f - 8f), (component.height / 2f) - 4f)
                particleManager.spawnHeart(posX + (Math.random().toFloat() * 12f - 6f), (component.height / 2f) - 6f)
            }
            particleManager.update()
            return
        }

        // Coding
        if (temporaryState == PetActivityState.CODING) {
            isWalking = false
            if (tick % 18L == 0L) {
                particleManager.spawnSparkle(posX + 8f, (component.height / 2f) - 4f)
            }
            particleManager.update()
            return
        }

        // Watching tests (Stadium mode)
        if (temporaryState == PetActivityState.WATCHING_TESTS) {
            isWalking = false
            if (tick % 14L == 0L) {
                particleManager.spawnSweat(posX + 4f, (component.height / 2f) - 6f)
            }
            particleManager.update()
            return
        }

        // Stadium victory (100% test pass)
        if (temporaryState == PetActivityState.STADIUM_VICTORY) {
            isWalking = false
            if (tick % 8L == 0L) {
                particleManager.spawnConfetti(posX, component.height / 2f)
                particleManager.spawnSparkle(posX + (Math.random().toFloat() * 12f - 6f), (component.height / 2f) - 4f)
            }
            particleManager.update()
            return
        }

        // Fainted or Meme Warning or Scrunching Paper
        if (temporaryState == PetActivityState.FAINTED || temporaryState == PetActivityState.MEME_WARNING || temporaryState == PetActivityState.SCRUNCHING_PAPER) {
            isWalking = false
            particleManager.update()
            return
        }

        // Roaming with configured speed
        val minX = 12f
        val maxX = JBUI.scale(62).toFloat()
        val speed = state.getSpeed().walkSpeed

        val dist = targetX - posX
        if (abs(dist) > 1.0f) {
            isWalking = true
            facingRight = dist > 0
            posX += if (facingRight) speed else -speed
            walkCycle += speed * 0.7f
        } else {
            isWalking = false
            walkCycle = 0f

            // Rest pauses
            if (tick % 90L == 0L && Math.random() < 0.5) {
                targetX = minX + (Math.random().toFloat() * (maxX - minX))
            }
        }

        particleManager.update()
    }

    private fun onLiveTyping(charsCount: Int) {
        val state = PetPersistentState.getInstance()
        if (state.isPaused) return

        wakeUp()
        temporaryState = PetActivityState.CODING
        temporaryStateEndTime = System.currentTimeMillis() + 1400L
        component.repaint()

        // 45-minute continuous focus hydration reminder
        val now = System.currentTimeMillis()
        if (now - continuousCodingStartMs > 45 * 60 * 1000L && now - lastHealthReminderMs > 30 * 60 * 1000L) {
            lastHealthReminderMs = now
            showCheerBubble("💧 Take a sip of water & stretch! You've been coding intensely for 45m!", state.getPetName())
        }
    }

    private fun onDeletion(deletedLen: Int) {
        val state = PetPersistentState.getInstance()
        if (state.isPaused) return

        wakeUp()
        particleManager.spawnPaperBall(posX, component.height / 2f)
        triggerTempState(PetActivityState.SCRUNCHING_PAPER, 1500L)
    }

    private fun onSaved() {
        val state = PetPersistentState.getInstance()
        if (state.isPaused) return

        showSaveStamp = true
        saveStampEndTime = System.currentTimeMillis() + 1500L
        component.repaint()
    }

    private fun onWatchingTests(runName: String) {
        val state = PetPersistentState.getInstance()
        if (state.isPaused) return

        wakeUp()
        triggerTempState(PetActivityState.WATCHING_TESTS, 7000L)
        repeat(3) {
            particleManager.spawnSweat(posX + 4f, component.height / 2f - 6f)
        }
        statusBar?.setInfo("🏟️ ${state.getPetName()} is biting nails watching your test suite run!")
    }

    private fun onFridayPushMeme() {
        val state = PetPersistentState.getInstance()
        if (state.isPaused) return

        wakeUp()
        triggerTempState(PetActivityState.MEME_WARNING, 4500L)
        showCheerBubble("⚠️ Pushing on a Friday afternoon?!\nLiving dangerously! 🚀", state.getPetName())
    }

    private fun onMonsterDiffMeme(fileCount: Int) {
        val state = PetPersistentState.getInstance()
        if (state.isPaused) return

        wakeUp()
        triggerTempState(PetActivityState.MEME_WARNING, 4500L)
        showCheerBubble("😱 $fileCount files in one commit!\nPraying for your code reviewers! 🙏", state.getPetName())
    }

    private fun onLateNightMeme() {
        val state = PetPersistentState.getInstance()
        if (state.isPaused) return

        showCheerBubble("🦉 Late night code grind!\nDon't forget to push before you sleep! ☕", state.getPetName())
    }

    private fun onCommitCelebration(commitMsg: String, leveledUp: Boolean) {
        val state = PetPersistentState.getInstance()
        val petName = state.getPetName()

        if (!state.isPaused) {
            wakeUp()
            jumpVelocity = 6.5f
            repeat(8) {
                particleManager.spawnSparkle(posX + (it * 3f - 10f), component.height / 2f - 2f)
                particleManager.spawnHeart(posX + (it * 3f - 10f), component.height / 2f - 4f)
            }
            triggerTempState(PetActivityState.CELEBRATING, 3500L)
        }

        val summary = commitMsg.lines().firstOrNull()?.take(40) ?: "Great commit"
        showCheerBubble("🎉 Commit Pushed! +50 XP\n\"$summary\"", petName)

        if (leveledUp) {
            showLevelUpBalloon(state.level, petName)
        }
    }

    private fun onRunFinished(runName: String, isSuccess: Boolean, isTest: Boolean) {
        val state = PetPersistentState.getInstance()
        val petName = state.getPetName()
        if (isTest) {
            if (isSuccess) {
                if (!state.isPaused) {
                    wakeUp()
                    jumpVelocity = 6.0f
                    repeat(12) {
                        particleManager.spawnConfetti(posX, component.height / 2f)
                    }
                    triggerTempState(PetActivityState.STADIUM_VICTORY, 3800L)
                }
                val cleanName = runName.take(28)
                showCheerBubble("🏟️ 100% TESTS PASSING! (+25 XP)\n\"$cleanName\"\nHigh five! Green across the board! 🟢", petName)
            } else {
                if (!state.isPaused) {
                    wakeUp()
                    triggerTempState(PetActivityState.FAINTED, 3500L)
                }
                val cleanName = runName.take(28)
                showCheerBubble("🟥 TEST SUITE FAILED!\n\"$cleanName\"\nRef says check the traceback! 🔧", petName)
            }
        } else {
            if (isSuccess) {
                if (!state.isPaused) {
                    wakeUp()
                    jumpVelocity = 4.5f
                    repeat(5) {
                        particleManager.spawnSparkle(posX + (it * 3f - 6f), component.height / 2f - 2f)
                    }
                    triggerTempState(PetActivityState.CELEBRATING, 2000L)
                }
                val cleanName = runName.take(28)
                showCheerBubble("⚡ Run Succeeded! (+10 XP)\n\"$cleanName\"", petName)
            } else {
                val cleanName = runName.take(28)
                showCheerBubble("🔧 Build Failed:\n\"$cleanName\"\nDon't worry, let's debug it together!", petName)
            }
        }
    }

    private fun showLevelUpBalloon(newLevel: Int, petName: String) {
        val state = PetPersistentState.getInstance()
        val petType = state.getSelectedPetType()
        val cheer = petType.cheerPhrases.random()

        particleManager.spawnSparkle(posX, component.height / 2f)
        particleManager.spawnHeart(posX + 5f, component.height / 2f)

        val unlockedAcc = PetAccessory.entries.find { it.unlockLevel == newLevel }
        val accMsg = if (unlockedAcc != null) "\n🎁 Unlocked: ${unlockedAcc.displayName}!" else ""

        runCatching {
            NotificationGroupManager.getInstance()
                .getNotificationGroup("Pet Notifications")
                .createNotification(
                    "🎉 $petName Leveled Up! (Level $newLevel)$accMsg",
                    "$cheer ✨",
                    NotificationType.INFORMATION
                )
                .notify(project)
        }
    }

    private fun showPetMenu(invoker: Component, x: Int, y: Int) {
        val state = PetPersistentState.getInstance()
        val petType = state.getSelectedPetType()
        val name = state.getPetName()
        val rank = state.getDeveloperRank()
        val menu = JPopupMenu()

        // Header
        val header = JMenuItem("🐾 $name (${petType.displayName}) - $rank [Lv. ${state.level}]").apply {
            isEnabled = false
            font = font.deriveFont(Font.BOLD)
        }
        menu.add(header)
        menu.add(JSeparator())

        // 1. Achievements & Milestones
        val achItem = JMenuItem("🏆 Achievements & Milestones...").apply {
            addActionListener {
                AchievementsDialog(project).show()
            }
        }
        menu.add(achItem)

        // 3. Pomodoro Focus Sprint
        val pomoTitle = if (isPomodoroActive) {
            val remainingSec = ((pomodoroDurationMs - (System.currentTimeMillis() - pomodoroStartMs)) / 1000L).coerceAtLeast(0)
            val m = remainingSec / 60
            val s = remainingSec % 60
            "⏹️ Stop Pomodoro Focus (${m}m ${s}s left)"
        } else {
            "⏱️ Start 25m Pomodoro Focus Sprint"
        }
        val pomoItem = JMenuItem(pomoTitle).apply {
            addActionListener {
                if (isPomodoroActive) {
                    isPomodoroActive = false
                    statusBar?.setInfo("Pomodoro focus sprint stopped.")
                } else {
                    isPomodoroActive = true
                    pomodoroStartMs = System.currentTimeMillis()
                    statusBar?.setInfo("⏱️ 25m Pomodoro Focus sprint started with $name!")
                    showCheerBubble("⏱️ 25m Focus Sprint started!\nStay in the flow zone!", name)
                }
            }
        }
        menu.add(pomoItem)
        menu.add(JSeparator())

        // 4. Feed Treat Action
        val feedItem = JMenuItem("${petType.treatEmoji} Feed ${petType.treatName} (+XP)").apply {
            addActionListener {
                if (state.isPaused) return@addActionListener
                wakeUp()
                val (leveledUp, bonusXp) = state.feed()
                jumpVelocity = 3.5f
                repeat(3) {
                    particleManager.spawnSparkle(posX + (it * 4f - 4f), component.height / 2f)
                }
                triggerTempState(PetActivityState.EATING, 1800)
                statusBar?.setInfo("Nom nom nom! $name enjoyed the ${petType.treatName}! (+$bonusXp XP)")
                if (leveledUp) {
                    showLevelUpBalloon(state.level, name)
                }
            }
        }
        menu.add(feedItem)

        // 5. Cheer Me Up
        val cheerItem = JMenuItem("✨ Cheer Me Up!").apply {
            addActionListener {
                if (!state.isPaused) {
                    wakeUp()
                    jumpVelocity = 5.0f
                    repeat(5) {
                        particleManager.spawnSparkle(posX + (it * 4f - 8f), component.height / 2f)
                        particleManager.spawnHeart(posX + (it * 3f - 6f), component.height / 2f - 6f)
                    }
                }
                val cheer = petType.cheerPhrases.random()
                showCheerBubble(cheer, name)
            }
        }
        menu.add(cheerItem)

        menu.add(JSeparator())

        // 6. Animation Speed Controller Submenu
        val speedMenu = JMenu("⏱️ Animation Speed")
        val currentSpeed = state.getSpeed()
        AnimationSpeed.entries.forEach { speed ->
            val isCurrent = speed == currentSpeed
            val prefix = if (isCurrent) "✓ " else "   "
            val item = JMenuItem("$prefix${speed.displayName}").apply {
                addActionListener {
                    state.setSpeed(speed)
                    animationTimer.delay = speed.delayMs
                    statusBar?.setInfo("Set animation speed to: ${speed.displayName}")
                    component.repaint()
                }
            }
            speedMenu.add(item)
        }
        menu.add(speedMenu)

        // 7. Unlockable Accessories Submenu
        val accMenu = JMenu("🎩 Accessories (Level Unlocks)")
        PetAccessory.entries.forEach { acc ->
            val unlocked = state.isAccessoryUnlocked(acc)
            val active = state.isAccessoryActive(acc)
            val title = if (unlocked) {
                (if (active) "✓ " else "   ") + acc.displayName
            } else {
                "🔒 ${acc.displayName} (Unlocks at Lv. ${acc.unlockLevel})"
            }

            val item = JMenuItem(title).apply {
                isEnabled = unlocked
                addActionListener {
                    state.toggleAccessory(acc)
                    component.repaint()
                    val statusText = if (state.isAccessoryActive(acc)) "Equipped" else "Unequipped"
                    statusBar?.setInfo("$statusText ${acc.displayName}!")
                }
            }
            accMenu.add(item)
        }
        menu.add(accMenu)

        // 8. Freeze / Pause Movement
        val pauseItem = JMenuItem(
            if (state.isPaused) "▶️ Resume Movement" else "⏸️ Freeze / Pause Movement"
        ).apply {
            addActionListener {
                state.isPaused = !state.isPaused
                if (state.isPaused) {
                    animationTimer.stop()
                    isWalking = false
                    walkCycle = 0f
                    jumpOffset = 0f
                    jumpVelocity = 0f
                    isMousePetting = false
                    temporaryState = null
                    particleManager.clear()
                    component.repaint()
                    statusBar?.setInfo("⏸️ $name is completely frozen (Absolute stillness mode).")
                } else {
                    wakeUp()
                    animationTimer.delay = state.getSpeed().delayMs
                    animationTimer.start()
                    component.repaint()
                    statusBar?.setInfo("▶️ $name is active and moving again!")
                }
            }
        }
        menu.add(pauseItem)

        // 10. Switch Companion
        val switchMenu = JMenu("🔄 Switch Companion")
        PetType.entries.forEach { type ->
            val isCurrent = type == petType
            val prefix = if (isCurrent) "✓ " else "   "
            val item = JMenuItem("$prefix${type.treatEmoji} ${type.displayName}").apply {
                addActionListener {
                    state.setSelectedPetType(type)
                    if (!state.isPaused) {
                        wakeUp()
                    }
                    component.repaint()
                    statusBar?.setInfo("Switched companion to ${state.getPetName()} the ${type.displayName}!")
                }
            }
            switchMenu.add(item)
        }
        menu.add(switchMenu)

        menu.show(invoker, x, y - menu.preferredSize.height)
    }

    private fun showCheerBubble(cheerText: String, petName: String) {
        val bubblePanel = JPanel(BorderLayout()).apply {
            border = JBUI.Borders.compound(
                JBUI.Borders.customLine(JBColor(Color(0xFA, 0x82, 0x31), Color(0xF3, 0x9C, 0x12)), 1),
                JBUI.Borders.empty(8, 12)
            )
            background = JBColor(Color(0xFF, 0xFA, 0xF0), Color(0x2B, 0x2D, 0x30))

            val formattedText = cheerText.replace("\n", "<br/>")
            val label = JLabel("<html><b>🐾 $petName:</b><br/>$formattedText</html>").apply {
                font = Font("Segoe UI", Font.PLAIN, 12)
                foreground = JBColor(Color(0x2C, 0x34, 0x37), Color(0xEB, 0xBC, 0x71))
            }
            add(label, BorderLayout.CENTER)
        }

        val popup = JBPopupFactory.getInstance()
            .createComponentPopupBuilder(bubblePanel, null)
            .setRequestFocus(false)
            .setCancelOnClickOutside(true)
            .setCancelOnOtherWindowOpen(true)
            .createPopup()

        val locationOnScreen = runCatching { component.locationOnScreen }.getOrNull()
        if (locationOnScreen != null) {
            val point = RelativePoint(
                component,
                Point(0, -bubblePanel.preferredSize.height - 8)
            )
            popup.show(point)
        }

        statusBar?.setInfo("🐾 $petName: \"${cheerText.lines().firstOrNull()}\"")
    }

    private fun triggerTempState(newState: PetActivityState, durationMs: Long) {
        temporaryState = newState
        temporaryStateEndTime = System.currentTimeMillis() + durationMs
        component.repaint()
    }
}
