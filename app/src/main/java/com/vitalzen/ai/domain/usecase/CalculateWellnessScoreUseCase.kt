package com.vitalzen.ai.domain.usecase

import javax.inject.Inject

class CalculateWellnessScoreUseCase @Inject constructor() {
    operator fun invoke(heartRate: Int, oxygenLevel: Int, stressLevel: String): Int {
        var score = 100
        if (heartRate > 100 || heartRate < 60) score -= 10
        if (oxygenLevel > 0 && oxygenLevel < 95) score -= 20
        if (stressLevel == "High") score -= 15
        return score.coerceIn(0, 100)
    }
}
