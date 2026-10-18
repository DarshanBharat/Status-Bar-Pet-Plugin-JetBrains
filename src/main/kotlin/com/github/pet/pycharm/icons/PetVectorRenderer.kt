package com.github.pet.pycharm.icons

import com.github.pet.pycharm.models.PetAccessory
import com.github.pet.pycharm.models.PetActivityState
import com.github.pet.pycharm.models.PetType
import com.intellij.ui.JBColor
import java.awt.*
import java.awt.geom.*
import kotlin.math.cos
import kotlin.math.sin

object PetVectorRenderer {

    // Palette
    private val CAT_ORANGE = Color(0xF5, 0x82, 0x20)
    private val CAT_WHITE = Color(0xFD, 0xFD, 0xFD)
    private val CAT_INNER_EAR = Color(0xFF, 0x8A, 0xA5)

    private val DOG_BODY = Color(0xEA, 0x8A, 0x1A)
    private val DOG_EAR = Color(0xC7, 0x6E, 0x08)
    private val DOG_WHITE = Color(0xFD, 0xFD, 0xFD)
    private val DOG_TONGUE = Color(0xFF, 0x6B, 0x81)

    private val DUCK_YELLOW = Color(0xFF, 0xD4, 0x26)
    private val DUCK_BEAK = Color(0xFF, 0x76, 0x1E)
    private val DUCK_BEAK_SHADOW = Color(0xD8, 0x58, 0x0B)
    private val DUCK_WING = Color(0xF2, 0xBF, 0x11)

    private val EYE_COLOR = Color(0x1E, 0x27, 0x2C)
    private val EYE_SHINE = Color(0xFF, 0xFF, 0xFF)
    private val NOSE_BLACK = Color(0x24, 0x2B, 0x2E)

    // Laptop colors
    private val LAPTOP_BODY = Color(0x3E, 0x47, 0x51)
    private val LAPTOP_SCREEN = Color(0x00, 0xE5, 0xD8)
    private val LAPTOP_KEYBOARD = Color(0x1A, 0x20, 0x26)

    // Accessory colors
    private val HEADPHONES_BAND = Color(0x2D, 0x34, 0x36)
    private val HEADPHONES_CUP = Color(0x00, 0xD2, 0xD3)
    private val GLASSES_FRAME = Color(0x22, 0x22, 0x22)
    private val HAT_PURPLE = Color(0x6C, 0x5C, 0xE7)
    private val HAT_GOLD = Color(0xFD, 0xCB, 0x6E)
    private val CROWN_GOLD = Color(0xFF, 0xD3, 0x2A)
    private val CROWN_RUBY = Color(0xFF, 0x47, 0x57)

    private val SLEEP_MAT = Color(0x70, 0xA1, 0xFF, 0x88)
    private val SHADOW_COLOR = Color(0, 0, 0, 30)

    fun renderPet(
        g2: Graphics2D,
        petType: PetType,
        state: PetActivityState,
        tick: Long,
        walkCycle: Float,
        facingRight: Boolean,
        jumpOffset: Float = 0f,
        isPaused: Boolean = false,
        activeAccessories: Set<String> = emptySet(),
        showSaveStamp: Boolean = false
    ) {
        val origTransform = g2.transform
        val origHints = g2.renderingHints

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE)

        val effectiveJump = if (isPaused) 0f else jumpOffset
        val effectiveWalk = if (isPaused) 0f else walkCycle

        // Ground shadow
        val shadowW = if (state == PetActivityState.SLEEPING) 22f else 18f
        g2.color = SHADOW_COLOR
        g2.fill(Ellipse2D.Float(-shadowW / 2, 7.5f, shadowW, 3.5f))

        // Sleeping mat
        if (state == PetActivityState.SLEEPING && !isPaused) {
            g2.color = SLEEP_MAT
            g2.fill(RoundRectangle2D.Float(-12f, 5.5f, 24f, 4.5f, 4f, 4f))
        }

        g2.translate(0.0, -effectiveJump.toDouble())
        if (!facingRight) {
            g2.scale(-1.0, 1.0)
        }

        when (petType) {
            PetType.CAT -> drawCat(g2, state, tick, effectiveWalk, isPaused)
            PetType.DOG -> drawDog(g2, state, tick, effectiveWalk, isPaused)
            PetType.DUCK -> drawDuck(g2, state, tick, effectiveWalk, isPaused)
        }

        // Render Accessories over Pet
        if (state != PetActivityState.SLEEPING && !isPaused) {
            drawAccessories(g2, activeAccessories, petType, tick)
        }

        // Draw Mini Laptop if coding
        if (state == PetActivityState.CODING && !isPaused) {
            drawLaptop(g2, tick)
        }

        // Render Stadium, Meme, Wastebasket & Save Stamp props
        if (!isPaused) {
            when (state) {
                PetActivityState.STADIUM_VICTORY -> drawFoamFinger(g2)
                PetActivityState.FAINTED -> drawRedCard(g2)
                PetActivityState.MEME_WARNING -> drawHazardSign(g2)
                PetActivityState.SCRUNCHING_PAPER -> drawWastebasket(g2)
                else -> {}
            }
            if (showSaveStamp) {
                drawSaveStamp(g2, tick)
            }
        }

        g2.transform = origTransform
        g2.setRenderingHints(origHints)
    }

    // =========================================================================
    // DOG (SPARKY) - Clean Kawaii Corgi Proportions
    // =========================================================================
    private fun drawDog(g2: Graphics2D, state: PetActivityState, tick: Long, walkCycle: Float, isPaused: Boolean) {
        val breathing = if (isPaused) 0f else sin(tick * 0.12).toFloat() * 0.5f
        val tailSwing = if (isPaused) 0f else when (state) {
            PetActivityState.CODING -> sin(tick * 0.55).toFloat() * 26f
            PetActivityState.SLEEPING -> sin(tick * 0.05).toFloat() * 4f
            PetActivityState.CELEBRATING -> sin(tick * 0.7).toFloat() * 32f
            else -> sin(tick * 0.2).toFloat() * 16f
        }

        if (state == PetActivityState.SLEEPING && !isPaused) {
            // Sleeping Dog (curled up)
            g2.color = DOG_BODY
            g2.fill(RoundRectangle2D.Float(-9f, -1f + breathing, 18f, 9f, 6f, 6f))

            // Floppy sleeping ear
            g2.color = DOG_EAR
            g2.fill(RoundRectangle2D.Float(-8f, -2f + breathing, 4f, 7f, 3f, 3f))

            // White muzzle patch
            g2.color = DOG_WHITE
            g2.fill(Ellipse2D.Float(1f, 1f + breathing, 6f, 4f))

            // Sleeping eye (- -)
            g2.color = EYE_COLOR
            g2.stroke = BasicStroke(1.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)
            g2.drawLine(2, 2, 5, 2)

            // Button nose
            g2.color = NOSE_BLACK
            g2.fill(Ellipse2D.Float(5.5f, 2f, 2f, 1.8f))
            return
        }

        // 1. Wagging Tail (Behind body)
        g2.color = DOG_BODY
        g2.stroke = BasicStroke(2.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)
        val tailPath = GeneralPath()
        tailPath.moveTo(-6.0, 3.0)
        tailPath.quadTo(-10.0 - tailSwing * 0.1, 0.0, -9.0 + tailSwing * 0.2, -5.0 + tailSwing * 0.1)
        g2.draw(tailPath)

        // 2. Walking Legs
        val legBob = if (walkCycle != 0f) sin(walkCycle).toFloat() * 1.8f else 0f
        g2.color = DOG_WHITE
        g2.fill(RoundRectangle2D.Float(-5f, 5.5f + legBob, 3.5f, 3.5f, 1.8f, 1.8f))
        g2.fill(RoundRectangle2D.Float(1.5f, 5.5f - legBob, 3.5f, 3.5f, 1.8f, 1.8f))

        // 3. Body
        g2.color = DOG_BODY
        val bodyY = -3f + breathing - (if (walkCycle != 0f) kotlin.math.abs(legBob) * 0.4f else 0f)
        g2.fill(RoundRectangle2D.Float(-7f, bodyY, 14f, 10f, 6f, 6f))

        // White chest fluff
        g2.color = DOG_WHITE
        g2.fill(Ellipse2D.Float(-2f, bodyY + 3f, 6f, 6f))

        // 4. Head (Proportional & Centered)
        val headY = bodyY - 7f
        g2.color = DOG_BODY
        g2.fill(RoundRectangle2D.Float(-6f, headY, 13f, 9.5f, 5f, 5f))

        // Floppy Ears (left & right flanking the head)
        val earBounce = if (!isPaused && walkCycle != 0f) sin(walkCycle * 2).toFloat() * 1.2f else 0f
        g2.color = DOG_EAR
        g2.fill(RoundRectangle2D.Float(-8.5f, headY + earBounce + 1f, 3.5f, 7.5f, 2.5f, 2.5f))
        g2.fill(RoundRectangle2D.Float(5.5f, headY + earBounce + 1f, 3.5f, 7.5f, 2.5f, 2.5f))

        // 5. White Muzzle (Centered at bottom half of face, BELOW eyes!)
        g2.color = DOG_WHITE
        g2.fill(RoundRectangle2D.Float(-1.5f, headY + 4f, 6f, 4.5f, 3f, 3f))

        // Centered Cute Black Button Nose
        g2.color = NOSE_BLACK
        g2.fill(Ellipse2D.Float(0.5f, headY + 4.2f, 2.2f, 1.8f))

        // 6. Eyes (Positioned ABOVE the muzzle, cleanly spaced!)
        if (!isPaused && (state == PetActivityState.CODING || state == PetActivityState.PETTING || state == PetActivityState.CELEBRATING || state == PetActivityState.STADIUM_VICTORY)) {
            // Happy smiling curved eyes (⌒ ⌒)
            g2.color = EYE_COLOR
            g2.stroke = BasicStroke(1.3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)
            g2.drawArc(-4, headY.toInt() + 2, 3, 3, 0, 180)
            g2.drawArc(2, headY.toInt() + 2, 3, 3, 0, 180)

            // Centered Cute Panting Tongue!
            g2.color = DOG_TONGUE
            val tongueBob = sin(tick * 0.5).toFloat() * 0.8f
            g2.fill(RoundRectangle2D.Float(0.5f, headY + 6.8f, 2.2f, 3f + tongueBob, 1.2f, 1.2f))
        } else if (!isPaused && state == PetActivityState.FAINTED) {
            // X_X eyes
            g2.color = EYE_COLOR
            g2.stroke = BasicStroke(1.3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)
            g2.drawLine(-5, headY.toInt() + 1, -2, headY.toInt() + 4)
            g2.drawLine(-2, headY.toInt() + 1, -5, headY.toInt() + 4)
            g2.drawLine(2, headY.toInt() + 1, 5, headY.toInt() + 4)
            g2.drawLine(5, headY.toInt() + 1, 2, headY.toInt() + 4)
        } else if (!isPaused && state == PetActivityState.WATCHING_TESTS) {
            // Nervous / intense squinting eyes (> <)
            g2.color = EYE_COLOR
            g2.stroke = BasicStroke(1.3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)
            g2.drawLine(-5, headY.toInt() + 1, -2, headY.toInt() + 3)
            g2.drawLine(-5, headY.toInt() + 5, -2, headY.toInt() + 3)
            g2.drawLine(5, headY.toInt() + 1, 2, headY.toInt() + 3)
            g2.drawLine(5, headY.toInt() + 5, 2, headY.toInt() + 3)
        } else if (!isPaused && state == PetActivityState.MEME_WARNING) {
            // Shock anime eyes (°ロ°)
            g2.color = EYE_COLOR
            g2.fill(Ellipse2D.Float(-5f, headY + 1.2f, 3.8f, 4.2f))
            g2.fill(Ellipse2D.Float(1.5f, headY + 1.2f, 3.8f, 4.2f))
            g2.color = EYE_SHINE
            g2.fill(Ellipse2D.Float(-4f, headY + 1.8f, 1.8f, 1.8f))
            g2.fill(Ellipse2D.Float(2.5f, headY + 1.8f, 1.8f, 1.8f))
            g2.color = NOSE_BLACK
            g2.drawOval(0, headY.toInt() + 6, 3, 3)
        } else {
            // Big shiny curious puppy eyes
            val blink = !isPaused && (tick % 100L in 95L..99L)
            if (blink) {
                g2.color = EYE_COLOR
                g2.stroke = BasicStroke(1.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)
                g2.drawLine(-4, headY.toInt() + 3, -2, headY.toInt() + 3)
                g2.drawLine(2, headY.toInt() + 3, 4, headY.toInt() + 3)
            } else {
                g2.color = EYE_COLOR
                g2.fill(Ellipse2D.Float(-4.5f, headY + 1.8f, 3f, 3.5f))
                g2.fill(Ellipse2D.Float(1.8f, headY + 1.8f, 3f, 3.5f))

                g2.color = EYE_SHINE
                g2.fill(Ellipse2D.Float(-3.8f, headY + 2.2f, 1.2f, 1.2f))
                g2.fill(Ellipse2D.Float(2.5f, headY + 2.2f, 1.2f, 1.2f))
            }
        }

        // Tapping paws when coding
        if (state == PetActivityState.CODING && !isPaused) {
            val tapL = sin(tick * 0.8).toFloat() * 1.8f
            val tapR = cos(tick * 0.8).toFloat() * 1.8f
            g2.color = DOG_WHITE
            g2.fill(Ellipse2D.Float(1f, bodyY + 3.5f - tapL, 3.5f, 2.8f))
            g2.fill(Ellipse2D.Float(4.5f, bodyY + 3.5f - tapR, 3.5f, 2.8f))
        }
    }

    // =========================================================================
    // CAT (MOCHI) - Clean Rounded Tabby
    // =========================================================================
    private fun drawCat(g2: Graphics2D, state: PetActivityState, tick: Long, walkCycle: Float, isPaused: Boolean) {
        val breathing = if (isPaused) 0f else sin(tick * 0.12).toFloat() * 0.5f
        val tailSwing = if (isPaused) 0f else when (state) {
            PetActivityState.CODING -> sin(tick * 0.45).toFloat() * 22f
            PetActivityState.SLEEPING -> sin(tick * 0.05).toFloat() * 4f
            PetActivityState.CELEBRATING -> sin(tick * 0.6).toFloat() * 30f
            else -> sin(tick * 0.18).toFloat() * 14f
        }

        if (state == PetActivityState.SLEEPING && !isPaused) {
            g2.color = CAT_ORANGE
            g2.fill(RoundRectangle2D.Float(-9f, -1f + breathing, 18f, 9f, 6f, 6f))

            val leftEar = Polygon(intArrayOf(-8, -5, -3), intArrayOf(-1, -5, -1), 3)
            val rightEar = Polygon(intArrayOf(-1, 2, 4), intArrayOf(-1, -5, -1), 3)
            g2.fill(leftEar)
            g2.fill(rightEar)

            g2.color = EYE_COLOR
            g2.stroke = BasicStroke(1.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)
            g2.drawLine(-6, 2, -3, 2)
            g2.drawLine(-1, 2, 2, 2)
            return
        }

        // Tail
        g2.color = CAT_ORANGE
        g2.stroke = BasicStroke(2.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)
        val tailPath = GeneralPath()
        tailPath.moveTo(-6.0, 3.0)
        tailPath.quadTo(-11.0 - tailSwing * 0.1, -1.0, -10.0 + tailSwing * 0.2, -7.0 + tailSwing * 0.1)
        g2.draw(tailPath)

        // Legs
        val legBob = if (walkCycle != 0f) sin(walkCycle).toFloat() * 1.8f else 0f
        g2.color = CAT_WHITE
        g2.fill(RoundRectangle2D.Float(-5f, 5.5f + legBob, 3.5f, 3.5f, 1.8f, 1.8f))
        g2.fill(RoundRectangle2D.Float(1.5f, 5.5f - legBob, 3.5f, 3.5f, 1.8f, 1.8f))

        // Body
        g2.color = CAT_ORANGE
        val bodyY = -3f + breathing - (if (walkCycle != 0f) kotlin.math.abs(legBob) * 0.4f else 0f)
        g2.fill(RoundRectangle2D.Float(-7f, bodyY, 14f, 10f, 6f, 6f))

        // White belly patch
        g2.color = CAT_WHITE
        g2.fill(Ellipse2D.Float(-2f, bodyY + 3f, 6f, 6f))

        // Head
        val headY = bodyY - 7f
        g2.color = CAT_ORANGE
        g2.fill(RoundRectangle2D.Float(-6f, headY, 13f, 9.5f, 5f, 5f))

        // Ears with pink inner ear
        val earL = Polygon(intArrayOf(-6, -3, 0), intArrayOf(headY.toInt() + 1, headY.toInt() - 4, headY.toInt() + 1), 3)
        val earR = Polygon(intArrayOf(1, 4, 7), intArrayOf(headY.toInt() + 1, headY.toInt() - 4, headY.toInt() + 1), 3)
        g2.fill(earL)
        g2.fill(earR)

        g2.color = CAT_INNER_EAR
        val inEarL = Polygon(intArrayOf(-5, -3, -1), intArrayOf(headY.toInt() + 1, headY.toInt() - 2, headY.toInt() + 1), 3)
        val inEarR = Polygon(intArrayOf(2, 4, 6), intArrayOf(headY.toInt() + 1, headY.toInt() - 2, headY.toInt() + 1), 3)
        g2.fill(inEarL)
        g2.fill(inEarR)

        // Eyes
        if (!isPaused && (state == PetActivityState.CODING || state == PetActivityState.PETTING || state == PetActivityState.CELEBRATING || state == PetActivityState.STADIUM_VICTORY)) {
            g2.color = EYE_COLOR
            g2.stroke = BasicStroke(1.3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)
            g2.drawArc(-4, headY.toInt() + 2, 3, 3, 0, 180)
            g2.drawArc(2, headY.toInt() + 2, 3, 3, 0, 180)
        } else if (!isPaused && state == PetActivityState.FAINTED) {
            g2.color = EYE_COLOR
            g2.stroke = BasicStroke(1.3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)
            g2.drawLine(-5, headY.toInt() + 1, -2, headY.toInt() + 4)
            g2.drawLine(-2, headY.toInt() + 1, -5, headY.toInt() + 4)
            g2.drawLine(2, headY.toInt() + 1, 5, headY.toInt() + 4)
            g2.drawLine(5, headY.toInt() + 1, 2, headY.toInt() + 4)
        } else if (!isPaused && state == PetActivityState.WATCHING_TESTS) {
            g2.color = EYE_COLOR
            g2.stroke = BasicStroke(1.3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)
            g2.drawLine(-5, headY.toInt() + 1, -2, headY.toInt() + 3)
            g2.drawLine(-5, headY.toInt() + 5, -2, headY.toInt() + 3)
            g2.drawLine(5, headY.toInt() + 1, 2, headY.toInt() + 3)
            g2.drawLine(5, headY.toInt() + 5, 2, headY.toInt() + 3)
        } else if (!isPaused && state == PetActivityState.MEME_WARNING) {
            g2.color = EYE_COLOR
            g2.fill(Ellipse2D.Float(-5f, headY + 1.2f, 3.8f, 4.2f))
            g2.fill(Ellipse2D.Float(1.5f, headY + 1.2f, 3.8f, 4.2f))
            g2.color = EYE_SHINE
            g2.fill(Ellipse2D.Float(-4f, headY + 1.8f, 1.8f, 1.8f))
            g2.fill(Ellipse2D.Float(2.5f, headY + 1.8f, 1.8f, 1.8f))
        } else {
            val blink = !isPaused && (tick % 90L in 85L..89L)
            if (blink) {
                g2.color = EYE_COLOR
                g2.stroke = BasicStroke(1.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)
                g2.drawLine(-4, headY.toInt() + 3, -2, headY.toInt() + 3)
                g2.drawLine(2, headY.toInt() + 3, 4, headY.toInt() + 3)
            } else {
                g2.color = EYE_COLOR
                g2.fill(Ellipse2D.Float(-4.5f, headY + 1.8f, 3f, 3.5f))
                g2.fill(Ellipse2D.Float(1.8f, headY + 1.8f, 3f, 3.5f))

                g2.color = EYE_SHINE
                g2.fill(Ellipse2D.Float(-3.8f, headY + 2.2f, 1.2f, 1.2f))
                g2.fill(Ellipse2D.Float(2.5f, headY + 2.2f, 1.2f, 1.2f))
            }
        }

        // Cute pink nose
        g2.color = CAT_INNER_EAR
        g2.fill(Ellipse2D.Float(-0.5f, headY + 4.5f, 1.8f, 1.4f))

        // Paws
        if (state == PetActivityState.CODING && !isPaused) {
            val tapL = sin(tick * 0.8).toFloat() * 1.8f
            val tapR = cos(tick * 0.8).toFloat() * 1.8f
            g2.color = CAT_WHITE
            g2.fill(Ellipse2D.Float(1f, bodyY + 3.5f - tapL, 3.5f, 2.8f))
            g2.fill(Ellipse2D.Float(4.5f, bodyY + 3.5f - tapR, 3.5f, 2.8f))
        }
    }

    // =========================================================================
    // DUCK (QUACKERS) - Crispy Classic Debugger
    // =========================================================================
    private fun drawDuck(g2: Graphics2D, state: PetActivityState, tick: Long, walkCycle: Float, isPaused: Boolean) {
        val breathing = if (isPaused) 0f else sin(tick * 0.15).toFloat() * 0.5f
        val wingFlap = if (isPaused) 0f else when (state) {
            PetActivityState.CODING -> sin(tick * 0.5).toFloat() * 2.5f
            PetActivityState.CELEBRATING -> sin(tick * 0.8).toFloat() * 5f
            else -> sin(tick * 0.18).toFloat() * 1.2f
        }

        if (state == PetActivityState.SLEEPING && !isPaused) {
            g2.color = DUCK_YELLOW
            g2.fill(Ellipse2D.Float(-8f, -1f + breathing, 16f, 9f))
            g2.fill(Ellipse2D.Float(1f, -4f + breathing, 9f, 8f))

            g2.color = DUCK_BEAK
            g2.fill(RoundRectangle2D.Float(7f, -1f + breathing, 4.5f, 3f, 1.5f, 1.5f))

            g2.color = EYE_COLOR
            g2.stroke = BasicStroke(1.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)
            g2.drawLine(3, -1, 6, -1)
            return
        }

        // Tail feather
        g2.color = DUCK_YELLOW
        val tail = Polygon(intArrayOf(-8, -5, -5), intArrayOf(-1, 2, -4), 3)
        g2.fill(tail)

        // Feet / Waddling
        val footBob = if (walkCycle != 0f) sin(walkCycle).toFloat() * 1.8f else 0f
        g2.color = DUCK_BEAK
        g2.fill(RoundRectangle2D.Float(-3.5f, 5.5f + footBob, 3.5f, 3f, 1.5f, 1.5f))
        g2.fill(RoundRectangle2D.Float(1.5f, 5.5f - footBob, 3.5f, 3f, 1.5f, 1.5f))

        // Body
        val bodyY = -3f + breathing - (if (walkCycle != 0f) kotlin.math.abs(footBob) * 0.4f else 0f)
        g2.color = DUCK_YELLOW
        g2.fill(Ellipse2D.Float(-7f, bodyY, 14f, 10f))

        // Wing
        g2.color = DUCK_WING
        g2.fill(RoundRectangle2D.Float(-3.5f, bodyY + 2f - wingFlap, 7f, 4.5f, 2.5f, 2.5f))

        // Head
        val headY = bodyY - 6.5f
        g2.color = DUCK_YELLOW
        g2.fill(Ellipse2D.Float(-0.5f, headY, 10f, 9f))

        // Orange Bill / Beak
        g2.color = DUCK_BEAK
        g2.fill(RoundRectangle2D.Float(6.5f, headY + 3.5f, 6.5f, 3.5f, 1.8f, 1.8f))
        g2.color = DUCK_BEAK_SHADOW
        g2.drawLine(7, headY.toInt() + 5, 12, headY.toInt() + 5)

        // Eye
        if (!isPaused && (state == PetActivityState.CODING || state == PetActivityState.PETTING || state == PetActivityState.CELEBRATING || state == PetActivityState.STADIUM_VICTORY)) {
            g2.color = EYE_COLOR
            g2.stroke = BasicStroke(1.3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)
            g2.drawArc(2, headY.toInt() + 2, 3, 3, 0, 180)
        } else if (!isPaused && state == PetActivityState.FAINTED) {
            g2.color = EYE_COLOR
            g2.stroke = BasicStroke(1.3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)
            g2.drawLine(2, headY.toInt() + 1, 5, headY.toInt() + 4)
            g2.drawLine(5, headY.toInt() + 1, 2, headY.toInt() + 4)
        } else if (!isPaused && state == PetActivityState.WATCHING_TESTS) {
            g2.color = EYE_COLOR
            g2.stroke = BasicStroke(1.3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)
            g2.drawLine(5, headY.toInt() + 1, 2, headY.toInt() + 3)
            g2.drawLine(5, headY.toInt() + 5, 2, headY.toInt() + 3)
        } else if (!isPaused && state == PetActivityState.MEME_WARNING) {
            g2.color = EYE_COLOR
            g2.fill(Ellipse2D.Float(1.5f, headY + 1.2f, 3.8f, 4.2f))
            g2.color = EYE_SHINE
            g2.fill(Ellipse2D.Float(2.5f, headY + 1.8f, 1.8f, 1.8f))
        } else {
            val blink = !isPaused && (tick % 95L in 90L..94L)
            if (blink) {
                g2.color = EYE_COLOR
                g2.stroke = BasicStroke(1.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)
                g2.drawLine(2, headY.toInt() + 3, 5, headY.toInt() + 3)
            } else {
                g2.color = EYE_COLOR
                g2.fill(Ellipse2D.Float(2f, headY + 1.8f, 3.2f, 3.5f))

                g2.color = EYE_SHINE
                g2.fill(Ellipse2D.Float(3.2f, headY + 2.2f, 1.2f, 1.2f))
            }
        }
    }

    // =========================================================================
    // ACCESSORIES (Visual Unlocks on Level Up)
    // =========================================================================
    private fun drawAccessories(g2: Graphics2D, activeAccessories: Set<String>, petType: PetType, tick: Long) {
        val headY = -10f

        // 1. Dapper Bowtie (Level 2+)
        if (activeAccessories.contains(PetAccessory.BOWTIE.id)) {
            val neckY = 0f
            g2.color = Color(0xFF, 0x47, 0x57)
            val leftWing = Polygon(intArrayOf(-1, -4, -4), intArrayOf(neckY.toInt(), neckY.toInt() - 2, neckY.toInt() + 2), 3)
            val rightWing = Polygon(intArrayOf(1, 4, 4), intArrayOf(neckY.toInt(), neckY.toInt() - 2, neckY.toInt() + 2), 3)
            g2.fill(leftWing)
            g2.fill(rightWing)
            g2.color = Color(0xD6, 0x30, 0x31)
            g2.fillOval(-1, neckY.toInt() - 1, 3, 3)
        }

        // 2. Headphones (Level 3+)
        if (activeAccessories.contains(PetAccessory.HEADPHONES.id)) {
            g2.color = HEADPHONES_BAND
            g2.stroke = BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)
            g2.drawArc(-6, headY.toInt() - 2, 12, 10, 20, 140)

            // Neon cyan ear-cups
            g2.color = HEADPHONES_CUP
            g2.fill(RoundRectangle2D.Float(-7.5f, headY + 1f, 2.5f, 5f, 1.5f, 1.5f))
            g2.fill(RoundRectangle2D.Float(5f, headY + 1f, 2.5f, 5f, 1.5f, 1.5f))
        }

        // 3. Glasses (Level 5+)
        if (activeAccessories.contains(PetAccessory.GLASSES.id)) {
            g2.color = GLASSES_FRAME
            g2.stroke = BasicStroke(1.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)
            g2.draw(RoundRectangle2D.Float(-5f, headY + 1.5f, 4f, 3.5f, 1f, 1f))
            g2.draw(RoundRectangle2D.Float(1f, headY + 1.5f, 4f, 3.5f, 1f, 1f))
            g2.drawLine(-1, headY.toInt() + 3, 1, headY.toInt() + 3) // bridge
        }

        // 4. Wizard Hat (Level 7+)
        if (activeAccessories.contains(PetAccessory.WIZARD_HAT.id)) {
            g2.color = HAT_PURPLE
            // Brim
            g2.fill(Ellipse2D.Float(-6f, headY - 1f, 12f, 3f))
            // Cone
            val cone = Polygon(intArrayOf(-4, 0, 4), intArrayOf(headY.toInt(), headY.toInt() - 8, headY.toInt()), 3)
            g2.fill(cone)
            // Gold buckle
            g2.color = HAT_GOLD
            g2.fillRect(-1, headY.toInt() - 2, 2, 2)
        }

        // 5. Pirate Captain Hat (Level 9+)
        if (activeAccessories.contains(PetAccessory.PIRATE_HAT.id)) {
            g2.color = Color(0x2D, 0x34, 0x36)
            val tricorne = Polygon(
                intArrayOf(-7, -4, 0, 4, 7, 0),
                intArrayOf(headY.toInt(), headY.toInt() - 5, headY.toInt() - 3, headY.toInt() - 5, headY.toInt(), headY.toInt() - 1),
                6
            )
            g2.fill(tricorne)
            // Gold skull / button
            g2.color = HAT_GOLD
            g2.fillOval(-1, headY.toInt() - 3, 2, 2)
        }

        // 6. Golden Crown (Level 12+)
        if (activeAccessories.contains(PetAccessory.CROWN.id)) {
            g2.color = CROWN_GOLD
            val crown = Polygon(
                intArrayOf(-4, -5, -2, 0, 2, 5, 4),
                intArrayOf(headY.toInt(), headY.toInt() - 5, headY.toInt() - 3, headY.toInt() - 6, headY.toInt() - 3, headY.toInt() - 5, headY.toInt()),
                7
            )
            g2.fill(crown)
            // Ruby gem
            g2.color = CROWN_RUBY
            g2.fill(Ellipse2D.Float(-0.8f, headY.toFloat() - 2.5f, 1.6f, 1.6f))
        }

        // 7. Angelic Halo (Level 15+)
        if (activeAccessories.contains(PetAccessory.HALO.id)) {
            val haloY = headY - 6f + sin(tick * 0.15).toFloat() * 1f
            g2.color = Color(0xFF, 0xEA, 0x00)
            g2.stroke = BasicStroke(1.4f)
            g2.drawOval(-5, haloY.toInt(), 10, 3)
        }

        // 8. Viking Horns (Level 20+)
        if (activeAccessories.contains(PetAccessory.VIKING_HELMET.id)) {
            g2.color = Color(0x7F, 0x8C, 0x8D)
            g2.fill(RoundRectangle2D.Float(-5f, headY - 1f, 10f, 3f, 1.5f, 1.5f))
            // Left Horn
            g2.color = Color(0xFA, 0xFA, 0xFA)
            val hornL = Polygon(intArrayOf(-5, -8, -5), intArrayOf(headY.toInt(), headY.toInt() - 6, headY.toInt() - 2), 3)
            val hornR = Polygon(intArrayOf(5, 8, 5), intArrayOf(headY.toInt(), headY.toInt() - 6, headY.toInt() - 2), 3)
            g2.fill(hornL)
            g2.fill(hornR)
        }
    }

    private fun drawLaptop(g2: Graphics2D, tick: Long) {
        val laptopX = 4f
        val laptopY = -2f

        g2.color = LAPTOP_BODY
        g2.fill(RoundRectangle2D.Float(laptopX, laptopY + 5f, 10f, 2.5f, 1.5f, 1.5f))

        g2.color = LAPTOP_KEYBOARD
        g2.fill(RoundRectangle2D.Float(laptopX + 1f, laptopY + 5.5f, 8f, 1.5f, 1f, 1f))

        val screenBack = Polygon(
            intArrayOf((laptopX + 7f).toInt(), (laptopX + 12f).toInt(), (laptopX + 11f).toInt(), (laptopX + 6f).toInt()),
            intArrayOf((laptopY + 5f).toInt(), (laptopY - 3f).toInt(), (laptopY - 3f).toInt(), (laptopY + 5f).toInt()),
            4
        )
        g2.fill(screenBack)

        val screenGlow = sin(tick * 0.3).toFloat() * 0.2f + 0.8f
        g2.color = Color(
            (LAPTOP_SCREEN.red * screenGlow).toInt().coerceIn(0, 255),
            (LAPTOP_SCREEN.green * screenGlow).toInt().coerceIn(0, 255),
            (LAPTOP_SCREEN.blue * screenGlow).toInt().coerceIn(0, 255)
        )
        val screenGlowShape = Polygon(
            intArrayOf((laptopX + 7f).toInt(), (laptopX + 11f).toInt(), (laptopX + 10f).toInt(), (laptopX + 6f).toInt()),
            intArrayOf((laptopY + 4f).toInt(), (laptopY - 2.5f).toInt(), (laptopY - 2.5f).toInt(), (laptopY + 4f).toInt()),
            4
        )
        g2.fill(screenGlowShape)
    }

    private fun drawFoamFinger(g2: Graphics2D) {
        val finger = GeneralPath()
        finger.moveTo(5.0, -2.0)
        finger.lineTo(5.0, -11.0)
        finger.curveTo(5.0, -13.0, 7.5, -13.0, 7.5, -11.0)
        finger.lineTo(7.5, -6.0)
        finger.lineTo(9.5, -6.0)
        finger.lineTo(9.5, -2.0)
        finger.lineTo(5.0, -2.0)
        finger.closePath()

        g2.color = Color(0x2E, 0xCC, 0x71)
        g2.fill(finger)
        g2.color = Color(0x27, 0xAE, 0x60)
        g2.stroke = BasicStroke(0.8f)
        g2.draw(finger)

        g2.color = Color.WHITE
        g2.font = Font("Segoe UI", Font.BOLD, 5)
        g2.drawString("#1", 5.5f, -3.5f)
    }

    private fun drawRedCard(g2: Graphics2D) {
        g2.color = Color(0xE7, 0x4C, 0x3C)
        g2.fill(RoundRectangle2D.Float(6f, -10f, 6.5f, 9f, 1f, 1f))
        g2.color = Color(0xC0, 0x39, 0x2B)
        g2.stroke = BasicStroke(0.8f)
        g2.draw(RoundRectangle2D.Float(6f, -10f, 6.5f, 9f, 1f, 1f))
    }

    private fun drawHazardSign(g2: Graphics2D) {
        val sign = Polygon(intArrayOf(6, 12, 18), intArrayOf(-2, -12, -2), 3)
        g2.color = Color(0xF1, 0xC4, 0x0F)
        g2.fill(sign)
        g2.color = Color(0xD6, 0x89, 0x10)
        g2.stroke = BasicStroke(0.8f)
        g2.draw(sign)

        g2.color = Color(0x1E, 0x27, 0x2C)
        g2.fillRect(11, -9, 2, 3)
        g2.fillRect(11, -4, 2, 2)
    }

    private fun drawWastebasket(g2: Graphics2D) {
        g2.color = Color(0x7F, 0x8C, 0x8D)
        val bin = Polygon(intArrayOf(-12, -7, -8, -11), intArrayOf(1, 1, 8, 8), 4)
        g2.fill(bin)
        g2.color = Color(0x95, 0xA5, 0xA6)
        g2.draw(bin)

        g2.color = Color.WHITE
        g2.fillOval(-11, 0, 4, 3)
    }

    private fun drawSaveStamp(g2: Graphics2D, tick: Long) {
        val stampY = -15f + sin(tick * 0.2).toFloat() * 1.2f
        g2.color = Color(0x2E, 0xCC, 0x71)
        g2.fill(RoundRectangle2D.Float(-12f, stampY, 24f, 7.5f, 3f, 3f))
        g2.color = Color(0x27, 0xAE, 0x60)
        g2.stroke = BasicStroke(0.8f)
        g2.draw(RoundRectangle2D.Float(-12f, stampY, 24f, 7.5f, 3f, 3f))

        g2.color = Color.WHITE
        g2.font = Font("Segoe UI", Font.BOLD, 5)
        g2.drawString("✓ SAVED", -10f, stampY + 5.5f)
    }
}
