package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.QrCodeScanner
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

@Composable
fun LabelScannerDialog(
    onDismiss: () -> Unit,
    onConfirmScannedFood: (String) -> Unit
) {
    var rawText by remember { mutableStateOf("Nutrition Facts\nServing Size: 50 g\nCalories: 180 kcal\nProtein: 12 g\nCarbohydrates: 20 g\nFat: 6 g") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = com.example.ui.theme.ApexCard,
            border = androidx.compose.foundation.BorderStroke(1.5.dp, com.example.ui.theme.ApexCyan),
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(com.example.ui.theme.ApexCyan.copy(alpha = 0.2f), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.QrCodeScanner, contentDescription = null, tint = com.example.ui.theme.ApexCyan)
                        }
                        Column {
                            Text("NUTRITION LABEL SCANNER", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = com.example.ui.theme.ApexCyan)
                            Text("Scan or Paste Label", fontSize = 16.sp, fontWeight = FontWeight.Black, color = com.example.ui.theme.ApexTextPrimary)
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = com.example.ui.theme.ApexTextMuted)
                    }
                }

                HorizontalDivider(color = com.example.ui.theme.ApexCardBorder)

                Text("Paste or edit the label text below. AI/OCR will extract serving size, calories, and macros:", fontSize = 12.sp, color = com.example.ui.theme.ApexTextMuted)

                OutlinedTextField(
                    value = rawText,
                    onValueChange = { rawText = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .testTag("label_scanner_text_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = com.example.ui.theme.ApexInput,
                        unfocusedContainerColor = com.example.ui.theme.ApexInput,
                        focusedBorderColor = com.example.ui.theme.ApexCyan,
                        unfocusedBorderColor = com.example.ui.theme.ApexCardBorder,
                        focusedTextColor = com.example.ui.theme.ApexTextPrimary,
                        unfocusedTextColor = com.example.ui.theme.ApexTextPrimary
                    )
                )

                Button(
                    onClick = {
                        if (rawText.isNotBlank()) {
                            onConfirmScannedFood(rawText)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("extract_label_data_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = com.example.ui.theme.ApexCyan, contentColor = com.example.ui.theme.ApexBackground),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("EXTRACT & CONVERT TO FOOD ITEM", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
                }
            }
        }
    }
}
