package com.github.pet.pycharm.icons

import java.awt.AlphaComposite
import java.awt.Color
import java.awt.Font
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.geom.GeneralPath
import kotlin.math.sin

enum class ParticleType {
    ZZZ,
    HEART,
    SPARKLE,
    SWEAT,
    PAPER_BALL,
    CONFETTI
}

data class Particle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var alpha: Float,
    val type: ParticleType,
    val text: String = "Z",
    val maxAge: Int = 45,
    var age: Int = 0,
    val color: Color = Color.WHITE
)

class PetParticleManager {

    // All animation work runs on Swing's event thread; copying on each update is unnecessary.
    private val particles = ArrayList<Particle>(64)
    private fun addParticle(particle: Particle) { if (particles.size < 64) particles.add(particle) }
    private val zFont = Font("Segoe UI", Font.BOLD, 10)
    private val zColor = Color(0x70, 0xA1, 0xFF)
    private val heartColor = Color(0xFF, 0x47, 0x57)
    private val sparkleColor = Color(0xFF, 0xD3, 0x2A)
    private val sweatColor = Color(0x34, 0x98, 0xDB)
    private val paperColor = Color(0xFD, 0xFD, 0xFD)
    private val confettiColors = listOf(
        Color(0x2E, 0xCC, 0x71),
        Color(0x00, 0xD2, 0xD3),
        Color(0xFF, 0xD3, 0x2A),
        Color(0xFF, 0x76, 0x75),
        Color(0x9B, 0x59, 0xB6)
    )

    fun spawnZzz(x: Float, y: Float) {
        addParticle(
            Particle(
                x = x + (Math.random().toFloat() * 4f - 2f),
                y = y,
                vx = 0.25f,
                vy = -0.45f,
                alpha = 1.0f,
                type = ParticleType.ZZZ,
                text = "Zzz",
                maxAge = 40
            )
        )
    }

    fun spawnHeart(x: Float, y: Float) {
        addParticle(
            Particle(
                x = x + (Math.random().toFloat() * 6f - 3f),
                y = y,
                vx = (Math.random().toFloat() * 0.4f - 0.2f),
                vy = -0.6f,
                alpha = 1.0f,
                type = ParticleType.HEART,
                maxAge = 35
            )
        )
    }

    fun spawnSparkle(x: Float, y: Float) {
        addParticle(
            Particle(
                x = x + (Math.random().toFloat() * 8f - 4f),
                y = y + (Math.random().toFloat() * 4f - 2f),
                vx = (Math.random().toFloat() * 0.4f - 0.2f),
                vy = -0.5f,
                alpha = 1.0f,
                type = ParticleType.SPARKLE,
                maxAge = 25
            )
        )
    }

    fun spawnSweat(x: Float, y: Float) {
        addParticle(
            Particle(
                x = x + (Math.random().toFloat() * 4f - 2f),
                y = y,
                vx = (Math.random().toFloat() * 0.2f - 0.1f),
                vy = -0.3f,
                alpha = 1.0f,
                type = ParticleType.SWEAT,
                maxAge = 28
            )
        )
    }

    fun spawnPaperBall(x: Float, y: Float) {
        addParticle(
            Particle(
                x = x,
                y = y,
                vx = -0.9f - (Math.random().toFloat() * 0.5f), // Toss backwards
                vy = -1.2f, // Arc upwards then falls
                alpha = 1.0f,
                type = ParticleType.PAPER_BALL,
                maxAge = 25
            )
        )
    }

    fun spawnConfetti(x: Float, y: Float) {
        val col = confettiColors.random()
        addParticle(
            Particle(
                x = x + (Math.random().toFloat() * 12f - 6f),
                y = y,
                vx = (Math.random().toFloat() * 1.6f - 0.8f),
                vy = -1.5f - (Math.random().toFloat() * 0.8f),
                alpha = 1.0f,
                type = ParticleType.CONFETTI,
                maxAge = 32,
                color = col
            )
        )
    }

    fun update() {
        val iterator = particles.iterator()
        while (iterator.hasNext()) {
            val p = iterator.next()
            p.age++
            if (p.type == ParticleType.PAPER_BALL) {
                p.x += p.vx
                p.y += p.vy
                p.vy += 0.16f // Gravity arc downwards
            } else if (p.type == ParticleType.CONFETTI) {
                p.x += p.vx + sin(p.age * 0.4).toFloat() * 0.2f
                p.y += p.vy
                p.vy += 0.12f // Flutter down
            } else {
                p.x += p.vx + sin(p.age * 0.2).toFloat() * 0.15f
                p.y += p.vy
            }
            p.alpha = (1f - (p.age.toFloat() / p.maxAge)).coerceIn(0f, 1f)
            if (p.age >= p.maxAge) {
                iterator.remove()
            }
        }
    }

    fun render(g2: Graphics2D) {
        if (particles.isEmpty()) return

        val origComposite = g2.composite
        val origHints = g2.renderingHints
        val origFont = g2.font
        val origColor = g2.color

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)

        for (p in particles) {
            if (p.alpha <= 0.05f) continue
            g2.composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, p.alpha)

            when (p.type) {
                ParticleType.ZZZ -> {
                    g2.color = zColor
                    g2.font = zFont
                    g2.drawString(p.text, p.x, p.y)
                }
                ParticleType.HEART -> {
                    g2.color = heartColor
                    drawMiniHeart(g2, p.x, p.y)
                }
                ParticleType.SPARKLE -> {
                    g2.color = sparkleColor
                    drawMiniSparkle(g2, p.x, p.y)
                }
                ParticleType.SWEAT -> {
                    g2.color = sweatColor
                    drawMiniSweat(g2, p.x, p.y)
                }
                ParticleType.PAPER_BALL -> {
                    g2.color = paperColor
                    drawMiniPaperBall(g2, p.x, p.y)
                }
                ParticleType.CONFETTI -> {
                    g2.color = p.color
                    g2.fillRect(p.x.toInt(), p.y.toInt(), 2, 3)
                }
            }
        }

        g2.composite = origComposite
        g2.setRenderingHints(origHints)
        g2.font = origFont
        g2.color = origColor
    }

    private fun drawMiniHeart(g2: Graphics2D, x: Float, y: Float) {
        val path = GeneralPath()
        path.moveTo(x.toDouble(), (y + 1.5f).toDouble())
        path.curveTo((x - 2.5f).toDouble(), (y - 2f).toDouble(), (x - 3.5f).toDouble(), (y + 0.5f).toDouble(), x.toDouble(), (y + 3.5f).toDouble())
        path.curveTo((x + 3.5f).toDouble(), (y + 0.5f).toDouble(), (x + 2.5f).toDouble(), (y - 2f).toDouble(), x.toDouble(), (y + 1.5f).toDouble())
        g2.fill(path)
    }

    private fun drawMiniSparkle(g2: Graphics2D, x: Float, y: Float) {
        val s = 2.5f
        g2.drawLine((x - s).toInt(), y.toInt(), (x + s).toInt(), y.toInt())
        g2.drawLine(x.toInt(), (y - s).toInt(), x.toInt(), (y + s).toInt())
    }

    private fun drawMiniSweat(g2: Graphics2D, x: Float, y: Float) {
        val path = GeneralPath()
        path.moveTo(x.toDouble(), y.toDouble())
        path.curveTo((x - 1.5f).toDouble(), (y + 2f).toDouble(), (x + 1.5f).toDouble(), (y + 2f).toDouble(), x.toDouble(), y.toDouble())
        g2.fill(path)
        g2.fillOval((x - 1.2f).toInt(), (y + 1.5f).toInt(), 2, 2)
    }

    private fun drawMiniPaperBall(g2: Graphics2D, x: Float, y: Float) {
        g2.fillOval((x - 2f).toInt(), (y - 2f).toInt(), 4, 4)
        g2.color = Color(0x9E, 0x9E, 0x9E)
        g2.drawLine((x - 1f).toInt(), (y - 1f).toInt(), (x + 1f).toInt(), (y + 1f).toInt())
    }

    fun clear() {
        particles.clear()
    }
}
