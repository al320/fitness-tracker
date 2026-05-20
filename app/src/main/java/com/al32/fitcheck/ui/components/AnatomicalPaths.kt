package com.al32.fitcheck.ui.components

import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.vector.PathParser
import com.al32.fitcheck.domain.physiology.backPaths
import com.al32.fitcheck.domain.physiology.frontPaths
import com.al32.fitcheck.domain.physiology.MuscleGroup

object AnatomicalPaths {
    // Athletic proportions matching ViewBox 0 0 200 520
    
    private val pathData = mapOf(
        // ── FRONT MUSCLES ──
        "pec_upper_l" to "M105,100 C125,95 140,105 148,120 L105,130 Z",
        "pec_upper_r" to "M95,100 C75,95 60,105 52,120 L95,130 Z",
        "pec_lower_l" to "M105,130 L148,120 C155,150 145,180 105,185 Z",
        "pec_lower_r" to "M95,130 L52,120 C45,150 55,180 95,185 Z",
        
        "ant_delt_l" to "M150,105 C165,110 172,135 160,155 L145,135 Z",
        "ant_delt_r" to "M50,105 C35,110 28,135 40,155 L55,135 Z",
        
        "lat_delt_l" to "M162,120 C182,130 175,165 162,185 L152,150 Z",
        "lat_delt_r" to "M38,120 C18,130 25,165 38,185 L48,150 Z",
        
        "bicep_l" to "M155,165 C175,175 170,230 155,245 L145,210 Z",
        "bicep_r" to "M45,165 C25,175 30,230 45,245 L55,210 Z",
        
        "forearm_front_l" to "M150,255 C165,265 160,320 150,340 L140,290 Z",
        "forearm_front_r" to "M50,255 C35,265 40,320 50,340 L60,290 Z",
        
        "abs_upper" to "M88,190 L112,190 L112,215 L88,215 Z",
        "abs_mid" to "M88,220 L112,220 L112,245 L88,245 Z",
        "abs_lower" to "M88,250 L112,250 L108,285 L92,285 Z",
        "oblique_l" to "M118,200 C135,215 130,275 115,295 L110,245 Z",
        "oblique_r" to "M82,200 C65,215 70,275 85,295 L90,245 Z",
        
        "quad_l" to "M105,320 C140,330 155,420 135,465 L108,465 Z",
        "quad_r" to "M95,320 C60,330 45,420 65,465 L92,465 Z",
        
        "tibialis_l" to "M110,475 L130,475 L120,510 L110,510 Z",
        "tibialis_r" to "M90,475 L70,475 L80,510 L90,510 Z",

        // ── BACK MUSCLES ──
        "trap_upper_l" to "M100,75 L125,100 L115,120 L100,110 Z",
        "trap_upper_r" to "M100,75 L75,100 L85,120 L100,110 Z",
        "trap_mid" to "M85,115 L115,115 L110,165 L90,165 Z",
        
        "rear_delt_l" to "M150,110 C165,120 160,145 145,155 L140,125 Z",
        "rear_delt_r" to "M50,110 C35,120 40,145 55,155 L60,125 Z",
        
        "lat_l" to "M115,140 C145,150 150,240 125,270 L115,255 Z",
        "lat_r" to "M85,140 C55,150 50,240 75,270 L85,255 Z",
        
        "lower_back" to "M92,265 L108,265 L106,305 L94,305 Z",
        
        "tricep_l" to "M155,160 C175,170 170,230 155,240 L145,200 Z",
        "tricep_r" to "M45,160 C25,170 30,230 45,240 L55,200 Z",
        
        "glute_l" to "M102,300 C145,310 150,380 105,395 Z",
        "glute_r" to "M98,300 C55,310 50,380 95,395 Z",
        
        "hamstring_l" to "M105,405 C140,415 135,470 120,485 L105,485 Z",
        "hamstring_r" to "M95,405 C60,415 65,470 80,485 L95,485 Z",
        
        "calf_l" to "M110,490 C130,495 125,515 110,515 Z",
        "calf_r" to "M90,490 C70,495 75,515 90,515 Z"
    )

    private const val frontSilhouette = "M100,15 C112,15 122,25 122,42 C122,55 118,65 112,70 C125,75 145,85 155,105 C170,120 185,150 190,190 L185,260 C185,280 175,310 165,330 L168,480 C168,500 155,510 140,510 L115,505 L102,490 L100,485 L98,490 L85,505 L60,510 C45,510 32,500 32,480 L35,330 C25,310 15,280 15,260 L10,190 C15,150 30,120 45,105 C55,85 75,75 88,70 C82,65 78,55 78,42 C78,25 88,15 100,15 Z"
    private const val backSilhouette = "M100,15 C112,15 122,25 122,42 C122,55 118,65 112,70 C125,75 145,85 155,105 C170,120 185,150 190,190 L185,260 C185,280 175,310 165,330 L168,480 C168,500 155,510 140,510 L115,505 L102,490 L100,485 L98,490 L85,505 L60,510 C45,510 32,500 32,480 L35,330 C25,310 15,280 15,260 L10,190 C15,150 30,120 45,105 C55,85 75,75 88,70 C82,65 78,55 78,42 C78,25 88,15 100,15 Z"

    fun getMusclePaths(group: MuscleGroup, isFront: Boolean): List<Path> {
        val names: List<String> = if (isFront) group.frontPaths() else group.backPaths()
        return names.mapNotNull { name: String ->
            pathData[name]?.let { data: String ->
                PathParser().parsePathString(data).toPath()
            }
        }
    }

    fun getSilhouette(isFront: Boolean): Path {
        return PathParser().parsePathString(if (isFront) frontSilhouette else backSilhouette).toPath()
    }
}
