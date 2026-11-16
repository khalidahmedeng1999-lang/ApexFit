package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.db.entities.FoodItem
import com.example.ui.theme.*
import kotlin.math.roundToInt

@Composable
fun AiEstimateDialog(
    foodItem: FoodItem,
    onDismiss: () -> Unit,
    onEditValues: (FoodItem) -> Unit,
    onAddToDiary: (FoodItem) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = ApexCard,
            border = androidx.compose.foundation.BorderStroke(1.5.dp, ApexGold),
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(ApexGold.copy(alpha = 0.2f), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = ApexGold)
                        }
                        Column {
                            Surface(color = ApexGold.copy(alpha = 0.2f), shape = RoundedCornerShape(6.dp)) {
                                Text("AI ESTIMATE", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontSize = 10.sp, fontWeight = FontWeight.Black, color = ApexGold)
                            }
                            Text(foodItem.name, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = ApexTextPrimary)
                        }
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = ApexTextMuted)
                    }
                }

                HorizontalDivider(color = ApexCardBorder)

                // Warning Disclaimer Box (Requirement 58)
                Card(
                    colors = CardDefaults.cardColors(containerColor = ApexGold.copy(alpha = 0.12f)),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ApexGold.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Warning, contentDescription = "Warning", tint = ApexGold, modifier = Modifier.size(20.dp))
                        Text(
                            text = "⚠ AI ESTIMATE: Values may vary depending on exact ingredients, preparation method, and oil content.",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = ApexGold
                        )
                    }
                }

                // Estimated Nutrition Per 100g Box
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = ApexInput),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ApexCardBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "ESTIMATED NUTRITION (PER 100 G)",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = ApexGold,
                            letterSpacing = 0.8.sp
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("${foodItem.caloriesPer100g.roundToInt()} kcal", fontSize = 24.sp, fontWeight = FontWeight.Black, color = ApexEmerald)

                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("CARBS", fontSize = 9.sp, color = ApexTextMuted, fontWeight = FontWeight.Bold)
                                    Text("${foodItem.carbsPer100g}g", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = ApexTextPrimary)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("PROTEIN", fontSize = 9.sp, color = ApexTextMuted, fontWeight = FontWeight.Bold)
                                    Text("${foodItem.proteinPer100g}g", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = ApexCyan)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("FAT", fontSize = 9.sp, color = ApexTextMuted, fontWeight = FontWeight.Bold)
                                    Text("${foodItem.fatPer100g}g", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = ApexGold)
                                }
                            }
                        }
                    }
                }

                // Action Buttons: [ EDIT VALUES ] & [ ADD TO DIARY ]
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = { onEditValues(foodItem) },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("edit_ai_estimate_button"),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, ApexCyan),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ApexCyan)
                    ) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("EDIT VALUES", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { onAddToDiary(foodItem) },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("add_ai_estimate_to_diary_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ApexEmerald, contentColor = ApexBackground)
                    ) {
                        Text("ADD TO DIARY", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
        }
    }
}
