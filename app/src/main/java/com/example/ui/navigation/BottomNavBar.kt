package com.example.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.WaterDrop

enum class BottomTab(
    val title: String,
    val icon: ImageVector,
    val tag: String
) {
    Dashboard("Dashboard", Icons.Default.Dashboard, "tab_dashboard"),
    Workout("Workout", Icons.Default.FitnessCenter, "tab_workout"),
    Nutrition("Nutrition", Icons.Default.Restaurant, "tab_nutrition"),
    Hydration("Water", Icons.Default.WaterDrop, "tab_hydration"),
    Volume("Volume", Icons.Default.BarChart, "tab_volume"),
    History("History", Icons.Default.History, "tab_history"),
    Profile("Profile", Icons.Default.Person, "tab_profile")
}

@Composable
fun BottomNavBar(
    selectedTab: BottomTab,
    onTabSelected: (BottomTab) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier
            .windowInsetsPadding(WindowInsets.navigationBars),
        containerColor = androidx.compose.ui.graphics.Color(0xFF0F0F0F),
        tonalElevation = 0.dp
    ) {
        BottomTab.entries.forEach { tab ->
            val isSelected = tab == selectedTab
            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(tab) },
                icon = {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = tab.title,
                        tint = if (isSelected) ApexBackground else ApexTextMuted
                    )
                },
                label = {
                    Text(
                        text = tab.title,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium,
                        color = if (isSelected) ApexEmerald else ApexTextMuted
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = ApexEmerald,
                    unselectedIconColor = ApexTextMuted,
                    selectedIconColor = ApexBackground
                ),
                modifier = Modifier.testTag(tab.tag)
            )
        }
    }
}
