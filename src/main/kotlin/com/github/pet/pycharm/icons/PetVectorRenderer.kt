package com.github.pet.pycharm.icons

import com.github.pet.pycharm.models.PetAccessory
import com.github.pet.pycharm.models.PetActivityState
import com.github.pet.pycharm.models.PetType
import com.github.pet.pycharm.models.PetCoat
import com.intellij.ui.JBColor
import java.awt.*
import java.awt.geom.*
import kotlin.math.cos
import kotlin.math.sin

object PetVectorRenderer {
    // Reusable geometry buffers to eliminate per-frame allocations during 60 FPS rendering
    private val sharedRound = RoundRectangle2D.Float()
    private val sharedEllipse = Ellipse2D.Float()
    private val sharedLine = Line2D.Float()
    private val sharedPath = Path2D.Float()
    private val sharedPolyX = IntArray(8)
    private val sharedPolyY = IntArray(8)
    private val sharedPoly = Polygon()

    private fun Graphics2D.fillOval(x: Float, y: Float, width: Float, height: Float) {
        sharedEllipse.setFrame(x.toDouble(), y.toDouble(), width.toDouble(), height.toDouble())
        fill(sharedEllipse)
    }
    private fun Graphics2D.drawOval(x: Float, y: Float, width: Float, height: Float) {
        sharedEllipse.setFrame(x.toDouble(), y.toDouble(), width.toDouble(), height.toDouble())
        draw(sharedEllipse)
    }
    private fun Graphics2D.fillRoundRect(x: Float, y: Float, w: Float, h: Float, arcW: Float, arcH: Float) {
        sharedRound.setRoundRect(x.toDouble(), y.toDouble(), w.toDouble(), h.toDouble(), arcW.toDouble(), arcH.toDouble())
        fill(sharedRound)
    }
    private fun Graphics2D.drawRoundRect(x: Float, y: Float, w: Float, h: Float, arcW: Float, arcH: Float) {
        sharedRound.setRoundRect(x.toDouble(), y.toDouble(), w.toDouble(), h.toDouble(), arcW.toDouble(), arcH.toDouble())
        draw(sharedRound)
    }
    private fun Graphics2D.drawLine(x1: Float, y1: Float, x2: Float, y2: Float) {
        sharedLine.setLine(x1.toDouble(), y1.toDouble(), x2.toDouble(), y2.toDouble())
        draw(sharedLine)
    }
    private fun Graphics2D.fillTriangle(x1: Float, y1: Float, x2: Float, y2: Float, x3: Float, y3: Float) {
        sharedPolyX[0] = x1.toInt(); sharedPolyX[1] = x2.toInt(); sharedPolyX[2] = x3.toInt()
        sharedPolyY[0] = y1.toInt(); sharedPolyY[1] = y2.toInt(); sharedPolyY[2] = y3.toInt()
        sharedPoly.npoints = 3
        sharedPoly.xpoints = sharedPolyX
        sharedPoly.ypoints = sharedPolyY
        sharedPoly.invalidate()
        fill(sharedPoly)
    }
    private fun Graphics2D.fillTriangle(x1: Int, y1: Int, x2: Int, y2: Int, x3: Int, y3: Int) {
        sharedPolyX[0] = x1; sharedPolyX[1] = x2; sharedPolyX[2] = x3
        sharedPolyY[0] = y1; sharedPolyY[1] = y2; sharedPolyY[2] = y3
        sharedPoly.npoints = 3
        sharedPoly.xpoints = sharedPolyX
        sharedPoly.ypoints = sharedPolyY
        sharedPoly.invalidate()
        fill(sharedPoly)
    }

    // Palette
    private val CAT_WHITE = Color(0xFD, 0xFD, 0xFD)
    private val CAT_INNER_EAR = Color(0xFF, 0x8A, 0xA5)

    private val DOG_WHITE = Color(0xFD, 0xFD, 0xFD)
    private val DOG_TONGUE = Color(0xFF, 0x6B, 0x81)

    private val DUCK_BEAK = Color(0xFF, 0x76, 0x1E)
    private val DUCK_BEAK_SHADOW = Color(0xD8, 0x58, 0x0B)

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

    // Reusable cached strokes to eliminate per-frame allocations
    private val STROKE_08_PLAIN = BasicStroke(0.8f)
    private val STROKE_08_ROUND = BasicStroke(0.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)
    private val STROKE_09_ROUND = BasicStroke(0.9f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)
    private val STROKE_10_ROUND = BasicStroke(1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)
    private val STROKE_11_ROUND = BasicStroke(1.1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)
    private val STROKE_12_ROUND = BasicStroke(1.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)
    private val STROKE_13_ROUND = BasicStroke(1.3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)
    private val STROKE_14_PLAIN = BasicStroke(1.4f)
    private val STROKE_14_ROUND = BasicStroke(1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)
    private val STROKE_18_ROUND = BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)
    private val STROKE_20_ROUND = BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)
    private val STROKE_22_ROUND = BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)
    private val STROKE_26_ROUND = BasicStroke(2.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)
    private val STROKE_28_ROUND = BasicStroke(2.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)

    fun renderPet(
        g2: Graphics2D, petType: PetType, state: PetActivityState, tick: Long,
        walkCycle: Float, facingRight: Boolean, jumpOffset: Float = 0f,
        isPaused: Boolean = false, activeAccessories: Set<String> = emptySet(),
        showSaveStamp: Boolean = false, gazeX: Float = 0f,
        coat: PetCoat = PetCoat.ORIGINAL, sleepBlend: Float = if (state == PetActivityState.SLEEPING) 1f else 0f,
        actionProgress: Float = 0f, reducedMotion: Boolean = false
    ) {
        val blend = sleepBlend.coerceIn(0f, 1f)
        val frozen = isPaused || reducedMotion
        val phaseTick = if (frozen) 0L else tick
        if (blend > 0f && blend < 1f) {
            drawSleepTransition(g2, petType, blend, phaseTick, facingRight, coat, activeAccessories)
            return
        }
        val origTransform = g2.transform
        if (blend < 1f) {
            try {
                val stretch = if (!frozen && state == PetActivityState.STRETCHING) sin(actionProgress.coerceIn(0f, 1f) * Math.PI).toFloat() else 0f
                g2.translate(0.0, 8.0)
                g2.scale(1.0 + stretch * 0.18, 1.0 - stretch * 0.13)
                g2.translate(0.0, -8.0)
                renderPose(g2, petType, if (state == PetActivityState.SLEEPING) PetActivityState.YAWNING else state,
                    phaseTick, if (frozen) 0f else walkCycle, facingRight, if (frozen) 0f else jumpOffset,
                    frozen, activeAccessories, showSaveStamp, gazeX, coat, actionProgress)
            } finally { g2.transform = origTransform }
        }
        if (blend > 0f) {
            try {
                renderPose(g2, petType, PetActivityState.SLEEPING, phaseTick, 0f, facingRight,
                    0f, frozen, emptySet(), false, 0f, coat, actionProgress)
            } finally { g2.transform = origTransform }
        }
    }

    /** Interpolate one body and one head, keeping the feet and world position fixed. */
    private fun drawSleepTransition(original: Graphics2D, type: PetType, amount: Float, tick: Long,
                                    facingRight: Boolean, coat: PetCoat, accessories: Set<String>) {
        val g = original.create() as Graphics2D
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
            g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE)
            if (!facingRight) g.scale(-1.0, 1.0)
            val t = amount * amount * (3f - 2f * amount)
            fun lerp(a: Float, b: Float) = a + (b - a) * t
            g.color = SHADOW_COLOR
            g.fill(Ellipse2D.Float(lerp(-9f, -15f), 7.5f, lerp(18f, 30f), 3.5f))
            g.color = Color(0x70, 0xA1, 0xFF, (136 * t).toInt())
            g.fill(RoundRectangle2D.Float(-15f, 5.5f, 30f, 4.5f, 4f, 4f))
            val limbs = g.create() as Graphics2D
            try {
                limbs.composite = AlphaComposite.SrcOver.derive(1f - t)
                limbs.color = coat.body(type)
                limbs.stroke = STROKE_26_ROUND
                if (type == PetType.CAT || type == PetType.DOG) limbs.draw(Path2D.Float().apply {
                    moveTo(-6.0, 3.0); curveTo(-12.0, 2.0, -12.0, -7.0, -9.0, -6.0)
                })
                limbs.color = if (type == PetType.DUCK) DUCK_BEAK else if (type == PetType.CRAB) coat.detail(type) else CAT_WHITE
                limbs.fill(RoundRectangle2D.Float(-5f, 5.5f, 3.5f, 3.5f, 2f, 2f))
                limbs.fill(RoundRectangle2D.Float(1.5f, 5.5f, 3.5f, 3.5f, 2f, 2f))
            } finally { limbs.dispose() }
            val bodyY = lerp(-3f, 1.5f) + sin(tick * 0.12).toFloat() * 0.3f
            g.color = coat.body(type)
            if (type == PetType.DUCK) g.fill(Ellipse2D.Float(lerp(-7f, -12f), bodyY, lerp(14f, 23f), lerp(10f, 7f)))
            else if (type == PetType.CRAB) g.fill(RoundRectangle2D.Float(lerp(-8f, -12f), bodyY, lerp(16f, 24f), lerp(9f, 6.5f), 5f, 5f))
            else g.fill(RoundRectangle2D.Float(lerp(-7f, -12f), bodyY, lerp(14f, 24f), lerp(10f, 6.5f), 6f, 6f))
            if (type == PetType.CAT || type == PetType.DOG) {
                val chest = g.create() as Graphics2D
                try {
                    chest.composite = AlphaComposite.SrcOver.derive(1f - t)
                    chest.color = CAT_WHITE
                    chest.fill(Ellipse2D.Float(-2f, bodyY + 3f, 6f, 6f))
                } finally { chest.dispose() }
            } else if (type == PetType.DUCK) {
                g.color = coat.detail(type)
                g.fill(RoundRectangle2D.Float(lerp(-3.5f, -5f), bodyY + 2f, 7f, lerp(4.5f, 3f), 3f, 3f))
            } else if (type == PetType.CRAB) {
                g.color = coat.detail(type)
                g.fill(RoundRectangle2D.Float(lerp(-4f, -6f), bodyY + 2.5f, 8f, lerp(4.5f, 3f), 3f, 3f))
            }
            val headX = lerp(if (type == PetType.DUCK) -4f else -6f, if (type == PetType.DUCK) 3f else 1f)
            val headY = lerp(-10f, 0f) + sin(tick * 0.12).toFloat() * 0.3f
            val headW = lerp(if (type == PetType.DUCK) 12f else 13f, 10f)
            val headH = lerp(9.5f, 6f)
            if (type != PetType.CRAB) {
                g.color = coat.body(type)
                if (type == PetType.DUCK) g.fill(Ellipse2D.Float(headX, headY, headW, headH))
                else g.fill(RoundRectangle2D.Float(headX, headY, headW, headH, 5f, 5f))
            }
            when (type) {
                PetType.CAT -> {
                    g.fill(Path2D.Float().apply {
                        moveTo(headX.toDouble(), (headY + 1f).toDouble())
                        lineTo((headX + 3f).toDouble(), (headY - lerp(4f, 1f)).toDouble())
                        lineTo((headX + 6f).toDouble(), (headY + 1f).toDouble()); closePath()
                    })
                    g.fill(Path2D.Float().apply {
                        moveTo((headX + 7f).toDouble(), (headY + 1f).toDouble())
                        lineTo((headX + 10f).toDouble(), (headY - lerp(4f, 1f)).toDouble())
                        lineTo((headX + 12f).toDouble(), (headY + 1f).toDouble()); closePath()
                    })
                }
                PetType.DOG -> {
                    g.color = coat.detail(type)
                    g.fill(RoundRectangle2D.Float(headX - lerp(2.5f, 0f), headY + 1f, 3.5f, lerp(7.5f, 6f), 3f, 3f))
                    if (t < 0.8f) g.fill(RoundRectangle2D.Float(headX + headW - 1.5f, headY + 1f, 3.5f, lerp(7.5f, 3f), 3f, 3f))
                    g.color = DOG_WHITE
                    g.fill(RoundRectangle2D.Float(lerp(-1.5f, 6f), headY + 4f, lerp(6f, 7f), lerp(4.5f, 4f), 3f, 3f))
                }
                PetType.DUCK -> {
                    g.color = DUCK_BEAK
                    g.fill(RoundRectangle2D.Float(headX + headW - 3f, headY + 3f, lerp(6f, 4.5f), 2f, 2f, 2f))
                }
                PetType.CRAB -> {
                    g.color = coat.detail(type)
                    g.fill(Ellipse2D.Float(headX - lerp(4f, 2f), headY + 1f, 4.5f, 4f))
                    g.fill(Ellipse2D.Float(headX + headW - lerp(1f, 2f), headY + 1f, 4.5f, 4f))
                }
            }
            g.color = EYE_COLOR
            g.stroke = STROKE_12_ROUND
            if (type != PetType.DUCK && t < 0.8f) g.draw(Line2D.Float(headX + 2f, headY + 3f, headX + 4f, headY + 3f))
            val eyeX = lerp(2f, if (type == PetType.CAT) 7f else 5f)
            g.draw(Line2D.Float(eyeX, headY + 3f, eyeX + 2f, headY + 3f))
            if (type == PetType.DOG) g.fill(Ellipse2D.Float(lerp(0.5f, 11f), headY + 4.2f, 2.2f, 1.8f))
            if (accessories.isNotEmpty()) {
                val hats = g.create() as Graphics2D
                try {
                    hats.composite = AlphaComposite.SrcOver.derive(1f - t)
                    hats.translate((headX - if (type == PetType.DUCK) -4f else -6f).toDouble(), (headY + 10f).toDouble())
                    drawAccessories(hats, accessories, type, tick)
                } finally { hats.dispose() }
            }
        } finally { g.dispose() }
    }

    private fun renderPose(
        g2: Graphics2D,
        petType: PetType,
        state: PetActivityState,
        tick: Long,
        walkCycle: Float,
        facingRight: Boolean,
        jumpOffset: Float = 0f,
        isPaused: Boolean = false,
        activeAccessories: Set<String> = emptySet(),
        showSaveStamp: Boolean = false,
        gazeX: Float = 0f,
        coat: PetCoat,
        actionProgress: Float
    ) {
        val origTransform = g2.transform

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE)

        val effectiveJump = if (isPaused) 0f else jumpOffset
        val effectiveWalk = if (isPaused) 0f else walkCycle

        // Ground shadow
        val shadowW = if (state == PetActivityState.SLEEPING) 30f else 18f
        g2.color = SHADOW_COLOR
        g2.fillOval(-shadowW / 2, 7.5f, shadowW, 3.5f)

        // Sleeping mat
        if (state == PetActivityState.SLEEPING) {
            g2.color = SLEEP_MAT
            g2.fillRoundRect(-15f, 5.5f, 30f, 4.5f, 4f, 4f)
        }

        g2.translate(0.0, -effectiveJump.toDouble())
        if (!facingRight) {
            g2.scale(-1.0, 1.0)
        }

        when (petType) {
            PetType.CAT -> drawCat(g2, state, tick, effectiveWalk, isPaused, if (facingRight) gazeX else -gazeX, coat)
            PetType.DOG -> drawDog(g2, state, tick, effectiveWalk, isPaused, if (facingRight) gazeX else -gazeX, coat)
            PetType.DUCK -> drawDuck(g2, state, tick, effectiveWalk, isPaused, if (facingRight) gazeX else -gazeX, coat)
            PetType.CRAB -> drawCrab(g2, state, tick, effectiveWalk, isPaused, if (facingRight) gazeX else -gazeX, coat)
        }

        // Render Accessories over Pet
        if (state != PetActivityState.SLEEPING) {
            drawAccessories(g2, activeAccessories, petType, tick)
            if (!isPaused) drawPersonality(g2, petType, state, tick, actionProgress, coat)
        }

        // Draw Mini Laptop if coding
        if (state == PetActivityState.CODING) {
            drawLaptop(g2, tick)
        }

        // Render activity props after the pet so their motion remains readable at status-bar size.
        if (!isPaused) {
            when (state) {
                PetActivityState.STADIUM_VICTORY -> drawFoamFinger(g2)
                PetActivityState.MEME_WARNING -> drawHazardSign(g2)
                PetActivityState.SCRUNCHING_PAPER -> drawWastebasket(g2, tick)
                else -> {}
            }
            if (showSaveStamp) {
                drawSaveStamp(g2, tick)
            }
        }
        drawActivityCue(g2, petType, state, tick)

        g2.transform = origTransform
    }

    fun renderHome(g2: Graphics2D, petType: PetType, tick: Long) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        when (petType) {
            PetType.DOG -> drawDogHouse(g2, tick)
            PetType.CAT -> drawCatBed(g2, tick)
            PetType.DUCK -> drawDuckPond(g2, tick)
            PetType.CRAB -> drawCrabCove(g2, tick)
        }
        val hour = java.time.LocalTime.now().hour
        if (hour >= 21 || hour < 6) {
            drawNightSky(g2, tick)
        }
    }

    private fun drawNightSky(g2: Graphics2D, tick: Long) {
        val moon = java.awt.geom.Area(Ellipse2D.Float(-14f, -12.5f, 5.5f, 5.5f))
        moon.subtract(java.awt.geom.Area(Ellipse2D.Float(-12.2f, -13.5f, 5.2f, 5.2f)))
        g2.color = Color(0xFF, 0xEE, 0x88, 220)
        g2.fill(moon)

        val twinkle = (sin(tick * 0.18).toFloat() + 1f) * 0.5f
        g2.color = Color(0xFF, 0xFF, 0xEE, (110 + twinkle * 145).toInt().coerceIn(0, 255))
        g2.stroke = STROKE_08_ROUND
        val starX = -5.5f
        val starY = -10f
        g2.draw(Line2D.Float(starX - 1.2f, starY, starX + 1.2f, starY))
        g2.draw(Line2D.Float(starX, starY - 1.2f, starX, starY + 1.2f))
    }

    private fun drawCrabCove(g2: Graphics2D, tick: Long) {
        val waterShimmer = (sin(tick * 0.1).toFloat() + 1f) * 0.5f
        // Warm sand bank
        g2.color = Color(0xDE, 0xB8, 0x87)
        g2.fill(RoundRectangle2D.Float(-13f, 3.5f, 26f, 5.2f, 4f, 4f))
        g2.color = Color(0xF5, 0xDE, 0xB3)
        g2.fill(RoundRectangle2D.Float(-10f, 4.5f, 20f, 3.2f, 2.5f, 2.5f))

        // Tide pool shimmer
        g2.color = Color(0x76, 0xC7, 0xE8, (70 + (waterShimmer * 50).toInt()).coerceIn(0, 255))
        g2.fill(Ellipse2D.Float(-4f, 4.8f, 11f, 2.6f))

        // Slate tide rocks on left
        g2.color = Color(0x70, 0x80, 0x90)
        g2.fill(RoundRectangle2D.Float(-15f, 1.5f, 6.5f, 5.5f, 2.5f, 2.5f))
        g2.color = Color(0x88, 0x99, 0xA6)
        g2.fill(RoundRectangle2D.Float(-14f, 1.8f, 4.5f, 3f, 2f, 2f))

        // Tiny sea kelp tuft
        val sway = (sin(tick * 0.15).toFloat() * 1.5f)
        g2.color = Color(0x27, 0xAE, 0x60)
        g2.stroke = STROKE_12_ROUND
        g2.draw(Path2D.Float().apply {
            moveTo(-11.0, 2.0)
            curveTo(-11.0 + sway, -1.0, -13.0 + sway, -3.0, -12.0 + sway, -5.0)
        })

        // Little shiny bronze gear / cog resting against the rock
        g2.color = Color(0xD3, 0x54, 0x00)
        g2.fill(Ellipse2D.Float(-9.5f, 3.5f, 3.2f, 3.2f))
        g2.color = Color(0xF3, 0x9C, 0x12)
        g2.fill(Ellipse2D.Float(-8.8f, 4.2f, 1.8f, 1.8f))
    }

    private fun drawDogHouse(g2: Graphics2D, tick: Long) {
        val glow = (sin(tick * 0.08).toFloat() + 1f) * 0.35f
        val house = RoundRectangle2D.Float(-15f, -3f, 11f, 10f, 2.5f, 2.5f)
        val roof = Polygon(intArrayOf(-16, -10, -3), intArrayOf(-3, -10, -3), 3)

        g2.color = Color(0xC9, 0x5A, 0x23)
        g2.fill(house)
        g2.color = Color(0x8D, 0x38, 0x19)
        g2.fill(roof)

        g2.color = Color(0x4A, 0x25, 0x17)
        g2.fill(RoundRectangle2D.Float(-12.5f, 0f, 5f, 7f, 3f, 3f))
        g2.color = Color(0xFF, 0xD2, 0x6B, (80 + (glow * 80).toInt()).coerceIn(0, 255))
        g2.fillOval(-11.2f, 1.3f, 2.4f, 3f)
    }

    private fun drawCatBed(g2: Graphics2D, tick: Long) {
        val cushionBob = sin(tick * 0.12).toFloat() * 0.35f
        g2.color = Color(0x9B, 0x59, 0xB6)
        g2.fill(RoundRectangle2D.Float(-12f, 3f + cushionBob, 24f, 5.2f, 4f, 4f))
        g2.color = Color(0xD2, 0xB4, 0xDE)
        g2.fill(RoundRectangle2D.Float(-9f, 4.2f + cushionBob, 18f, 2.8f, 2.5f, 2.5f))

        g2.color = Color(0xF5, 0x82, 0x20)
        g2.stroke = STROKE_12_ROUND
        g2.drawOval(-15, 1, 4, 4)
        g2.drawLine(-13, 2, -10, 5)
        g2.drawLine(-13, 5, -10, 2)
    }

    private fun drawDuckPond(g2: Graphics2D, tick: Long) {
        val ripple = sin(tick * 0.17).toFloat()
        g2.color = Color(0x76, 0xC7, 0xE8)
        g2.fill(RoundRectangle2D.Float(-13f, 3.5f, 27f, 5f, 5f, 5f))
        g2.color = Color(0xC5, 0xF1, 0xFF)
        g2.stroke = STROKE_08_ROUND
        g2.drawArc(-9, 4, 8, 2 + (ripple * 0.8f).toInt(), 10, 160)
        g2.drawArc(3, 5, 8, 2 + (-ripple * 0.8f).toInt(), 10, 160)

        g2.color = Color(0x4E, 0xA5, 0x4F)
        g2.stroke = STROKE_13_ROUND
        g2.drawLine(-12, 4, -13, -1)
        g2.drawLine(-10, 4, -9, 0)
        g2.color = Color(0xF7, 0xD5, 0x4A)
        g2.fillOval(-14f, -2f, 2.5f, 1.5f)
    }

    private fun drawActivityCue(g2: Graphics2D, petType: PetType, state: PetActivityState, tick: Long) {
        when (state) {
            PetActivityState.EATING -> drawTreat(g2, petType, tick)
            PetActivityState.RUNNING -> drawRunCue(g2, tick)
            PetActivityState.COMMITTING -> drawCommitCue(g2, tick)
            PetActivityState.DEBUGGING -> drawDebugCue(g2, tick)
            PetActivityState.HYDRATE_REMINDER -> drawHydrateCue(g2, tick)
            PetActivityState.MERGE_CONFLICT -> drawMergeConflictCue(g2, tick)
            PetActivityState.RUN_SUCCEEDED, PetActivityState.RUN_FAILED,
            PetActivityState.TESTS_FAILED, PetActivityState.COMMIT_SUCCEEDED,
            PetActivityState.STADIUM_VICTORY -> drawOutcome(g2, state)
            else -> {}
        }
    }

    private fun drawHydrateCue(g2: Graphics2D, tick: Long) {
        val bob = sin(tick * 0.45).toFloat() * 0.6f
        val mugY = -10f + bob
        g2.color = Color(0x34, 0x98, 0xDB)
        g2.fill(RoundRectangle2D.Float(8f, mugY, 7.5f, 7f, 2f, 2f))
        g2.color = Color(0x29, 0x80, 0xB9)
        g2.fill(RoundRectangle2D.Float(9f, mugY + 1f, 5.5f, 1.5f, 1f, 1f))
        g2.color = Color(0x34, 0x98, 0xDB)
        g2.stroke = STROKE_12_ROUND
        g2.draw(Arc2D.Float(13.8f, mugY + 1.2f, 3.2f, 4.2f, -90f, 180f, Arc2D.OPEN))
        val steam1 = sin(tick * 0.3).toFloat() * 0.6f
        val steam2 = sin(tick * 0.3 + 1.6).toFloat() * 0.6f
        g2.color = Color(0xE8, 0xF4, 0xF8, 200)
        g2.stroke = STROKE_09_ROUND
        g2.draw(Line2D.Float(10f + steam1, mugY - 1f, 10.5f - steam1, mugY - 3.5f))
        g2.draw(Line2D.Float(13f + steam2, mugY - 0.8f, 13.5f - steam2, mugY - 3.8f))
    }

    private fun drawMergeConflictCue(g2: Graphics2D, tick: Long) {
        val bob = sin(tick * 0.5).toFloat() * 0.7f
        val lensY = -11f + bob
        g2.color = Color(0xF3, 0x9C, 0x12)
        g2.stroke = STROKE_14_ROUND
        g2.draw(Ellipse2D.Float(8f, lensY, 7f, 7f))
        g2.color = Color(0x76, 0xD7, 0xEA, 130)
        g2.fill(Ellipse2D.Float(8.7f, lensY + 0.7f, 5.6f, 5.6f))
        g2.color = Color(0xC0, 0x39, 0x2B)
        g2.stroke = STROKE_09_ROUND
        g2.drawLine(12, (lensY + 2.2f).toInt(), 10, (lensY + 3.5f).toInt())
        g2.drawLine(10, (lensY + 3.5f).toInt(), 12, (lensY + 4.8f).toInt())
        g2.color = Color(0x79, 0x55, 0x48)
        g2.stroke = STROKE_20_ROUND
        g2.draw(Line2D.Float(13.8f, lensY + 5.8f, 17f, lensY + 9f))
    }

    private fun drawOutcome(g: Graphics2D, state: PetActivityState) {
        val failed = state == PetActivityState.RUN_FAILED || state == PetActivityState.TESTS_FAILED
        g.color = when {
            failed -> Color(0xE66A73)
            state == PetActivityState.RUN_SUCCEEDED -> Color(0x58A6EF)
            else -> Color(0x65C98A)
        }
        g.fill(RoundRectangle2D.Float(7f, -13f, 10f, 9f, 3f, 3f))
        g.color = Color.WHITE
        g.stroke = STROKE_12_ROUND
        when (state) {
            PetActivityState.TESTS_FAILED -> {
                g.draw(Rectangle2D.Float(9f, -11f, 2f, 2f))
                g.draw(Line2D.Float(13f, -10f, 15f, -8f)); g.draw(Line2D.Float(15f, -10f, 13f, -8f))
            }
            PetActivityState.RUN_FAILED -> {
                g.draw(Line2D.Float(10f, -11f, 14f, -7f)); g.draw(Line2D.Float(14f, -11f, 10f, -7f))
            }
            PetActivityState.COMMIT_SUCCEEDED -> {
                g.draw(Line2D.Float(10f, -11f, 10f, -7f))
                g.draw(Ellipse2D.Float(9f, -11.5f, 2f, 2f))
                g.draw(Line2D.Float(12f, -8.5f, 13f, -7.5f)); g.draw(Line2D.Float(13f, -7.5f, 15f, -10f))
            }
            else -> {
                g.draw(Line2D.Float(9.5f, -9f, 11.5f, -7f)); g.draw(Line2D.Float(11.5f, -7f, 14.5f, -11f))
            }
        }
    }

    private fun drawRestingEyes(g: Graphics2D, headY: Float, singleEye: Boolean) {
        g.color = EYE_COLOR
        g.stroke = STROKE_12_ROUND
        if (!singleEye) g.drawLine(-4f, headY + 3f, -2f, headY + 3f)
        g.drawLine(2f, headY + 3f, 4f, headY + 3f)
    }

    private fun drawPersonality(g: Graphics2D, type: PetType, state: PetActivityState, tick: Long, progress: Float, coat: PetCoat) {
        val wave = sin(tick * 0.5).toFloat()
        g.stroke = STROKE_12_ROUND
        when (state) {
            PetActivityState.YAWNING -> {
                val opening = sin(progress.coerceIn(0f, 1f) * Math.PI).toFloat().coerceAtLeast(0f)
                g.color = EYE_COLOR
                val mouthX = if (type == PetType.DUCK) 7f else if (type == PetType.CRAB) 1f else 0f
                g.fillOval(mouthX, -3.5f, 2.5f, 1f + opening * 3f)
                if (opening > 0.4f) {
                    g.color = DOG_TONGUE
                    g.fillOval(if (type == PetType.DUCK) 7.8f else if (type == PetType.CRAB) 1.7f else 0.7f, -1.5f, 1.2f, opening * 1.5f)
                }
            }
            PetActivityState.GROOMING -> {
                g.color = if (type == PetType.DUCK || type == PetType.CRAB) coat.detail(type) else Color(0xFDFDFD)
                g.fill(Ellipse2D.Float(3f, -4f + wave * 1.2f, 4f, 3f))
                g.color = DOG_TONGUE
                g.draw(Line2D.Float(2.5f, -3f, 5f, -2f + wave))
            }
            PetActivityState.GREETING, PetActivityState.PLAYING -> {
                drawSocialQuirk(g, type, state, tick, coat)
            }
            PetActivityState.STRETCHING -> {
                val reach = sin(progress.coerceIn(0f, 1f) * Math.PI).toFloat() * 4f
                g.color = if (type == PetType.DUCK || type == PetType.CRAB) coat.detail(type) else Color(0xFDFDFD)
                g.fill(RoundRectangle2D.Float(2f, 5.5f, 4f + reach, 2.5f, 2f, 2f))
            }
            else -> {}
        }
    }

    private fun drawSocialQuirk(g: Graphics2D, type: PetType, state: PetActivityState, tick: Long, coat: PetCoat) {
        when (type) {
            PetType.CAT -> {
                val pawBob = sin(tick * 0.65).toFloat() * 2f
                g.color = coat.detail(PetType.CAT)
                g.fill(RoundRectangle2D.Float(5f, 3f + pawBob, 4f, 2.4f, 1.5f, 1.5f))
                g.color = DOG_TONGUE
                g.fill(Ellipse2D.Float(7.5f, 3.6f + pawBob, 1.2f, 1.2f))
                g.color = Color(0xF3, 0x9C, 0x12)
                g.stroke = STROKE_11_ROUND
                g.draw(Arc2D.Float(8f, -7f + pawBob * 0.5f, 3.5f, 4f, -45f, 180f, Arc2D.OPEN))
            }
            PetType.DOG -> {
                val bowWag = sin(tick * 0.85).toFloat() * 2.5f
                g.color = coat.detail(PetType.DOG)
                g.fill(RoundRectangle2D.Float(4.5f, 5.2f, 5.5f, 2.5f, 1.5f, 1.5f))
                g.color = coat.body(PetType.DOG)
                g.stroke = STROKE_22_ROUND
                g.draw(Line2D.Float(-9f, 0f, -14f, -4f + bowWag))
                g.color = Color(0x70, 0xA1, 0xFF)
                g.stroke = STROKE_11_ROUND
                g.draw(Arc2D.Float(9f, -6f, 3.5f, 4.5f, -40f, 80f, Arc2D.OPEN))
            }
            PetType.DUCK -> {
                val wingFlap = sin(tick * 0.75).toFloat() * 3f
                g.color = coat.detail(PetType.DUCK)
                val wing = Path2D.Float().apply {
                    moveTo(1.0, 0.0)
                    curveTo(3.0, -5.0 + wingFlap, 7.0, -4.0 + wingFlap, 5.0, 1.0)
                    closePath()
                }
                g.fill(wing)
                g.color = Color(0x34, 0x98, 0xDB)
                g.stroke = STROKE_11_ROUND
                g.draw(Arc2D.Float(11f, -5f, 4f, 4f, -50f, 100f, Arc2D.OPEN))
            }
            PetType.CRAB -> {
                val snip1 = sin(tick * 0.7).toFloat() * 2.5f
                val snip2 = sin(tick * 0.7 + Math.PI.toFloat()).toFloat() * 2.5f
                g.color = coat.detail(PetType.CRAB)
                g.fill(Ellipse2D.Float(-7f, -11f + snip1, 3.5f, 4.5f))
                g.fill(Ellipse2D.Float(6f, -11f + snip2, 3.5f, 4.5f))
                g.color = Color(0xE6, 0x7E, 0x22)
                g.stroke = STROKE_10_ROUND
                g.draw(Line2D.Float(9.5f, -11f + snip2, 11.5f, -13f + snip2))
                g.draw(Line2D.Float(-7.5f, -11f + snip1, -9.5f, -13f + snip1))
            }
        }
    }

    private fun drawTreat(g2: Graphics2D, petType: PetType, tick: Long) {
        val bitePhase = ((tick % 18L) / 18f)
        val bob = sin(tick * 0.75).toFloat() * 0.8f
        val treatX = 10f - bitePhase * 5f
        val treatY = -1.5f + bob
        val treatColor = when (petType) {
            PetType.CAT -> Color(0x5D, 0xA9, 0xD6)
            PetType.DOG -> Color(0xA9, 0x5A, 0x2A)
            PetType.DUCK -> Color(0xE8, 0xB3, 0x31)
            PetType.CRAB -> Color(0xCE, 0x7B, 0x3B)
        }

        g2.color = treatColor
        when (petType) {
            PetType.CAT -> {
                val fish = Polygon(
                    intArrayOf(treatX.toInt() - 3, treatX.toInt() + 2, treatX.toInt() + 5, treatX.toInt() + 2),
                    intArrayOf(treatY.toInt(), treatY.toInt() - 2, treatY.toInt(), treatY.toInt() + 2),
                    4
                )
                g2.fill(fish)
                g2.fillOval(treatX - 4f, treatY - 1.5f, 3f, 3f)
            }
            PetType.DOG -> {
                g2.fill(RoundRectangle2D.Float(treatX - 3f, treatY - 1f, 6f, 2.2f, 1f, 1f))
                g2.fillOval(treatX - 4.3f, treatY - 2f, 2.6f, 2.6f)
                g2.fillOval(treatX + 1.7f, treatY - 2f, 2.6f, 2.6f)
            }
            PetType.DUCK -> {
                g2.fillOval(treatX - 2f, treatY - 2f, 4f, 4f)
                g2.color = Color(0xF9, 0xE6, 0x9B)
                g2.fillOval(treatX - 0.4f, treatY - 0.6f, 0.8f, 0.8f)
            }
            PetType.CRAB -> {
                g2.fillOval(treatX - 2.5f, treatY - 2.5f, 5f, 5f)
                for (angle in 0 until 6) {
                    val rad = Math.toRadians(angle * 60.0 + (tick * 4.0))
                    val gx = treatX + (cos(rad) * 2.8).toFloat()
                    val gy = treatY + (sin(rad) * 2.8).toFloat()
                    g2.fillOval(gx - 0.8f, gy - 0.8f, 1.6f, 1.6f)
                }
                g2.color = Color(0x24, 0x2B, 0x2E)
                g2.fillOval(treatX - 0.9f, treatY - 0.9f, 1.8f, 1.8f)
            }
        }

        g2.color = treatColor
        g2.fillOval(5.2f + sin(tick * 1.1).toFloat(), 0.5f + cos(tick * 0.9).toFloat(), 1.2f, 1.2f)
        g2.fillOval(8f + cos(tick * 1.3).toFloat(), 1.5f, 1f, 1f)
    }

    private fun drawRunCue(g2: Graphics2D, tick: Long) {
        val pulse = sin(tick * 0.8).toFloat()
        g2.color = Color(0x64, 0xB5, 0xF6, 180)
        g2.stroke = STROKE_12_ROUND
        g2.drawLine(-14, 1, -8 + pulse.toInt(), 1)
        g2.drawLine(-12, 4, -5 + pulse.toInt(), 4)
        g2.drawLine(-16, -2, -10 + pulse.toInt(), -2)
        val play = Path2D.Float().apply { moveTo(9.0, -12.0); lineTo(15.0, -8.0); lineTo(9.0, -4.0); closePath() }
        g2.fill(play)
    }

    private fun drawCommitCue(g2: Graphics2D, tick: Long) {
        val stampBob = sin(tick * 0.45).toFloat() * 0.7f
        g2.color = Color(0x2E, 0xCC, 0x71)
        g2.fill(RoundRectangle2D.Float(8f, -10f + stampBob, 8f, 6f, 2f, 2f))
        g2.color = Color.WHITE
        g2.stroke = STROKE_12_ROUND
        g2.drawLine(10, (-7f + stampBob).toInt(), 11, (-6f + stampBob).toInt())
        g2.drawLine(11, (-6f + stampBob).toInt(), 14, (-9f + stampBob).toInt())
        g2.color = Color(0x27, 0xAE, 0x60)
        g2.fillOval(10f, -13f + stampBob, 2f, 2f)
        g2.fillOval(13f, -13f + stampBob, 2f, 2f)
        g2.fillRect(11, (-12f + stampBob).toInt(), 3, 1)
    }

    private fun drawDebugCue(g2: Graphics2D, tick: Long) {
        val wobble = sin(tick * 0.55).toFloat() * 0.8f
        g2.color = Color(0xAB, 0x47, 0xBC)
        g2.stroke = STROKE_13_ROUND
        g2.drawOval(8, (-10f + wobble).toInt(), 6, 6)
        g2.drawLine(13, (-5f + wobble).toInt(), 16, (-2f + wobble).toInt())
        g2.color = Color(0xEF, 0x53, 0x50)
        g2.fillOval(10f, -8f + wobble, 2.2f, 2.2f)
        g2.color = Color(0x4A, 0x14, 0x58)
        g2.stroke = STROKE_08_ROUND
        g2.drawLine(9, (-8f + wobble).toInt(), 7, (-10f + wobble).toInt())
        g2.drawLine(13, (-8f + wobble).toInt(), 15, (-10f + wobble).toInt())
    }

    // =========================================================================
    // DOG (SPARKY) - Clean Kawaii Corgi Proportions
    // =========================================================================
    private fun drawDog(g2: Graphics2D, state: PetActivityState, tick: Long, walkCycle: Float, isPaused: Boolean, gaze: Float, coat: PetCoat) {
        val dogBody = coat.body(PetType.DOG)
        val dogEar = coat.detail(PetType.DOG)
        val breathing = if (isPaused) 0f else sin(tick * 0.12).toFloat() * 0.5f
        val tailSwing = if (isPaused) 0f else when (state) {
            PetActivityState.CODING -> sin(tick * 0.55).toFloat() * 26f
            PetActivityState.SLEEPING -> sin(tick * 0.05).toFloat() * 4f
            PetActivityState.CELEBRATING -> sin(tick * 0.7).toFloat() * 32f
            PetActivityState.PETTING, PetActivityState.EATING -> (sin(tick * 0.9) * 26 + sin(tick * 1.7) * 6).toFloat()
            PetActivityState.DEBUGGING -> sin(tick * 0.08).toFloat() * 6f
            else -> sin(tick * 0.2).toFloat() * 16f
        }

        if (state == PetActivityState.SLEEPING) {
            // A long, low body and resting head read as lying down at small sizes.
            g2.color = dogBody
            g2.fillRoundRect(-12f, 1f + breathing, 24f, 6.5f, 6f, 6f)

            // Floppy sleeping ear
            g2.color = dogEar
            g2.fillRoundRect(2f, 0f + breathing, 4f, 6f, 3f, 3f)

            // White muzzle patch
            g2.color = DOG_WHITE
            g2.fillOval(6f, 3f + breathing, 7f, 4f)

            // Sleeping eye (- -)
            g2.color = EYE_COLOR
            g2.stroke = STROKE_12_ROUND
            g2.drawLine(5f, 3f, 8f, 3f)

            // Button nose
            g2.color = NOSE_BLACK
            g2.fillOval(11f, 4f, 2f, 1.8f)
            return
        }

        // 1. Wagging Tail (Behind body)
        g2.color = dogBody
        g2.stroke = STROKE_28_ROUND
        sharedPath.reset()
        sharedPath.moveTo(-6.0, 3.0)
        sharedPath.quadTo(-10.0 - tailSwing * 0.1, 0.0, -9.0 + tailSwing * 0.2, -5.0 + tailSwing * 0.1)
        g2.draw(sharedPath)

        // 2. Walking Legs
        val legBob = if (walkCycle != 0f) sin(walkCycle).toFloat() * 1.8f else 0f
        g2.color = DOG_WHITE
        g2.fillRoundRect(-5f, 5.5f + legBob, 3.5f, 3.5f, 1.8f, 1.8f)
        g2.fillRoundRect(1.5f, 5.5f - legBob, 3.5f, 3.5f, 1.8f, 1.8f)

        // 3. Body
        val bodyY = -3f + breathing - (if (walkCycle != 0f) kotlin.math.abs(legBob) * 0.4f else 0f)
        g2.color = dogBody
        g2.fillRoundRect(-7f, bodyY, 14f, 10f, 6f, 6f)

        // White chest fluff
        g2.color = DOG_WHITE
        g2.fillOval(-2f, bodyY + 3f, 6f, 6f)

        // 4. Head (Proportional & Centered)
        val headY = bodyY - 7f
        g2.color = dogBody
        g2.fillRoundRect(-6f, headY, 13f, 9.5f, 5f, 5f)

        // Floppy Ears (left & right flanking the head)
        val earBounce = if (!isPaused && state == PetActivityState.EAR_TWITCH) sin(tick * 0.9).toFloat() * 1.8f
            else if (!isPaused && walkCycle != 0f) sin(walkCycle * 2).toFloat() * 1.2f else 0f
        g2.color = dogEar
        g2.fillRoundRect(-8.5f, headY + earBounce + 1f, 3.5f, 7.5f, 2.5f, 2.5f)
        g2.fillRoundRect(5.5f, headY + earBounce + 1f, 3.5f, 7.5f, 2.5f, 2.5f)

        // 5. White Muzzle (Centered at bottom half of face, BELOW eyes!)
        g2.color = DOG_WHITE
        g2.fillRoundRect(-1.5f, headY + 4f, 6f, 4.5f, 3f, 3f)

        // Centered Cute Black Button Nose
        g2.color = NOSE_BLACK
        g2.fillOval(0.5f, headY + 4.2f, 2.2f, 1.8f)

        // 6. Eyes (Positioned ABOVE the muzzle, cleanly spaced!)
        if (!isPaused && (state == PetActivityState.YAWNING || state == PetActivityState.GROOMING)) {
            drawRestingEyes(g2, headY, false)
        } else if (!isPaused && (state == PetActivityState.CODING || state == PetActivityState.PETTING || state == PetActivityState.CELEBRATING || state == PetActivityState.STADIUM_VICTORY || state == PetActivityState.RUN_SUCCEEDED || state == PetActivityState.COMMIT_SUCCEEDED || state == PetActivityState.GREETING || state == PetActivityState.PLAYING)) {
            // Happy smiling curved eyes (⌒ ⌒)
            g2.color = EYE_COLOR
            g2.stroke = STROKE_13_ROUND
            g2.drawArc(-4, headY.toInt() + 2, 3, 3, 0, 180)
            g2.drawArc(2, headY.toInt() + 2, 3, 3, 0, 180)

            // Centered Cute Panting Tongue!
            g2.color = DOG_TONGUE
            val tongueBob = sin(tick * 0.5).toFloat() * 0.8f
            g2.fill(RoundRectangle2D.Float(0.5f, headY + 6.8f, 2.2f, 3f + tongueBob, 1.2f, 1.2f))
        } else if (!isPaused && state == PetActivityState.FAINTED) {
            // X_X eyes
            g2.color = EYE_COLOR
            g2.stroke = STROKE_13_ROUND
            g2.drawLine(-5, headY.toInt() + 1, -2, headY.toInt() + 4)
            g2.drawLine(-2, headY.toInt() + 1, -5, headY.toInt() + 4)
            g2.drawLine(2, headY.toInt() + 1, 5, headY.toInt() + 4)
            g2.drawLine(5, headY.toInt() + 1, 2, headY.toInt() + 4)
        } else if (!isPaused && state == PetActivityState.WATCHING_TESTS) {
            // Nervous / intense squinting eyes (> <)
            g2.color = EYE_COLOR
            g2.stroke = STROKE_13_ROUND
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
                g2.stroke = STROKE_12_ROUND
                g2.drawLine(-4, headY.toInt() + 3, -2, headY.toInt() + 3)
                g2.drawLine(2, headY.toInt() + 3, 4, headY.toInt() + 3)
            } else {
                g2.color = EYE_COLOR
                g2.fill(Ellipse2D.Float(-4.5f + gaze * 0.7f, headY + 1.8f, 3f, 3.5f))
                g2.fill(Ellipse2D.Float(1.8f + gaze * 0.7f, headY + 1.8f, 3f, 3.5f))

                g2.color = EYE_SHINE
                g2.fill(Ellipse2D.Float(-3.8f + gaze * 0.7f, headY + 2.2f, 1.2f, 1.2f))
                g2.fill(Ellipse2D.Float(2.5f + gaze * 0.7f, headY + 2.2f, 1.2f, 1.2f))
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
    private fun drawCat(g2: Graphics2D, state: PetActivityState, tick: Long, walkCycle: Float, isPaused: Boolean, gaze: Float, coat: PetCoat) {
        val catBody = coat.body(PetType.CAT)
        val breathing = if (isPaused) 0f else sin(tick * 0.12).toFloat() * 0.5f
        val tailSwing = if (isPaused) 0f else when (state) {
            PetActivityState.CODING -> sin(tick * 0.45).toFloat() * 22f
            PetActivityState.SLEEPING -> sin(tick * 0.05).toFloat() * 4f
            PetActivityState.CELEBRATING -> sin(tick * 0.6).toFloat() * 30f
            PetActivityState.PETTING -> sin(tick * 0.23).toFloat() * 18f
            PetActivityState.DEBUGGING -> sin(tick * 0.09).toFloat() * 8f
            else -> sin(tick * 0.18).toFloat() * 14f
        }

        if (state == PetActivityState.SLEEPING) {
            g2.color = catBody
            g2.fillRoundRect(-12f, 1.5f + breathing, 24f, 6f, 6f, 6f)

            g2.fillTriangle(1, 2, 3, -1, 5, 2)
            g2.fillTriangle(6, 2, 8, -1, 10, 2)

            g2.color = EYE_COLOR
            g2.stroke = STROKE_12_ROUND
            g2.drawLine(3, 4, 5, 4)
            g2.drawLine(7, 4, 9, 4)
            g2.color = CAT_WHITE
            g2.fillRoundRect(6f, 6f, 7f, 2f, 2f, 2f)
            return
        }

        // Tail
        g2.color = catBody
        g2.stroke = STROKE_26_ROUND
        sharedPath.reset()
        sharedPath.moveTo(-6.0, 3.0)
        sharedPath.curveTo(-13.0, 4.0 - tailSwing * 0.08, -14.0 + tailSwing * 0.12, -9.0, -9.0 + tailSwing * 0.08, -7.0)
        g2.draw(sharedPath)

        // Legs
        val legBob = if (walkCycle != 0f) sin(walkCycle).toFloat() * 1.8f else 0f
        g2.color = CAT_WHITE
        g2.fillRoundRect(-5f, 5.5f + legBob, 3.5f, 3.5f, 1.8f, 1.8f)
        g2.fillRoundRect(1.5f, 5.5f - legBob, 3.5f, 3.5f, 1.8f, 1.8f)

        // Body
        g2.color = catBody
        val bodyY = -3f + breathing - (if (walkCycle != 0f) kotlin.math.abs(legBob) * 0.4f else 0f)
        g2.fillRoundRect(-7f, bodyY, 14f, 10f, 6f, 6f)

        // White belly patch
        g2.color = CAT_WHITE
        g2.fillOval(-2f, bodyY + 3f, 6f, 6f)

        // Head
        val headY = bodyY - 7f
        g2.color = catBody
        g2.fillRoundRect(-6f, headY, 13f, 9.5f, 5f, 5f)

        // Ears with pink inner ear
        val twitch = if (!isPaused && state == PetActivityState.EAR_TWITCH) (sin(tick * 0.9) * 2).toInt() else 0
        val hY = headY.toInt()
        g2.fillTriangle(-6, hY + 1, -3 + twitch, hY - 4, 0, hY + 1)
        g2.fillTriangle(1, hY + 1, 4, hY - 4, 7, hY + 1)

        g2.color = CAT_INNER_EAR
        g2.fillTriangle(-5, hY + 1, -3, hY - 2, -1, hY + 1)
        g2.fillTriangle(2, hY + 1, 4, hY - 2, 6, hY + 1)

        // Eyes
        if (!isPaused && (state == PetActivityState.YAWNING || state == PetActivityState.GROOMING)) {
            drawRestingEyes(g2, headY, false)
        } else if (!isPaused && (state == PetActivityState.CODING || state == PetActivityState.PETTING || state == PetActivityState.CELEBRATING || state == PetActivityState.STADIUM_VICTORY || state == PetActivityState.RUN_SUCCEEDED || state == PetActivityState.COMMIT_SUCCEEDED || state == PetActivityState.GREETING || state == PetActivityState.PLAYING)) {
            g2.color = EYE_COLOR
            g2.stroke = STROKE_13_ROUND
            g2.drawArc(-4, headY.toInt() + 2, 3, 3, 0, 180)
            g2.drawArc(2, headY.toInt() + 2, 3, 3, 0, 180)
        } else if (!isPaused && state == PetActivityState.FAINTED) {
            g2.color = EYE_COLOR
            g2.stroke = STROKE_13_ROUND
            g2.drawLine(-5, headY.toInt() + 1, -2, headY.toInt() + 4)
            g2.drawLine(-2, headY.toInt() + 1, -5, headY.toInt() + 4)
            g2.drawLine(2, headY.toInt() + 1, 5, headY.toInt() + 4)
            g2.drawLine(5, headY.toInt() + 1, 2, headY.toInt() + 4)
        } else if (!isPaused && state == PetActivityState.WATCHING_TESTS) {
            g2.color = EYE_COLOR
            g2.stroke = STROKE_13_ROUND
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
                g2.stroke = STROKE_12_ROUND
                g2.drawLine(-4, headY.toInt() + 3, -2, headY.toInt() + 3)
                g2.drawLine(2, headY.toInt() + 3, 4, headY.toInt() + 3)
            } else {
                g2.color = EYE_COLOR
                g2.fill(Ellipse2D.Float(-4.5f + gaze * 0.7f, headY + 1.8f, 3f, 3.5f))
                g2.fill(Ellipse2D.Float(1.8f + gaze * 0.7f, headY + 1.8f, 3f, 3.5f))

                g2.color = EYE_SHINE
                g2.fill(Ellipse2D.Float(-3.8f + gaze * 0.7f, headY + 2.2f, 1.2f, 1.2f))
                g2.fill(Ellipse2D.Float(2.5f + gaze * 0.7f, headY + 2.2f, 1.2f, 1.2f))
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
    private fun drawDuck(g2: Graphics2D, state: PetActivityState, tick: Long, walkCycle: Float, isPaused: Boolean, gaze: Float, coat: PetCoat) {
        val duckBody = coat.body(PetType.DUCK)
        val duckWing = coat.detail(PetType.DUCK)
        val breathing = if (isPaused) 0f else sin(tick * 0.15).toFloat() * 0.5f
        val wingFlap = if (isPaused) 0f else when (state) {
            PetActivityState.CODING -> sin(tick * 0.5).toFloat() * 2.5f
            PetActivityState.CELEBRATING -> sin(tick * 0.8).toFloat() * 5f
            PetActivityState.GREETING, PetActivityState.PLAYING, PetActivityState.GROOMING -> sin(tick * 0.6).toFloat() * 3f
            PetActivityState.EAR_TWITCH -> sin(tick * 0.9).toFloat() * 2.2f
            else -> sin(tick * 0.18).toFloat() * 1.2f
        }

        if (state == PetActivityState.SLEEPING) {
            g2.color = duckBody
            g2.fillOval(-12f, 1f + breathing, 23f, 7f)
            g2.fillOval(3f, -0.5f + breathing, 9f, 6f)

            g2.color = DUCK_BEAK
            g2.fillRoundRect(9f, 2f + breathing, 4.5f, 2f, 1.5f, 1.5f)

            g2.color = EYE_COLOR
            g2.stroke = STROKE_12_ROUND
            g2.drawLine(5, 2, 8, 2)
            return
        }

        // Tail feather
        g2.color = duckBody
        val tailTwitch = if (isPaused) 0f else (sin(tick * 0.3) * 2).toFloat()
        g2.fillTriangle(-10f, -1f + tailTwitch, -5f, 3f, -5f, -4f)

        // Feet / Waddling
        val footBob = if (walkCycle != 0f) sin(walkCycle).toFloat() * 1.8f else 0f
        g2.color = DUCK_BEAK
        g2.fillRoundRect(-3.5f, 5.5f + footBob, 3.5f, 3f, 1.5f, 1.5f)
        g2.fillRoundRect(1.5f, 5.5f - footBob, 3.5f, 3f, 1.5f, 1.5f)

        // Body
        val bodyY = -3f + breathing - (if (walkCycle != 0f) kotlin.math.abs(footBob) * 0.4f else 0f)
        g2.color = duckBody
        g2.fillOval(-7f, bodyY, 14f, 10f)

        // Wing
        g2.color = duckWing
        g2.fillRoundRect(-3.5f, bodyY + 2f - wingFlap, 7f, 4.5f, 2.5f, 2.5f)

        // Head
        val headY = bodyY - 6.5f
        g2.color = duckBody
        g2.fillOval(-0.5f, headY, 10f, 9f)

        // Orange Bill / Beak
        g2.color = DUCK_BEAK
        g2.fillRoundRect(6.5f, headY + 3.5f, 6.5f, 3.5f, 1.8f, 1.8f)
        g2.color = DUCK_BEAK_SHADOW
        g2.drawLine(7, headY.toInt() + 5, 12, headY.toInt() + 5)

        // Eye
        if (!isPaused && (state == PetActivityState.YAWNING || state == PetActivityState.GROOMING)) {
            drawRestingEyes(g2, headY, true)
        } else if (!isPaused && (state == PetActivityState.CODING || state == PetActivityState.PETTING || state == PetActivityState.CELEBRATING || state == PetActivityState.STADIUM_VICTORY || state == PetActivityState.RUN_SUCCEEDED || state == PetActivityState.COMMIT_SUCCEEDED || state == PetActivityState.GREETING || state == PetActivityState.PLAYING)) {
            g2.color = EYE_COLOR
            g2.stroke = STROKE_13_ROUND
            g2.drawArc(2, headY.toInt() + 2, 3, 3, 0, 180)
        } else if (!isPaused && state == PetActivityState.FAINTED) {
            g2.color = EYE_COLOR
            g2.stroke = STROKE_13_ROUND
            g2.drawLine(2, headY.toInt() + 1, 5, headY.toInt() + 4)
            g2.drawLine(5, headY.toInt() + 1, 2, headY.toInt() + 4)
        } else if (!isPaused && state == PetActivityState.WATCHING_TESTS) {
            g2.color = EYE_COLOR
            g2.stroke = STROKE_13_ROUND
            g2.drawLine(5, headY.toInt() + 1, 2, headY.toInt() + 3)
            g2.drawLine(5, headY.toInt() + 5, 2, headY.toInt() + 3)
        } else if (!isPaused && state == PetActivityState.MEME_WARNING) {
            g2.color = EYE_COLOR
            g2.fillOval(1.5f, headY + 1.2f, 3.8f, 4.2f)
            g2.color = EYE_SHINE
            g2.fillOval(2.5f, headY + 1.8f, 1.8f, 1.8f)
        } else {
            val blink = !isPaused && (tick % 95L in 90L..94L)
            if (blink) {
                g2.color = EYE_COLOR
                g2.stroke = STROKE_12_ROUND
                g2.drawLine(2, headY.toInt() + 3, 5, headY.toInt() + 3)
            } else {
                g2.color = EYE_COLOR
                g2.fillOval(2f + gaze * 0.7f, headY + 1.8f, 3.2f, 3.5f)

                g2.color = EYE_SHINE
                g2.fillOval(3.2f + gaze * 0.7f, headY + 2.2f, 1.2f, 1.2f)
            }
        }
    }

    // =========================================================================
    // CRAB (FERRIS) - The Rustacean Mascot
    // =========================================================================
    private fun drawCrab(g2: Graphics2D, state: PetActivityState, tick: Long, walkCycle: Float, isPaused: Boolean, gaze: Float, coat: PetCoat) {
        val crabBody = coat.body(PetType.CRAB)
        val crabClaw = coat.detail(PetType.CRAB)
        val breathing = if (isPaused) 0f else sin(tick * 0.12).toFloat() * 0.4f

        if (state == PetActivityState.SLEEPING) {
            // Flat, cozy shell resting on mat
            g2.color = crabBody
            g2.fillRoundRect(-12f, 1.5f + breathing, 24f, 6.5f, 6f, 6f)

            // Folded claws
            g2.color = crabClaw
            g2.fillRoundRect(-14f, 2.5f + breathing, 4.5f, 5f, 2.5f, 2.5f)
            g2.fillRoundRect(9.5f, 2.5f + breathing, 4.5f, 5f, 2.5f, 2.5f)

            // Sleeping eye slits (- -) on little lowered stalks
            g2.color = EYE_COLOR
            g2.stroke = STROKE_12_ROUND
            g2.drawLine(-4, 2, -1, 2)
            g2.drawLine(1, 2, 4, 2)
            return
        }

        // 1. Scuttling Walking Legs (under/behind shell)
        val scuttleL = if (walkCycle != 0f) sin(walkCycle * 2.5f) * 2f else 0f
        val scuttleR = if (walkCycle != 0f) cos(walkCycle * 2.5f) * 2f else 0f
        g2.color = crabClaw
        g2.stroke = STROKE_18_ROUND

        // 3 legs on left side, 3 legs on right side
        g2.drawLine(-5f, 4f, -9f, 7.5f + scuttleL)
        g2.drawLine(-3f, 4.5f, -6f, 7.5f - scuttleL)
        g2.drawLine(-1f, 4.5f, -3f, 7.5f + scuttleL)

        g2.drawLine(1f, 4.5f, 3f, 7.5f - scuttleR)
        g2.drawLine(3f, 4.5f, 6f, 7.5f + scuttleR)
        g2.drawLine(5f, 4f, 9f, 7.5f - scuttleR)

        // 2. Rounded Shell (Carapace)
        val bodyY = -3f + breathing - (if (walkCycle != 0f) kotlin.math.abs(scuttleL) * 0.25f else 0f)
        g2.color = crabBody
        g2.fillRoundRect(-7.5f, bodyY, 15f, 9.5f, 7f, 7f)

        // Lighter under-shell highlight
        g2.color = Color(
            (crabBody.red * 1.08).toInt().coerceIn(0, 255),
            (crabBody.green * 1.12).toInt().coerceIn(0, 255),
            (crabBody.blue * 1.15).toInt().coerceIn(0, 255)
        )
        g2.fillRoundRect(-4.5f, bodyY + 3.5f, 9f, 4.5f, 4f, 4f)

        // 3. Eyestalks (Protruding from top of shell)
        val stalkDroop = if (!isPaused && state == PetActivityState.FAINTED) 1.5f else 0f
        val headY = bodyY - 7f + stalkDroop
        g2.color = crabBody
        g2.fillRoundRect(-4.2f, headY + 2.5f, 2.5f, 4.5f, 1.2f, 1.2f)
        g2.fillRoundRect(1.7f, headY + 2.5f, 2.5f, 4.5f, 1.2f, 1.2f)

        // Eyeball balls at tip of stalks
        g2.fillOval(-4.5f, headY + 1f, 3.2f, 3.2f)
        g2.fillOval(1.4f, headY + 1f, 3.2f, 3.2f)

        // 4. Eyes on stalks
        if (!isPaused && (state == PetActivityState.YAWNING || state == PetActivityState.GROOMING)) {
            drawRestingEyes(g2, headY, false)
        } else if (!isPaused && (state == PetActivityState.CODING || state == PetActivityState.PETTING || state == PetActivityState.CELEBRATING || state == PetActivityState.STADIUM_VICTORY || state == PetActivityState.RUN_SUCCEEDED || state == PetActivityState.COMMIT_SUCCEEDED || state == PetActivityState.GREETING || state == PetActivityState.PLAYING)) {
            g2.color = EYE_COLOR
            g2.stroke = STROKE_13_ROUND
            g2.drawArc(-4, headY.toInt() + 1, 3, 3, 0, 180)
            g2.drawArc(2, headY.toInt() + 1, 3, 3, 0, 180)
        } else if (!isPaused && state == PetActivityState.FAINTED) {
            g2.color = EYE_COLOR
            g2.stroke = STROKE_13_ROUND
            g2.drawLine(-5, headY.toInt() + 1, -2, headY.toInt() + 4)
            g2.drawLine(-2, headY.toInt() + 1, -5, headY.toInt() + 4)
            g2.drawLine(2, headY.toInt() + 1, 5, headY.toInt() + 4)
            g2.drawLine(5, headY.toInt() + 1, 2, headY.toInt() + 4)
        } else if (!isPaused && state == PetActivityState.WATCHING_TESTS) {
            g2.color = EYE_COLOR
            g2.stroke = STROKE_13_ROUND
            g2.drawLine(-5, headY.toInt() + 1, -2, headY.toInt() + 3)
            g2.drawLine(-5, headY.toInt() + 5, -2, headY.toInt() + 3)
            g2.drawLine(5, headY.toInt() + 1, 2, headY.toInt() + 3)
            g2.drawLine(5, headY.toInt() + 5, 2, headY.toInt() + 3)
        } else if (!isPaused && state == PetActivityState.MEME_WARNING) {
            g2.color = EYE_COLOR
            g2.fillOval(-5f, headY + 0.8f, 3.8f, 4f)
            g2.fillOval(1.5f, headY + 0.8f, 3.8f, 4f)
            g2.color = EYE_SHINE
            g2.fillOval(-4f, headY + 1.2f, 1.6f, 1.6f)
            g2.fillOval(2.5f, headY + 1.2f, 1.6f, 1.6f)
        } else {
            val blink = !isPaused && (tick % 100L in 95L..99L)
            if (blink) {
                g2.color = EYE_COLOR
                g2.stroke = STROKE_12_ROUND
                g2.drawLine(-4, headY.toInt() + 2, -2, headY.toInt() + 2)
                g2.drawLine(2, headY.toInt() + 2, 4, headY.toInt() + 2)
            } else {
                g2.color = EYE_COLOR
                g2.fillOval(-4.3f + gaze * 0.6f, headY + 1.2f, 2.8f, 3.2f)
                g2.fillOval(1.6f + gaze * 0.6f, headY + 1.2f, 2.8f, 3.2f)

                g2.color = EYE_SHINE
                g2.fillOval(-3.7f + gaze * 0.6f, headY + 1.5f, 1.1f, 1.1f)
                g2.fillOval(2.2f + gaze * 0.6f, headY + 1.5f, 1.1f, 1.1f)
            }
        }

        // Cute smiling mouth
        if (!isPaused && state != PetActivityState.YAWNING) {
            g2.color = EYE_COLOR
            g2.stroke = STROKE_11_ROUND
            g2.drawArc(-1, (bodyY + 2.5f).toInt(), 3, 2, 180, 180)
        }

        // 5. Claws (Chelae) with animated gestures
        drawCrabClaws(g2, state, tick, isPaused, bodyY, crabClaw, crabBody)
    }

    private fun drawCrabClaws(
        g2: Graphics2D, state: PetActivityState, tick: Long, isPaused: Boolean,
        bodyY: Float, clawColor: Color, armColor: Color
    ) {
        g2.stroke = STROKE_22_ROUND
        g2.color = armColor

        val snap = if (isPaused) 0f else sin(tick * 0.4).toFloat() * 1.5f

        when (state) {
            PetActivityState.CODING -> {
                // Alternating fast typing clatter on the mini keyboard!
                val tapL = if (isPaused) 0f else sin(tick * 0.85).toFloat() * 2.2f
                val tapR = if (isPaused) 0f else cos(tick * 0.85).toFloat() * 2.2f
                g2.drawLine(-6f, bodyY + 3f, -1f, bodyY + 4f - tapL)
                g2.drawLine(6f, bodyY + 3f, 5f, bodyY + 4f - tapR)
                g2.color = clawColor
                drawPincer(g2, 0f, bodyY + 4f - tapL, 0.4f)
                drawPincer(g2, 5.5f, bodyY + 4f - tapR, 0.4f)
            }
            PetActivityState.CELEBRATING, PetActivityState.STADIUM_VICTORY, PetActivityState.PETTING, PetActivityState.GREETING, PetActivityState.PLAYING -> {
                // Claws raised high in victory / happy cheer!
                val wave = if (isPaused) 0f else sin(tick * 0.6).toFloat() * 2f
                g2.drawLine(-6f, bodyY + 2f, -9f, bodyY - 4f + wave)
                g2.drawLine(6f, bodyY + 2f, 9f, bodyY - 4f - wave)
                g2.color = clawColor
                drawPincer(g2, -10f, bodyY - 5f + wave, 0.8f + snap * 0.2f)
                drawPincer(g2, 10f, bodyY - 5f - wave, 0.8f - snap * 0.2f)
            }
            PetActivityState.FAINTED -> {
                // Drooped outwards flat on ground
                g2.drawLine(-6f, bodyY + 4f, -11f, 6.5f)
                g2.drawLine(6f, bodyY + 4f, 11f, 6.5f)
                g2.color = clawColor
                drawPincer(g2, -12f, 6.5f, 0.1f)
                drawPincer(g2, 12f, 6.5f, 0.1f)
            }
            else -> {
                // Idle resting / scuttling pose: held at sides, ready to snip
                g2.drawLine(-6f, bodyY + 2.5f, -9.5f, bodyY + 0.5f + snap)
                g2.drawLine(6f, bodyY + 2.5f, 9.5f, bodyY + 0.5f - snap)
                g2.color = clawColor
                drawPincer(g2, -10.5f, bodyY + snap, 0.5f + snap * 0.15f)
                drawPincer(g2, 10.5f, bodyY - snap, 0.5f - snap * 0.15f)
            }
        }
    }

    private fun drawPincer(g: Graphics2D, x: Float, y: Float, openAmount: Float) {
        val pinch = openAmount.coerceIn(0.1f, 1.2f)
        g.fillRoundRect(x - 2f, y - 1f - pinch * 1.5f, 4f, 2.5f, 1.5f, 1.5f)
        g.fillRoundRect(x - 2f, y + 0.5f + pinch * 1.5f, 4f, 2.5f, 1.5f, 1.5f)
        g.fillOval(x - 1.8f, y - 1.2f, 3.6f, 3.6f)
    }

    // =========================================================================
    // ACCESSORIES (Visual Unlocks on Level Up)
    // =========================================================================
    private fun drawAccessories(g2: Graphics2D, activeAccessories: Set<String>, petType: PetType, tick: Long) {
        val original = g2.transform
        val bob = sin(tick * if (petType == PetType.DUCK) 0.15 else 0.12) * 0.5
        g2.translate(if (petType == PetType.DUCK) 4.0 else 0.0, bob + if (petType == PetType.DUCK) 0.5 else 0.0)
        if (petType == PetType.DUCK) g2.scale(0.78, 1.0)
        val headY = -10f

        // 1. Dapper Bowtie (Level 2+)
        if (activeAccessories.contains(PetAccessory.BOWTIE.id)) {
            val neckY = 0f
            g2.color = Color(0xFF, 0x47, 0x57)
            g2.fillTriangle(-1f, neckY, -4f, neckY - 2f, -4f, neckY + 2f)
            g2.fillTriangle(1f, neckY, 4f, neckY - 2f, 4f, neckY + 2f)
            g2.color = Color(0xD6, 0x30, 0x31)
            g2.fillOval(-1, neckY.toInt() - 1, 3, 3)
        }

        // 2. Headphones (Level 3+)
        if (activeAccessories.contains(PetAccessory.HEADPHONES.id)) {
            g2.color = HEADPHONES_BAND
            g2.stroke = STROKE_18_ROUND
            g2.drawArc(-6, headY.toInt() - 2, 12, 10, 20, 140)

            // Neon cyan ear-cups
            g2.color = HEADPHONES_CUP
            g2.fillRoundRect(-7.5f, headY + 1f, 2.5f, 5f, 1.5f, 1.5f)
            g2.fillRoundRect(5f, headY + 1f, 2.5f, 5f, 1.5f, 1.5f)
        }

        // 3. Glasses (Level 5+)
        if (activeAccessories.contains(PetAccessory.GLASSES.id)) {
            g2.color = GLASSES_FRAME
            g2.stroke = STROKE_12_ROUND
            if (petType == PetType.DUCK) {
                g2.drawRoundRect(-3, headY.toInt() + 1, 5, 4, 2, 2)
                g2.drawLine(-3, headY.toInt() + 3, -6, headY.toInt() + 1)
            } else {
                g2.drawRoundRect(-5, headY.toInt() + 1, 4, 3, 2, 2)
                g2.drawRoundRect(1, headY.toInt() + 1, 4, 3, 2, 2)
                g2.drawLine(-1, headY.toInt() + 3, 1, headY.toInt() + 3)
            }
        }

        // 4. Wizard Hat (Level 7+)
        if (activeAccessories.contains(PetAccessory.WIZARD_HAT.id)) {
            g2.color = HAT_PURPLE
            // Brim
            g2.fillOval(-6f, headY - 1f, 12f, 3f)
            // Cone
            g2.fillTriangle(-4f, headY, 0f, headY - 8f, 4f, headY)
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
            g2.stroke = STROKE_14_PLAIN
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
        g2.transform = original
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
        g2.stroke = STROKE_08_PLAIN
        g2.draw(finger)

        g2.color = Color.WHITE
        g2.font = Font("Segoe UI", Font.BOLD, 5)
        g2.drawString("#1", 5.5f, -3.5f)
    }

    private fun drawRedCard(g2: Graphics2D) {
        g2.color = Color(0xE7, 0x4C, 0x3C)
        g2.fill(RoundRectangle2D.Float(6f, -10f, 6.5f, 9f, 1f, 1f))
        g2.color = Color(0xC0, 0x39, 0x2B)
        g2.stroke = STROKE_08_PLAIN
        g2.draw(RoundRectangle2D.Float(6f, -10f, 6.5f, 9f, 1f, 1f))
    }

    private fun drawHazardSign(g2: Graphics2D) {
        val sign = Polygon(intArrayOf(6, 12, 18), intArrayOf(-2, -12, -2), 3)
        g2.color = Color(0xF1, 0xC4, 0x0F)
        g2.fill(sign)
        g2.color = Color(0xD6, 0x89, 0x10)
        g2.stroke = STROKE_08_PLAIN
        g2.draw(sign)

        g2.color = Color(0x1E, 0x27, 0x2C)
        g2.fillRect(11, -9, 2, 3)
        g2.fillRect(11, -4, 2, 2)
    }

    private fun drawWastebasket(g2: Graphics2D, tick: Long) {
        val phase = (tick % 28L) / 28f
        g2.color = Color(0x7F, 0x8C, 0x8D)
        val bin = Polygon(intArrayOf(-18, -12, -13, -17), intArrayOf(1, 1, 8, 8), 4)
        g2.fill(bin)
        g2.color = Color(0x95, 0xA5, 0xA6)
        g2.draw(bin)

        g2.drawLine(-19, 1, -11, 1)
        g2.color = Color.WHITE
        // One deterministic arc ends at the bin mouth, including after the pet turns.
        val paperX = -2f - 13f * phase
        val paperY = 1f - sin(phase * Math.PI).toFloat() * 8f
        g2.fill(Ellipse2D.Float(paperX - 1.5f, paperY - 1.5f, 3f, 3f))
    }

    private fun drawSaveStamp(g2: Graphics2D, tick: Long) {
        val stampY = -15f + sin(tick * 0.2).toFloat() * 1.2f
        g2.color = Color(0x2E, 0xCC, 0x71)
        g2.fill(RoundRectangle2D.Float(-12f, stampY, 24f, 7.5f, 3f, 3f))
        g2.color = Color(0x27, 0xAE, 0x60)
        g2.stroke = STROKE_08_PLAIN
        g2.draw(RoundRectangle2D.Float(-12f, stampY, 24f, 7.5f, 3f, 3f))

        g2.color = Color.WHITE
        g2.font = Font("Segoe UI", Font.BOLD, 5)
        g2.drawString("✓ SAVED", -10f, stampY + 5.5f)
    }
}
