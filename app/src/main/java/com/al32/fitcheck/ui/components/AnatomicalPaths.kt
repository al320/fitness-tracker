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
        "pec_upper_l" to "M105,95 C120,90 135,95 145,105 L105,115 Z",
        "pec_upper_r" to "M95,95 C80,90 65,95 55,105 L95,115 Z",
        "pec_lower_l" to "M105,115 L145,105 C150,130 145,160 105,165 Z",
        "pec_lower_r" to "M95,115 L55,105 C50,130 55,160 95,165 Z",
        
        "ant_delt_l" to "M145,90 C160,95 165,115 150,130 L140,110 Z",
        "ant_delt_r" to "M55,90 C40,95 35,115 50,130 L60,110 Z",
        
        "lat_delt_l" to "M155,105 C175,115 170,145 155,160 L145,130 Z",
        "lat_delt_r" to "M45,105 C25,115 30,145 45,160 L55,130 Z",
        
        "bicep_l" to "M145,145 C165,155 160,200 145,210 L135,180 Z",
        "bicep_r" to "M55,145 C35,155 40,200 55,210 L65,180 Z",
        
        "forearm_front_l" to "M145,220 C160,230 155,280 145,300 L135,250 Z",
        "forearm_front_r" to "M55,220 C40,230 45,280 55,300 L65,250 Z",
        
        "abs_upper" to "M85,175 L115,175 L115,200 L85,200 Z",
        "abs_mid" to "M85,205 L115,205 L115,230 L85,230 Z",
        "abs_lower" to "M85,235 L115,235 L110,270 L90,270 Z",
        "oblique_l" to "M118,185 C135,200 130,260 115,280 L110,230 Z",
        "oblique_r" to "M82,185 C65,200 70,260 85,280 L90,230 Z",
        
        "quad_l" to "M105,305 C135,315 145,400 125,445 L105,445 Z",
        "quad_r" to "M95,305 C65,315 55,400 75,445 L95,445 Z",
        
        "tibialis_l" to "M105,455 L125,455 L115,505 L105,505 Z",
        "tibialis_r" to "M95,455 L75,455 L85,505 L95,505 Z",

        // ── BACK MUSCLES ──
        "trap_upper_l" to "M100,45 L125,75 L110,95 L100,85 Z",
        "trap_upper_r" to "M100,45 L75,75 L90,95 L100,85 Z",
        "trap_mid" to "M85,85 L115,85 L110,130 L90,130 Z",
        
        "rear_delt_l" to "M145,95 C160,105 155,130 140,140 L135,110 Z",
        "rear_delt_r" to "M55,95 C40,105 45,130 60,140 L65,110 Z",
        
        "lat_l" to "M110,140 C140,150 145,240 120,265 L110,250 Z",
        "lat_r" to "M90,140 C60,150 55,240 80,265 L90,250 Z",
        
        "lower_back" to "M90,255 L110,255 L108,290 L92,290 Z",
        
        "tricep_l" to "M145,140 C165,150 160,205 145,215 L135,185 Z",
        "tricep_r" to "M55,140 C35,150 40,205 55,215 L65,185 Z",
        
        "glute_l" to "M105,295 C145,305 150,370 105,385 Z",
        "glute_r" to "M95,295 C55,305 50,370 95,385 Z",
        
        "hamstring_l" to "M105,395 C135,405 130,460 115,475 L105,475 Z",
        "hamstring_r" to "M95,395 C65,405 70,460 85,475 L95,475 Z",
        
        "calf_l" to "M105,485 C125,490 120,515 105,515 Z",
        "calf_r" to "M95,485 C75,490 80,515 95,515 Z"
    )

    private const val frontSilhouette = "M100,20 C115,20 130,35 130,60 L145,85 C175,100 195,140 185,180 L165,240 L155,320 L165,480 L135,510 L105,510 L100,500 L95,510 L65,510 L35,480 L45,320 L35,240 L15,180 C5,140 25,100 55,85 L70,60 C70,35 85,20 100,20 Z"
    private const val backSilhouette = "M100,20 C115,20 130,35 130,60 L150,85 C180,105 195,145 185,185 L165,245 L155,325 L165,485 L140,515 L100,510 L60,515 L35,485 L45,325 L35,245 L15,185 C5,145 20,105 50,85 L70,60 C70,35 85,20 100,20 Z"

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
