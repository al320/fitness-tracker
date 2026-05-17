package com.al32.fitcheck.ui.components

import androidx.compose.ui.graphics.Path
import com.al32.fitcheck.domain.physiology.MuscleGroup

object AnatomicalPaths {
    fun getMusclePaths(group: MuscleGroup, w: Float, h: Float): List<Path> {
        return listOf(
            createPath(group, w, h, isLeft = true),
            createPath(group, w, h, isLeft = false)
        )
    }

    private fun createPath(group: MuscleGroup, w: Float, h: Float, isLeft: Boolean): Path = Path().apply {
        val c = w * 0.5f
        fun fX(x: Float): Float = if (isLeft) c - (x - c) else x

        when (group) {
            MuscleGroup.UPPER_CHEST -> {
                moveTo(fX(c + w * 0.02f), h * 0.22f)
                cubicTo(fX(c + w * 0.08f), h * 0.21f, fX(c + w * 0.14f), h * 0.22f, fX(c + w * 0.18f), h * 0.25f)
                lineTo(fX(c + w * 0.16f), h * 0.26f)
                quadraticTo(fX(c + w * 0.08f), h * 0.25f, fX(c + w * 0.02f), h * 0.26f)
                close()
            }
            MuscleGroup.LOWER_CHEST -> {
                moveTo(fX(c + w * 0.02f), h * 0.27f)
                quadraticTo(fX(c + w * 0.12f), h * 0.26f, fX(c + w * 0.18f), h * 0.27f)
                lineTo(fX(c + w * 0.16f), h * 0.35f)
                quadraticTo(fX(c + w * 0.10f), h * 0.38f, fX(c + w * 0.02f), h * 0.36f)
                close()
            }
            MuscleGroup.FRONT_DELTS -> {
                moveTo(fX(c + w * 0.18f), h * 0.20f)
                cubicTo(fX(c + w * 0.25f), h * 0.21f, fX(c + w * 0.26f), h * 0.28f, fX(c + w * 0.22f), h * 0.32f)
                lineTo(fX(c + w * 0.16f), h * 0.26f)
                close()
            }
            MuscleGroup.SIDE_DELTS -> {
                moveTo(fX(c + w * 0.24f), h * 0.24f)
                cubicTo(fX(c + w * 0.32f), h * 0.26f, fX(c + w * 0.30f), h * 0.35f, fX(c + w * 0.26f), h * 0.38f)
                lineTo(fX(c + w * 0.21f), h * 0.32f)
                close()
            }
            MuscleGroup.BICEPS -> {
                moveTo(fX(c + w * 0.20f), h * 0.34f)
                quadraticTo(fX(c + w * 0.28f), h * 0.38f, fX(c + w * 0.24f), h * 0.48f)
                lineTo(fX(c + w * 0.16f), h * 0.46f)
                close()
            }
            MuscleGroup.ABS -> {
                if (isLeft) {
                    moveTo(c - w * 0.06f, h * 0.38f)
                    lineTo(c + w * 0.06f, h * 0.38f)
                    lineTo(c + w * 0.05f, h * 0.58f)
                    lineTo(c - w * 0.05f, h * 0.58f)
                    close()
                }
            }
            MuscleGroup.OBLIQUES -> {
                moveTo(fX(c + w * 0.07f), h * 0.40f)
                lineTo(fX(c + w * 0.12f), h * 0.42f)
                lineTo(fX(c + w * 0.10f), h * 0.58f)
                lineTo(fX(c + w * 0.06f), h * 0.57f)
                close()
            }
            MuscleGroup.QUADS -> {
                moveTo(fX(c + w * 0.04f), h * 0.62f)
                cubicTo(fX(c + w * 0.18f), h * 0.65f, fX(c + w * 0.22f), h * 0.78f, fX(c + w * 0.14f), h * 0.88f)
                lineTo(fX(c + w * 0.06f), h * 0.88f)
                close()
            }
            MuscleGroup.LATS -> {
                moveTo(fX(c + w * 0.08f), h * 0.28f)
                quadraticTo(fX(c + w * 0.22f), h * 0.35f, fX(c + w * 0.18f), h * 0.55f)
                lineTo(fX(c + w * 0.08f), h * 0.52f)
                close()
            }
            MuscleGroup.UPPER_BACK -> {
                moveTo(fX(c + w * 0.02f), h * 0.16f)
                lineTo(fX(c + w * 0.12f), h * 0.21f)
                lineTo(fX(c + w * 0.1f), h * 0.32f)
                lineTo(fX(c + w * 0.02f), h * 0.30f)
                close()
            }
            MuscleGroup.GLUTES -> {
                moveTo(fX(c + w * 0.02f), h * 0.58f)
                cubicTo(fX(c + w * 0.24f), h * 0.60f, fX(c + w * 0.26f), h * 0.72f, fX(c + w * 0.02f), h * 0.75f)
                close()
            }
            MuscleGroup.HAMSTRINGS -> {
                moveTo(fX(c + w * 0.06f), h * 0.76f)
                lineTo(fX(c + w * 0.18f), h * 0.76f)
                lineTo(fX(c + w * 0.14f), h * 0.92f)
                lineTo(fX(c + w * 0.08f), h * 0.92f)
                close()
            }
            MuscleGroup.CALVES -> {
                moveTo(fX(c + w * 0.06f), h * 0.89f)
                quadraticTo(fX(c + w * 0.13f), h * 0.91f, fX(c + w * 0.10f), h * 0.97f)
                lineTo(fX(c + w * 0.04f), h * 0.97f)
                close()
            }
            else -> {}
        }
    }

    fun drawLeanOutline(w: Float, h: Float): Path = Path().apply {
        val c = w * 0.5f
        // Head
        moveTo(c, h * 0.04f)
        cubicTo(c + w * 0.06f, h * 0.04f, c + w * 0.05f, h * 0.12f, c + w * 0.04f, h * 0.14f)
        // Traps
        lineTo(c + w * 0.14f, h * 0.18f)
        // Shoulder Cap
        cubicTo(c + w * 0.25f, h * 0.20f, c + w * 0.28f, h * 0.32f, c + w * 0.26f, h * 0.38f)
        // Arm Taper
        lineTo(c + w * 0.34f, h * 0.52f)
        lineTo(c + w * 0.28f, h * 0.54f)
        lineTo(c + w * 0.18f, h * 0.36f)
        // Lat/Waist
        cubicTo(c + w * 0.16f, h * 0.48f, c + w * 0.12f, h * 0.54f, c + w * 0.08f, h * 0.58f)
        // Hips/Leg
        cubicTo(c + w * 0.22f, h * 0.65f, c + w * 0.24f, h * 0.88f, c + w * 0.12f, h * 0.98f)
        lineTo(c + w * 0.02f, h * 0.98f)
        // Inseam
        lineTo(c, h * 0.68f)
        
        // Mirroring
        lineTo(c - w * 0.02f, h * 0.98f)
        lineTo(c - w * 0.12f, h * 0.98f)
        cubicTo(c - w * 0.24f, h * 0.88f, c - w * 0.22f, h * 0.65f, c - w * 0.08f, h * 0.58f)
        cubicTo(c - w * 0.12f, h * 0.54f, c - w * 0.16f, h * 0.48f, c - w * 0.18f, h * 0.36f)
        lineTo(c - w * 0.28f, h * 0.54f)
        lineTo(c - w * 0.34f, h * 0.52f)
        lineTo(c - w * 0.26f, h * 0.38f)
        cubicTo(c - w * 0.28f, h * 0.32f, c - w * 0.25f, h * 0.20f, c - w * 0.14f, h * 0.18f)
        lineTo(c - w * 0.14f, h * 0.18f) // Redundant point fix
        lineTo(c - w * 0.04f, h * 0.14f)
        cubicTo(c - w * 0.05f, h * 0.12f, c - w * 0.06f, h * 0.04f, c, h * 0.04f)
        close()
    }
}
