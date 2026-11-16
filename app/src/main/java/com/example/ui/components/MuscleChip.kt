package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun getMuscleColor(muscle: String): Color {
    return when (muscle.lowercase()) {
        "lats", "mid back", "traps" -> ApexEmerald
        "chest" -> ApexCoral
        "side delts", "rear delts" -> ApexCyan
        "biceps", "triceps" -> Color(0xFFC084FC) // Vibrant Purple
        "quads", "hamstrings", "calves" -> ApexAmber
        "abs" -> Color(0xFFF43F5E) // Vibrant Rose
        else -> ApexEmerald
    }
}

@Composable
fun MuscleChip(
    muscle: String,
    modifier: Modifier = Modifier
) {
    val color = getMuscleColor(muscle)
    Text(
        text = muscle.uppercase(),
        color = color,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.5.sp,
        modifier = modifier
            .background(color.copy(alpha = 0.12f), RoundedCornerShape(6.dp))
            .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    )
}
